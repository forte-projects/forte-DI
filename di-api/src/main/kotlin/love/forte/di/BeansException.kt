package love.forte.di

/**
 *
 * @author ForteScarlet
 */
public open class BeansException : IllegalStateException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}