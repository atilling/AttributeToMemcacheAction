package net.unicon.idp;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.Action;
import net.spy.memcached.MemcachedClient;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.IdPAttributePrincipal;
import net.shibboleth.idp.attribute.IdPAttribute;

import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.security.Principal;

import javax.security.auth.Subject;

public class AttributeToMemcacheAction implements Action {

    private final String memcacheHost="127.0.0.1";
    private final int memcachePort=11211;
    private MemcachedClient memcachedClient;

    public AttributeToMemcacheAction() throws Exception {
        memcachedClient = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));
    }

    @Override
    public Event execute(RequestContext springRequestContext) {
    	
    	Map<String, Object> attributes=new HashMap();
    	
        ProfileRequestContext profileRequestContext = (ProfileRequestContext) springRequestContext.getExternalContext().getNativeRequest();
        SubjectContext subjectContext = profileRequestContext.getSubcontext(SubjectContext.class);
        Subject subject = subjectContext.getSubjects().get(0);
        Set<IdPAttributePrincipal> principals = subject.getPrincipals(IdPAttributePrincipal.class);
        for (IdPAttributePrincipal principal:principals) {
        	IdPAttribute attribute = principal.getAttribute();
        	attributes.put(attribute.getId(),attribute.getValues());
        }
        

        try {
            memcachedClient.set("userAttributes", 3600, attributes);
        } catch (Exception e) {
            return new Event(this, "fail");
        }

        return new Event(this, "success");
    }
}