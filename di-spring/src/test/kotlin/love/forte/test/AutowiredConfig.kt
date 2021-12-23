package love.forte.test

import love.forte.di.annotation.Depend
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor
import org.springframework.stereotype.Component


private fun getMyAutowiredAnnotationBeanPostProcessor(): AutowiredAnnotationBeanPostProcessor =
    AutowiredAnnotationBeanPostProcessor().also {
        it.setRequiredParameterName("required")
        it.setAutowiredAnnotationType(Depend::class.java)
    }

/**
 *
 * @author ForteScarlet
 */
@Component
open class AutowiredConfig :
    SmartInstantiationAwareBeanPostProcessor
    by getMyAutowiredAnnotationBeanPostProcessor()