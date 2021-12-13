package love.forte.di.core.internal

import love.forte.di.*
import love.forte.di.core.CoreBeanManager
import love.forte.di.core.CoreBeanManagerBeanRegisterPostProcessor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf

/**
 * [CoreBeanManager] 基础实现.
 *
 * [CoreBeanManager] 不关系 [Bean] 的初始化、依赖关系、初始化或者单例情况，这一切都应由注册者决定。
 *
 * 修改之间线程安全，但是获取的时候不会。
 *
 *
 * @author ForteScarlet
 *
 * @param processorList 后置处理器列表
 * @param parentContainer 父容器。任何获取与注册检测都会优先使用父容器。
 */
internal class CoreBeanManagerImpl(
    processorList: List<CoreBeanManagerBeanRegisterPostProcessor>,
    override val parentContainer: BeanContainer = BeanContainer
) : CoreBeanManager, HierarchicalBeanContainer {
    private val processors = processorList.sorted()

    /** Bean与其对应唯一名称的Map。 */
    private val nameBeanMap = ConcurrentHashMap<String, Bean<*>>()

    /** 从 [nameBeanMap] 中获取对应类型的 [Bean] 并记录其类型对应关系。 */
    private val typeBeanMap = ConcurrentHashMap<KClass<*>, Bean<*>>()


    /**
     * 直接注册一个 [Bean]. 进行处理后验证其名称.
     */
    override fun register(name: String, bean: Bean<*>) {
        var beanProcessed: Bean<*>? = bean
        for (processor in processors) {
            beanProcessed = processor.process(beanProcessed!!, this)
            if (beanProcessed == null) {
                return
            }
        }

        beanProcessed as Bean<*>

        synchronized(nameBeanMap) {
            // check name
            if (contains(name)) {
                throw MultiSameTypeBeanException(name)
            }

            // not container, register to local
            nameBeanMap[name] = bean

        }
    }


    override fun contains(name: String): Boolean {
        return name in parentContainer || containsLocal(name)
    }


    override fun containsLocal(name: String): Boolean {
        return nameBeanMap.containsKey(name)
    }

    override fun getOrNull(name: String): Any? {
        return nameBeanMap[name]?.get()
    }

    override fun <T : Any> getOrNull(type: KClass<T>): T? {
        return typeBeanMap[type]?.let { type.cast(it.get()) }
            ?: synchronized(nameBeanMap) {
                val subTypes = nameBeanMap.values.filter { b -> b.type.isSubclassOf(type) }
                when {
                    subTypes.isEmpty() -> return null
                    subTypes.size == 1 -> {
                        val only = subTypes.first()
                        return only.getWithCast { type }.also {
                            typeBeanMap[type] = only
                        }
                    }
                    else -> {
                        // try find
                        val isPreferredList = subTypes.filter(Bean<*>::isPreferred)
                        if (isPreferredList.size == 1) {
                            val only = isPreferredList.first()
                            return only.getWithCast { type }.also {
                                typeBeanMap[type] = only
                            }
                        } else {
                            throw MultiSameTypeBeanException("$type")
                        }
                    }
                }
            }

    }

    override fun <T : Any> getAll(type: KClass<T>): List<String> {
        return nameBeanMap.keys().toList()
    }
}