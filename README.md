# Grails Mongo Demo with Java 8 Time Type

### Create App

```bash
sdk use grails 5.1.7
grails create-app org.grails.demo.grails-mongo-date-demo --profile rest-api
```

### Add GORM for MongoDB Dependency

```gradle
dependencies {
...
    implementation 'org.grails.plugins:mongodb'
...
}
```

### Create Domain and Controller

```bash
grails create-controller org.grails.demo.Post
```

```groovy
package org.grails.demo

import java.time.LocalDate
import java.time.ZonedDateTime

class Post {
    String title
    LocalDate day
    ZonedDateTime createdDate

    static constraints = {
    }
}
```

```groovy
package org.grails.demo

import grails.rest.*
import grails.converters.*

class PostController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        respond Post.list()
    }

    def save(Post post) {
        post.save()

        respond post
    }
}
```

### Test

1. Add post

```bash
➜  ~ http :8080/post title=Abc day=2020-10-01 createdDate=2020-10-01T10:10:00+0800

HTTP/1.1 200
Connection: keep-alive
Content-Language: en-CN
Content-Type: application/json;charset=UTF-8
Date: Tue, 19 Jul 2022 13:54:08 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "createdDate": "2020-10-01T10:10:00+08:00",
    "day": "2020-10-01",
    "id": 1,
    "title": "Abc"
}
```

2. Get posts

```bash
➜  ~ http :8080/post
HTTP/1.1 500
Connection: close
Content-Language: en-CN
Content-Type: application/json;charset=UTF-8
Date: Tue, 19 Jul 2022 13:54:20 GMT
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "error": 500,
    "message": "Internal server error"
}
```

3. Errors

```bash
Caused by: org.springframework.beans.ConversionNotSupportedException: Failed to convert property value of type 'java.lang.String' to required type 'java.time.ZonedDateTime' for property 'createdDate'; nested exception is java.lang.IllegalStateException: Cannot convert value of type 'java.lang.String' to required type 'java.time.ZonedDateTime' for property 'createdDate': no matching editors or conversion strategy found
        at org.springframework.beans.AbstractNestablePropertyAccessor.convertIfNecessary(AbstractNestablePropertyAccessor.java:595)
        at org.springframework.beans.AbstractNestablePropertyAccessor.convertForProperty(AbstractNestablePropertyAccessor.java:609)
        at org.springframework.beans.AbstractNestablePropertyAccessor.processLocalProperty(AbstractNestablePropertyAccessor.java:458)
        at org.springframework.beans.AbstractNestablePropertyAccessor.setPropertyValue(AbstractNestablePropertyAccessor.java:278)
        at org.springframework.beans.AbstractNestablePropertyAccessor.setPropertyValue(AbstractNestablePropertyAccessor.java:246)
        at org.grails.datastore.mapping.engine.BeanEntityAccess.setProperty(BeanEntityAccess.java:88)
        at org.grails.datastore.mapping.engine.NativeEntryEntityPersister$NativeEntryModifyingEntityAccess.setProperty(NativeEntryEntityPersister.java:1650)
```

### How to Fix it

1. Add `MappingContextBeanPostProcessor` in `src/main/groovy`

```groovy
class MappingContextBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
    private BeanFactory beanFactory

    @Override
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName == 'mongoMappingContext') {
            def mappingContext = this.beanFactory.getBean(beanName, MongoMappingContext)
            mappingContext.addTypeConverter(new Converter<String, LocalDate>() {
                @Override
                LocalDate convert(String source) {
                    return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            })
            mappingContext.addTypeConverter(new Converter<String, ZonedDateTime>() {
                @Override
                ZonedDateTime convert(String source) {
                    return ZonedDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }
            })
        }
    }

    @Override
    void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory
    }
}
```

2. Define bean in `grails-app/conf/spring/resources.groovy`

```groovy
// Place your Spring DSL code here
beans = {
    mappingContextBeanPostProcessor(MappingContextBeanPostProcessor)
}
```

3. Check it again

```bash
➜  ~ http :8080/post
HTTP/1.1 200
Connection: keep-alive
Content-Language: en-CN
Content-Type: application/json;charset=UTF-8
Date: Tue, 19 Jul 2022 14:07:11 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

[
    {
        "createdDate": "2020-10-01T10:10:00+08:00",
        "day": "2020-10-01",
        "id": 1,
        "title": "Abc"
    }
]
```

### Why?

In `gorm-mogodb`, there are some converters for Java 8 time types missing in `MongoMappingContext`, we can add these converters to fix the issue.

```groovy

    private void initialize(Class[] classes) {
...
        converterRegistry.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        });

        converterRegistry.addConverter(new Converter<String, ZonedDateTime>() {
            @Override
            public ZonedDateTime convert(String source) {
                return ZonedDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        });
...
    }

```

### Links

* [[Grails 5] Unclear how to enable codecs for Java 8 date/time class marshalling](https://github.com/grails/gorm-mongodb/issues/539)
