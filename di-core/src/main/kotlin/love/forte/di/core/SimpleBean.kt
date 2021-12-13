package love.forte.di.core

import love.forte.di.Bean
import kotlin.reflect.KClass

@DslMarker
@Retention(AnnotationRetention.BINARY)
internal annotation class SimpleBeanBuilderDSL

/**
 *
 * 基础的 [Bean] 注册器。
 *
 * @author ForteScarlet
 */
public class SimpleBeanBuilder<T : Any>(
    @Suppress("MemberVisibilityCanBePrivate")
    public val type: KClass<T>
) {

    @SimpleBeanBuilderDSL
    public var isPreferred: Boolean = false

    @SimpleBeanBuilderDSL
    public var isSingleton: Boolean = true


    @SimpleBeanBuilderDSL
    public fun preferred(): SimpleBeanBuilder<T> = also {
        isPreferred = true
    }

    @SimpleBeanBuilderDSL
    public fun singleton(): SimpleBeanBuilder<T> = also {
        isSingleton = true
    }

    @SimpleBeanBuilderDSL
    public var factory: (() -> T)? = null

    @SimpleBeanBuilderDSL
    public fun factory(block: () -> T): SimpleBeanBuilder<T> = also {
        this.factory = block
    }

    public fun build(): Bean<T> = SimpleBean(
        type, isPreferred, isSingleton, factory.ifNull { "Bean's factory function was null" }
    )
}

internal class SimpleBean<T : Any>(
    override val type: KClass<T>,
    override val isPreferred: Boolean,
    override val isSingleton: Boolean = true,
    private val getter: () -> T,
) : Bean<T> {
    override fun get(): T = getter()
}

