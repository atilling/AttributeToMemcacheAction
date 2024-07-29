package net.unicon.idp;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.action.AbstractProfileAction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.SecurityException;
import org.opensaml.core.xml.XMLObject;

import org.opensaml.storage.impl.memcached.MemcachedStorageService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
public class AttributeToMemcacheAction extends AbstractProfileAction {

    private final MemcachedStorageService memcachedStorageService;
    private final ObjectMapper objectMapper;

    public AttributeToMemcacheAction(MemcachedStorageService memcachedStorageService, ObjectMapper objectMapper) {
        this.memcachedStorageService = memcachedStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean doPreExecute(ProfileRequestContext profileRequestContext) {
        // Extract attributes
        List<Attribute> attributes = extractAttributes(profileRequestContext);

        // Create a new attribute object
        MyAttributeObject attributeObject = new MyAttributeObject();
        populateAttributeObject(attributes, attributeObject);

        // Generate a unique key based on user information (adjust as needed)
        String userId = extractEduPersonPrincipalName(attributes);
        
        String memcacheKey = "user_" + userId;

        // Serialize to JSON
        try {
	        String jsonString = objectMapper.writeValueAsString(attributeObject);
	
	        // Store in Memcache with expiration
	        memcachedStorageService.create("Attribs",memcacheKey,jsonString,(long)3600);  // Adjust expiration as needed
        } catch (Exception e) {
        	//error writing json to memcache
        }
        return true;
    }

    private List<Attribute> extractAttributes(ProfileRequestContext profileRequestContext) {
    	MessageContext messageContext = profileRequestContext.getSubcontext(MessageContext.class);
    	Object message =  messageContext.getMessage();
    	if (message instanceof Assertion) {
        	Assertion assertion = (Assertion)message;
	        List<AttributeStatement> attributeStatement = assertion.getAttributeStatements();
	        return attributeStatement.get(0).getAttributes();
    	} else {
    		return List.of();
    	}
    }
    
    private Attribute getAttributeByName(List<Attribute> attributes, String name) {
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null; // Or handle missing attribute as needed
    }
    

    private void populateAttributeObject(List<Attribute> attributes, MyAttributeObject attributeObject) {
        for (Attribute attribute : attributes) {
            String attributeName = attribute.getName();
            List<XMLObject> attributeValues = attribute.getAttributeValues();
            for (XMLObject xmlObject : attributeValues) {
                if (xmlObject instanceof AttributeValue) {
                    AttributeValue attributeValue = (AttributeValue) xmlObject;
                    attributeObject.addAttribute(attributeName, attributeValue.DEFAULT_ELEMENT_LOCAL_NAME);
                } else {
                    // Handle other XMLObject types if needed
                }
            }
        }
    }
    
    private String extractEduPersonPrincipalName(List<Attribute> attributes) {
    	// Assuming eduPersonPrincipalName is present 
    	Attribute eduPersonPrincipalNameAttribute = getAttributeByName(attributes, "eduPersonPrincipalName");
    	return eduPersonPrincipalNameAttribute.getAttributeValues().get(0).toString();
    }
    
    public static class MyAttributeObject {
        private Map<String, Object> attributes = new HashMap<>();

        public void addAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        public Object getAttribute(String name) {
            return attributes.get(name);
        }
    }
}