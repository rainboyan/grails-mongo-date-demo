package org.grails.demo

import org.grails.datastore.mapping.mongo.config.MongoMappingContext
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.convert.converter.Converter

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
