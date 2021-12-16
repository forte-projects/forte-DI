package love.forte.di

import javax.inject.Provider
import kotlin.reflect.KClass
import kotlin.reflect.cast


/**
 *
 * 一个存于 [BeanManager] 中的 [Bean].
 *
 * [Bean] 是对一个依赖的定义, 他们最终都能够通过 [get] 来得到一个最终实例，并且不可为null。
 *
 *
 * @author ForteScarlet
 */
public interface Bean<T : Any> : Provider<T>, BeanDescription {

    /**
     * 是否首选的.
     * 当通过类型获取时，会尝试优先选择 [isPreferred] == true 的元素。
     *
     * 在bean管理器中，对应类型能够得到的所有结果中应当至多存在一个 [isPreferred] == true 的结果。
     */
    public val isPreferred: Boolean

    /**
     * 此Bean是否为单例。如果是单例，则在调用一次 [get] 之后应由管理器保存其实例。
     */
    public val isSingleton: Boolean

    /**
     * 这个 [Bean] 的实际类型。
     */
    override val type: KClass<T>

    /**
     * 得到这个依赖的结果值。
     */
    override fun get(): T
}


public inline fun <T : Any> Bean<*>.getWithCast(type: () -> KClass<T>): T = type().cast(get())


/** [Bean]'s [value][Bean.get] */
public inline
val <T : Any> Bean<T>.value: T
    get() = get()


/**
 * 代理一个 [Bean] 并对他的返回值进行处理。
 */
public fun <T : Any> Bean<T>.postValue(block: (source: Bean<T>, T) -> T): Bean<T> = PostValueBean(block, this)


private class PostValueBean<T : Any>(private val processor: (Bean<T>, T) -> T, private val delegate: Bean<T>) :
    Bean<T> by delegate {
    override fun get(): T {
        return processor(delegate, delegate.get())
    }
}

