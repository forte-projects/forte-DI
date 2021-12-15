import javax.inject.Inject
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class PropAnn

class Home {

    @Inject // javax.inject.Inject
    @PropAnn
    var name: String = ""

}

fun main() {
    println(Home::name.findAnnotation<Inject>()) // null
    println(Home::name.getter.findAnnotation<Inject>()) // null
    println(Home::name.setter.findAnnotation<Inject>()) // null
    println(Home::name.findAnnotation<PropAnn>()) // @PropAnn
}