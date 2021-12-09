package love.forte.common.di

import javax.inject.Provider
import kotlin.reflect.KClass


/**
 *
 * 一个存于 [BeanManager] 中的 [Bean].
 *
 * [Bean] 是对一个依赖的定义, 他们最终都能够通过 [get] 来得到一个最终实例，并且不可为null。
 *
 *
 * @author ForteScarlet
 */
public interface Bean<T : Any> : Provider<T> {
    /**
     * [Bean] 的名称。[name] 在同一个 [BeanManager] 范围内唯一。
     */
    public val name: String

    /**
     * 这个 [Bean] 的描述类型。
     */
    public val type: KClass<T>

    /**
     * 得到这个依赖的结果值。
     */
    override fun get(): T
}

public inline val <T : Any> Bean<T>.value: T get() = get()

