package test2

import love.forte.annotationtool.core.KAnnotationTool
import love.forte.annotationtool.core.getAnnotation
import love.forte.di.annotation.Beans
import org.springframework.beans.factory.annotation.Autowired
import javax.annotation.processing.AbstractProcessor
import javax.inject.Inject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.getExtensionDelegate
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


class Foo2 {

    @Inject
    @Autowired
    lateinit var name: String

    @Inject
    @Autowired
    var age: Int = 1
}


@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val tool = KAnnotationTool()
    val prop = Foo2::class.memberProperties.first { p -> p.name == "age" }
    println(prop)

    println(prop.findAnnotations<Inject>())
    println(prop.annotations)
    println(prop.findAnnotations(Inject::class))

    prop as KMutableProperty<*>


    println()
    println(tool.getAnnotation<Inject>(prop))
    println(tool.getAnnotation<Inject>(prop.getter))
    println(tool.getAnnotation<Inject>(prop.setter))


}