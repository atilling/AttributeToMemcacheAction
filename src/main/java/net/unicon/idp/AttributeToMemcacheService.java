package net.unicon.idp;

import org.opensaml.profile.context.ProfileRequestContext;

import org.opensaml.storage.impl.memcached.MemcachedStorageService;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.IdPAttributePrincipal;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.logic.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeToMemcacheService implements Predicate<ProfileRequestContext>  {
	
	private static final Logger log = LoggerFactory.getLogger(AttributeToMemcacheService.class);

    private MemcachedStorageService memcachedStorageService;
    private final ObjectMapper objectMapper;
    private String keyName;
    
    public AttributeToMemcacheService(MemcachedStorageService memcachedStorageService, ObjectMapper objectMapper, String keyName) throws Exception {
    	this.memcachedStorageService = memcachedStorageService;
        this.objectMapper = objectMapper;
    	this.keyName = keyName;
    }
    
    @Override
    public boolean test(@Nullable ProfileRequestContext profileRequestContext)  {
    	
    	log.debug ("Starting save of attributes");
    	
    	String idKey = "default";
    	
    	Map<String, Object> attributes=new HashMap<String, Object>();
    	SubjectContext subjectContext = profileRequestContext.getSubcontext(SubjectContext.class);
        Subject subject = subjectContext.getSubjects().get(0);
        Set<IdPAttributePrincipal> principals = subject.getPrincipals(IdPAttributePrincipal.class);
        for (IdPAttributePrincipal principal:principals) {
        	IdPAttribute attribute = principal.getAttribute();
        	log.debug("Add attribute: "+ attribute.getId());
        	if (attribute.getId().equals(keyName)) {
        		idKey=attribute.getValues().get(0).toString();
        	}
        	attributes.put(attribute.getId(),attribute.getValues());
        }
        

        try {
        	
        	String jsonString = objectMapper.writeValueAsString(attributes);
        	 
        	memcachedStorageService.create("Attribs",idKey,jsonString,(long)3600);
        	
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}