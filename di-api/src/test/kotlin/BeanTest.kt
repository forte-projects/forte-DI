import javax.inject.Inject
import javax.inject.Named


@Named
class MyBeanA


@Named("bar")
class MyBeanB @Inject constructor(@Named val b1: MyBeanA)