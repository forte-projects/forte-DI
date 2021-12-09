package love.forte.common.di

/**
 *
 * 当获取指定Bean，但是没有对应结果的时候。
 * @author ForteScarlet
 */
public open class NoSuchBeanDefineException : IllegalStateException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}