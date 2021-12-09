
## forte-DI-api

基于并应用 JSR 330 标准中的注解来实现基础依赖注入功能。



| 注解 | 应用点 |  含义  |
|---|---|---|
|① @Inject|属性(或对应setter)|根据类型为属性注入|
|② @Inject + @Named|属性|根据名称为属性注入|
|③ @Inject(可省略) + @Named|有返回值有参数的函数|根据类型为函数参数注入，并将返回值纳入容器。函数所在类必须存在 @Named 注解。|
|④ @Inject|类构造|指定此构造为实例化函数并根据类型注入构造参数，可在参数中配合@Named。构造唯一时可省略。|
|⑤ @Named|类|将此类纳入容器。|
|⑥ @Named|有返回值无参数的函数|将返回值纳入容器。函数所在类必须存在 @Named 注解。|
|⑦ @Named|函数/构造参数|根据名称为参数注入。此函数必须标记 @Inject |
|@Singleton| - |无效 - 所有依赖在容器中仅存唯一实例|
|@Scope| - |无效 - 不提供作用域功能|




### 简单示例：
①

Java
```java
public class Foo {
    @Inject
    private Bar bar;
    
    private User user;
    
    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
```

Kotlin
```kotlin
class Foo {
    @Inject // inject by setter
    lateinit var bar: Bar
}
```

②
Java
```java
public class Foo {
    @Named("bar")
    @Inject
    private Bar bar;
    
    private User user;
    
    @Inject
    public void setUser(@Named("user") User user) {
        this.user = user;
    }
}
```

Kotlin
```kotlin
class Foo {
    @Named("bar")
    @Inject // inject by setter
    lateinit var bar: Bar
}
```
③ 

Java
```java
@Named("foo")
public class Foo {
    
    @Named // default bean name is 'bar'
    @Inject
    public Bar getBar(User user) {
        return ...
    }
    
    @Named("helloWorld")
    public Hello hello(User user) {
        return ...
    }
}    

```

Kotlin
```kotlin
@Named
class Foo {
    @Named // default name is 'bar'
    @Inject // can be omitted
    fun getBar(user: User): Bar = ...
    
    @Named("helloWorld")
    fun hello(user: User): Hello = ...
}    

```
④ 
```java
@Named
public class Foo {
    public Foo() {}
    
    @Inject
    public Foo(User user) {}
}
```

```kotlin
@Named
class Foo constructor() {
    @Inject
    constructor(user: User): this()
    
}
```
⑤ 
```java
@Named // default name is 'Foo'
public class Foo {
    
}

/// 

@Named("myBar")
public class Bar {
    
}

```
```kotlin
@Named // default name is 'Foo'
class Foo

@Named("myBar")
class Bar

```
⑥ 
```java
@Named
public class Foo {
    
    @Named
    public Bar bar() {
        return ...
    }
    
}
```
```kotlin
@Named
class Foo {
    @Named
    fun bar(): Bar = ...
    
}
```
⑦ 
```java
@Named
public class Foo {
    @Inject // // can be omitted if constructor only 1
    public Foo(@Named("bar") Bar bar) {
        // ...
    } 
    
}
```
```kotlin
@Named
class Foo(@Named("myBar") bar: Bar)
```