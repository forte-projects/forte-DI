package love.forte.di.spring.internal

import love.forte.di.Bean
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder


public fun <T : Any> Bean<T>.toBeanDefinition(): BeanDefinition {
    return BeanDefinitionBuilder.genericBeanDefinition(type.java, this::get).beanDefinition
}


public inline fun <reified T : Any> typedBeanDefinition(): BeanDefinition {
    return BeanDefinitionBuilder.genericBeanDefinition(T::class.java).beanDefinition
}