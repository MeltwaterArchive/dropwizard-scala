Dropwizard Scala
================

*Scala support for [Dropwizard](http://dropwizard.io).*

Core
----

  * A base `ScalaApplication` trait for applications to be defined as
    a singleton object:

  ```scala
  import io.dropwizard.Configuration
  import io.dropwizard.scala.ScalaApplication
  
  case class MyConfiguration(
    @NotEmpty greeting: String, 
    @NotNull greeters: List[String]
  ) extends Configuration

  object MyApplication extends ScalaApplication[MyConfiguration] {
    def init(bootstrap: Bootstrap[MyConfiguration]) {
      
    }

    def run(conf: MyConfiguration, env: Environment) {

    }
  }
  ```
  
  When you build an application like this, the `ScalaBundle` is automatically
  added, providing everything else described here.

  * Jackson support for Scala collections, `Option` and case classes, 
    enabling (de)serialization of Scala collections/case classes in 
    configurations and within Jersey request/response entities.

Jersey
------

  * Jersey support for Scala collections and `Option` in resource method 
    parameters and for request/response entities.

  * Jersey support for Scala's native `Boolean`, `Int` and `Long` types 
    in resource method parameters via the `BooleanParam`, `IntParam` and 
    `LongParam` wrapper types.

JDBI
----

  * JDBI marshalling of Scala collections, `Option` and case classes, 
    in both method parameters and result types.

  * A more idiomatic JDBI API:

  ```scala
  import io.dropwizard.scala.jdbi._
  
  val db = JDBI(dataSource)
  val dao = db.onDemand[MyDAO]
  val result: Int = db.inTransaction {
    handle: Handle => handle.attach[MyDAO].myQuery(123)
  }
  ```

Validation
----------

  * Support for all JSR-303 and Hibernate Validator constraints on Scala types.
    In particular, support is added for `@NotEmpty` and `@Size` on Scala 
    collections. All other constraint annotations work on Scala types out of 
    the box.

  * Validation of Scala `case class` properties using JSR-303 and Hibernate 
    Validator constraints. To validate a `case class`, you will need to use the
    wrapper constraints defined in `io.dropwizard.scala.validation.constraints`:
    
  ```scala
  import io.dropwizard.scala.validation.constraints._
  
  case class MyConfiguration(
    @NotEmpty names: List[String], 
    @Min(0) age: Int
  ) extends Configuration
  ```

### Limitations

In order to cascade validation using `@Valid` on collection types, Hibernate 
requires that the collection provide a Java `Iterator`. Since Scala collections
don't provide this, they cannot cascade validation.

In the following example, only `MyConfiguration` is validated. `Person` values
held in the `people` collection are not validated, though the size of `people` 
is.

```scala
case class MyConfiguration(@Valid @NotEmpty people: List[Person]) 
  extends Configuration

case class Person(@NotEmpty name: String, @Min(0) age: Int)
```

