package test1

import love.forte.di.annotation.Beans
import javax.inject.Inject

/**
 *
 * @author ForteScarlet
 */
@Beans
class MyController {

    @set:Inject
    lateinit var serviceGetter: () -> Service


    fun value() {
        val service = serviceGetter()
        println(service)
        println(service.str)
    }

}