package love.forte.common.di

/**
 *
 * 当通过类型获取出现重复bean类型的时候
 *
 * @author ForteScarlet
 */
public open class MultiSameTypeBeanDefinedException : IllegalStateException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}