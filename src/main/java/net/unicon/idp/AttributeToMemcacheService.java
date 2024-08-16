package net.unicon.idp;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.IdPAttributePrincipal;
import net.shibboleth.idp.attribute.IdPAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Predicate;
import java.util.HashMap;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AttributeToMemcacheService implements Predicate<ProfileRequestContext>  {
	
	private static final Logger log = LoggerFactory.getLogger(AttributeToMemcacheService.class);
    
	private MemcachedClient memcachedClient;
    private String server="127.0.0.1";
    private int port=11211;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String keyName="eduPersonPrincipalName";
    
	
	
    public AttributeToMemcacheService (String server, int port, ObjectMapper objectMapper, String keyName) {

	    try {
	    	this.server = server;
	    	this.port = port;
	        this.objectMapper = objectMapper;
	    	this.keyName = keyName;
	    	this.memcachedClient=new MemcachedClient(new InetSocketAddress(this.server,this.port));
    	} catch (Exception e) {
    		log.error("Error thrown: " + e.getMessage());
    	}
    }
    
    
    @Override
    public boolean test(@Nullable ProfileRequestContext profileRequestContext) {
    	/*
    	log.debug ("Starting save of attributes");
    	
    	String idKey = "default";

    	Map<String, Object> attributes=new HashMap<String, Object>();
    	
    	try {
	    	SubjectContext subjectContext = profileRequestContext.getSubcontext(SubjectContext.class);
	        Subject subject = subjectContext.getSubjects().get(0);
	        Set<IdPAttributePrincipal> principals = subject.getPrincipals(IdPAttributePrincipal.class);
	        for (IdPAttributePrincipal principal:principals) {
	        	IdPAttribute attribute = principal.getAttribute();
	        	log.debug("Add attribute: "+ attribute.getId());
	        	if (attribute.getId().equals(keyName)) {
	        		log.debug("key attribute value: "+ attribute.getValues().get(0).toString());
	        		idKey=attribute.getValues().get(0).toString();
	        	}
	        	attributes.put(attribute.getId(),attribute.getValues());
        }
    	} catch (Exception e) {
        	log.warn("Error thrown: " + e.getMessage());
            return false;
        }

        try {
        	
        	String jsonString = objectMapper.writeValueAsString(attributes);
        	
        	log.debug("jsonstring: " + jsonString);
        	this.memcachedClient.add(idKey,3600,jsonString);
        	
        } catch (Exception e) {
        	log.warn("Error thrown: " + e.getMessage());
            return false;
        }
		*/
        return true;
    }
}