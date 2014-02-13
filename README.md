Dropwizard Scala
================

*Scala support for [Dropwizard](http://dropwizard.io).*

Core
----

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
