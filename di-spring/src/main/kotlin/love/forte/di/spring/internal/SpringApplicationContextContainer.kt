package love.forte.di.spring.internal

import love.forte.di.*
import love.forte.di.spring.SpringBeanContainer
import love.forte.di.spring.SpringBeanManager
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.NoUniqueBeanDefinitionException
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import kotlin.reflect.KClass

/**
 *
 * 使用 [ListableBeanFactory] 作为功能支持的 [SpringBeanContainer].
 *
 *
 * @author ForteScarlet
 */
internal class SpringApplicationContextContainer(
    override val beanDefinitionRegistry: BeanDefinitionRegistry,
    override val listableBeanFactory: ListableBeanFactory,
) : SpringBeanManager {

    override fun register(name: String, bean: Bean<*>) {
        beanDefinitionRegistry.registerBeanDefinition(name, bean.toBeanDefinition())
    }

    //// ———————— container ———————— ////

    override fun contains(name: String): Boolean {
        return listableBeanFactory.containsBean(name)
    }

    override fun get(name: String): Any {
        return try {
            listableBeanFactory.getBean(name)
        } catch (e: NoSuchBeanDefinitionException) {
            throw NoSuchBeanException("named $name", e)
        }
    }


    override fun <T : Any> get(name: String, type: KClass<T>): T {
        return try {
            listableBeanFactory.getBean(name, type.java)
        } catch (e: NoSuchBeanDefinitionException) {
            throw NoSuchBeanException("named $name with type of $type", e)
        }
    }

    override fun <T : Any> getOrNull(name: String, type: KClass<T>): T? {
        return if (name in this) this[name, type] else null
    }

    @Api4J
    override fun <T : Any> get(name: String, type: Class<T>): T {
        return try {
            listableBeanFactory.getBean(name, type)
        } catch (e: NoSuchBeanDefinitionException) {
            throw NoSuchBeanException("named $name with type of $type", e)
        }
    }

    @Api4J
    override fun <T : Any> getOrNull(name: String, type: Class<T>): T? {
        return if (name in this) this[name, type] else null
    }

    override fun <T : Any> get(type: KClass<T>): T {
        return try {
            listableBeanFactory.getBean(type.java)
        } catch (e: NoSuchBeanDefinitionException) {
            throw NoSuchBeanException("type of $type", e)
        } catch (e: NoUniqueBeanDefinitionException) {
            throw MultiSameTypeBeanException(type.toString(), e)
        }
    }

    @Api4J
    override fun <T : Any> get(type: Class<T>): T {
        return try {
            listableBeanFactory.getBean(type)
        } catch (e: NoSuchBeanDefinitionException) {
            throw NoSuchBeanException("type of $type", e)
        } catch (e: NoUniqueBeanDefinitionException) {
            throw MultiSameTypeBeanException(type.toString(), e)
        }
    }

    @Api4J
    override fun <T : Any> getOrNull(type: Class<T>): T? {
        val names = listableBeanFactory.getBeanNamesForType(type)
        return when {
            names.isEmpty() -> null
            names.size == 1 -> this[names[0], type]
            else -> try {
                get(type)
            } catch (e: NoSuchBeanDefinitionException) {
                null
            } catch (e: NoUniqueBeanDefinitionException) {
                throw MultiSameTypeBeanException(type.toString(), e)
            }
        }
    }

    @Api4J
    override fun <T : Any> getAll(type: Class<T>?): List<String> {
        return listableBeanFactory.getBeanNamesForType(type).toList()
    }

    override fun getOrNull(name: String): Any? {
        return if (name in this) this[name] else null
    }

    override fun <T : Any> getOrNull(type: KClass<T>): T? {
        val names = listableBeanFactory.getBeanNamesForType(type.java)
        return when {
            names.isEmpty() -> null
            names.size == 1 -> this[names[0], type]
            else -> try {
                listableBeanFactory.getBean(type.java)
            } catch (e: NoSuchBeanDefinitionException) {
                null
            } catch (e: NoUniqueBeanDefinitionException) {
                throw MultiSameTypeBeanException(type.toString(), e)
            }
        }
    }

    override fun <T : Any> getAll(type: KClass<T>?): List<String> {
        return listableBeanFactory.getBeanNamesForType(type?.java).toList()
    }



    @OptIn(Api4J::class)
    override fun getTypeOrNull(name: String): KClass<*>? = getTypeClassOrNull(name)?.kotlin

    @OptIn(Api4J::class)
    override fun getType(name: String): KClass<*> = getTypeClass(name).kotlin

    @Api4J
    override fun getTypeClassOrNull(name: String): Class<*>? {
        return try {
            listableBeanFactory.getType(name)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }
    }

    @Api4J
    override fun getTypeClass(name: String): Class<*> {
        return try {
            super.getTypeClass(name)
        } catch (e: NoSuchBeanDefinitionException) {
            noSuchBeanDefine(e) { name }
        }
    }
}