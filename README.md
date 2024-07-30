# AttributeToMemcacheAction

To add to gobal.xml
```xml
```<bean id="shibboleth.MemcachedStorageService"
          class="org.opensaml.storage.impl.memcached.MemcachedStorageService"
          c:timeout="2">
        <constructor-arg name="client">
            <bean class="net.spy.memcached.spring.MemcachedClientFactoryBean"
                  p:servers="127.0.0.1:11211"
                  p:protocol="BINARY"
                  p:locatorType="CONSISTENT"
                  p:failureMode="Redistribute">
                <property name="hashAlg">
                    <util:constant static-field="net.spy.memcached.DefaultHashAlgorithm.FNV1_64_HASH" />
                </property>
                <property name="transcoder">
                    <!-- DO NOT MODIFY THIS PROPERTY -->
                    <bean class="org.opensaml.storage.impl.memcached.StorageRecordTranscoder" />
                </property>
            </bean>
        </constructor-arg>
    </bean>  

    <bean id="objectMapper" class="com.fasterxml.jackson.databind.ObjectMapper" />
    
    <bean id="AttributeToMemcacheAction" class="net.unicon.idp.AttributeToMemcacheAction">
        <constructor-arg ref="shibboleth.MemcachedStorageService" />
        <constructor-arg ref="objectMapper" />
        <constructor-arg name="keyName" value="eduPersonPrincipalName" />
    </bean>
```
```


Add to intercept-events-flow.xml
```xml
```<global-transitions>
        <transition on="AttributeToMemcacheAction" to="AttributeToMemcacheAction" />
		<transition on="proceed" to="proceed"/>
		<transition on="error" to="error"/>
    </global-transitions>
```
```