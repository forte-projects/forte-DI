package love.forte.di

/**
 *
 * 当获取指定Bean，但是没有对应结果的时候。
 * @author ForteScarlet
 */
public open class NoSuchBeanException : BeansException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public inline fun noSuchBeanDefine(e: Throwable? = null, name: () -> String): Nothing {
    val n = name()
    throw NoSuchBeanException("named $n", e)
}
