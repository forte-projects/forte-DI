package test1

import love.forte.annotationtool.core.KAnnotationTool
import love.forte.di.core.coreBeanClassRegistrar
import love.forte.di.core.coreBeanManager
import love.forte.di.core.internal.AnnotationGetter
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.cast


fun main() {
    val manager = coreBeanManager { }


    val getter = AnnoGetter

    val registrar = coreBeanClassRegistrar(getter)

    registrar.register(
        MyController::class,
        MyServiceImpl::class,
        Foo::class,
        Bar.BarB::class,
        Bar.BarA::class,
    ).inject(manager)

    val controller = manager[MyController::class]
    println(controller)
    controller.value()

}


private object AnnoGetter : AnnotationGetter {
    val tool = KAnnotationTool()

    override fun <R : Any> getAnnotationProperty(
        element: KAnnotatedElement,
        annotationType: KClass<out Annotation>,
        name: String,
        propertyType: KClass<R>
    ): R? {
        val annotation = tool.getAnnotation(element, annotationType) ?: return null
        return tool.getAnnotationValues(annotation)[name]?.let { propertyType.cast(it) }
    }

    override fun <R : Any> getAnnotationsProperties(
        element: KAnnotatedElement,
        annotationType: KClass<out Annotation>,
        name: String,
        propertyType: KClass<R>
    ): List<R> {
        return tool.getAnnotations(element, annotationType).mapNotNull { a ->
            tool.getAnnotationValues(a)[name]?.let { propertyType.cast(it) }
        }
    }

    override fun <T : Annotation> containsAnnotation(element: KAnnotatedElement, annotationType: KClass<T>): Boolean {
        return tool.getAnnotation(element, annotationType) != null
    }
}