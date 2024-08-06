# AttributeToMemcacheService

To add to `conf/gobal.xml`:

```xml
<bean id="shibboleth.MemcachedStorageService"
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
    
    <bean p:id="intercept/AttributeToMemcache" parent="shibboleth.InterceptFlow" />
```


Add to `/conf/intercept/profile-intercept.xml`:

```xml
    <bean id="shibboleth.AvailableInterceptFlows" parent="shibboleth.DefaultInterceptFlows" lazy-init="true">
        <property name="sourceList">
            <list merge="true">

                <bean id="intercept/AttributeToMemcache" parent="shibboleth.InterceptFlow" />
            </list>
        </property>
    </bean>
```


add to `/flows/intercept/AttributeToMemcache/AttributeToMemcache-beans.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    
       <bean id="AttributeToMemcacheService" class="net.unicon.idp.AttributeToMemcacheService"
		c_0:memcachedStorageService-ref="shibboleth.MemcachedStorageService"
		c_1:objectMapper-ref="shibboleth.JSONObjectMapper"
		c_2:keyName="eduPersonPrincipalName"/>

```

add to `/flows/intercept/AttributeToMemcache/AttributeToMemcache-flow.xml`:

```xml
<flow xmlns="http://www.springframework.org/schema/webflow"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/webflow http://www.springframework.org/schema/webflow/spring-webflow.xsd"
      parent="intercept.abstract">

    <decision-state id="CheckContext">
        <if test="AttributeToMemcacheService.test(opensamlProfileRequestContext)"
            then="proceed" else="Error" />
    </decision-state>

    <bean-import resource="AttributeToMemcache-beans.xml" />

</flow>
```