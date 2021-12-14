package test1

import love.forte.annotationtool.core.AnnotationMetadata
import love.forte.annotationtool.core.AnnotationTools
import love.forte.di.core.coreBeanClassRegistrar
import love.forte.di.core.coreBeanManager
import love.forte.di.core.internal.AnnotationGetter
import test1.AnnoGetter.toAnnotatedElement
import kotlin.reflect.*
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod


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
    val tool = AnnotationTools.getAnnotationTool()

    private fun <A : Annotation> KAnnotatedElement.toAnnotatedElement(annotationType: KClass<A>): A? {
        val at = annotationType.java
        return when (this) {
            is KClass<*> -> tool.getAnnotation(java, at)
            is KFunction<*> -> javaConstructor?.let { jc ->
                tool.getAnnotation(jc, at)
            } ?: javaMethod?.let { jm -> tool.getAnnotation(jm, at) }
            is KProperty<*> -> javaGetter?.let { jGetter ->
                tool.getAnnotation(jGetter, at)
            } ?: javaField?.let { jField -> tool.getAnnotation(jField, at) }
            is KParameter -> null // TODO

            else -> throw IllegalStateException("Not support annotated element: $this")
        }
    }

    override fun <A : Annotation, R : Any> getAnnotationProperty(
        element: KAnnotatedElement,
        annotationType: KClass<A>,
        name: String,
        propertyType: KClass<R>
    ): R? {
        val metadata = AnnotationMetadata.resolve(annotationType.java)
        val annotation = element.toAnnotatedElement(annotationType) ?: return null
        val value = metadata.getProperties(annotation)[name] ?: return null
        return propertyType.cast(value)
    }

    override fun <T : Annotation> containsAnnotation(
        element: KAnnotatedElement,
        annotationType: KClass<T>
    ): Boolean {

        return element.toAnnotatedElement(annotationType) != null
    }
}