Dropwizard Scala
================

See the [Convey README](./README_Convey.md) for Convey-specific build and deployment instructions.

*Scala support for [Dropwizard](http://dropwizard.io).*

Usage
-----

Just add a dependency to `dropwizard-scala-core` and `dropwizard-scala-jdbi` _(optional)_ to your project:

### SBT

```scala
libraryDependencies += "com.datasift.dropwizard.scala" %% "dropwizard-scala-core" % "1.0.0-1"
```

### Maven

Include the `dropwizard-scala-core` artifact in your POM:

```xml
<dependency>
    <groupId>com.datasift.dropwizard.scala</groupId>
    <artifactId>dropwizard-scala-core_2.10.2</artifactId>
    <version>1.0.0-1</version>
</dependency>
```

It's good practice to keep your Scala version as a global property that you
can use elsewhere to ensure coherence in your POM:

```xml
<properties>
    <scala.version>2.10.2</scala.version>
    <dropwizard.version>1.0.0</dropwizard.version>
    <dropwizard.scala.version>${dropwizard.version}-1</dropwizard.scala.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.datasift.dropwizard.scala</groupId>
        <artifactId>dropwizard-scala-core_${scala.version}</artifactId>
        <version>${dropwizard.scala.version}</version>
    </dependency>
</dependencies>
``` 

Core
----

  * A base `ScalaApplication` trait for applications to be defined as
    a singleton object:

  ```scala
  import io.dropwizard.Configuration
  import com.datasift.dropwizard.scala.ScalaApplication
  
  class MyConfiguration extends Configuration {
    @NotEmpty val greeting: String = "Hello, %s!"
    @NotNull val greeters: List[String] = Nil
  }

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

  * `log4s` is provided automatically, via a transitive dependency. To use it,
    simply `import org.log4s._`. See http://github.com/log4s/log4s for more
    details.

Metrics
-------

  * A more idiomatic API for metrics is provided by `com.datasift.dropwizard.scala.metrics._`.
  
```scala
import com.codahale.metrics._
import com.datasift.dropwizard.scala.metrics._

class MyApplication extends ScalaApplication[MyConfiguration] {
  def run (conf: MyConfiguration, env: Environment) {
    env.metrics.gauge("things.current_time") {
      System.currentTimeMillis()
    }
    
    env.metrics.timer("things.some_timer") {
      // do something and time the execution
    }
  }
}

```

Jersey
------

  * Support for `Option` in resource method parameters and for request/response
    entities.

  * Support for `Either[L, R]` in resource method parameters, where `L` and `R`
    are both types Jersey supports for parameters. By convention, it will
    attempt to decode the parameter first in to the right-side as an `R`, and if
    that fails, in to the left-side as an `L`.

  * Support for `Seq[A]`, `List[A]`, `Vector[A]`, `IndexedSeq[A]` and `Set[A]`
    in resource method parameters, where `A` is any non-collection type that
    Jersey supports for parameters. This is the same limitation imposed on Java
    collections.

  * Support for `BigInt` and `BigDecimal` in resource method parameters and
    request/response entities.

  * Support for Scala's native `Boolean`, `Int` and `Long` types in resource
    method parameters via the `BooleanParam`, `IntParam` and `LongParam` wrapper
    types.

JDBI
----

  * Scala collections and `Option` as the return type for a result set (i.e. 
    multiple rows of results).

    Note: when returning a single row as an `Option`, you must use the
    `@SingleValueResult` annotation:

    ```scala
    @SqlQuery("select i from tbl limit 1")
    @SingleValueResult
    def headOption: Option[Int]
    ```

  * Support for the `BigDecimal` and `Option` types as parameters and result 
    column types.

  * Support for returning a row as a case class or tuple, with the following
    constraints:

      * selected columns must match up with constructor paramaters
        _positionally_.
      * only the first defined public constructor will be used if multiple
        constructors are defined.
      * paramater types must be directly mappable from their SQL types,
        without the use of a mapper. The only exceptions to this rule are
        `Option` and `scala.BigDecimal`, which are natively supported.

  * case classes and tuples as parameters using the `BindProduct` annotation:
    
    ```scala
    @SqlUpdate("insert into tbl (a, b, c, d) values (:x.a, :x.b, :y._1, :y._2)")
    def insert(@BindProduct("x") x: Thing, @BindProduct("y") y: (Int, String))
    ```

    Note: `BindProduct` will bind to any no-args method or field (prioritizing
    no-arg methods).

  * A more idiomatic JDBI API:

    ```scala
    import com.datasift.dropwizard.scala.jdbi._
    
    val db = JDBI(dataSource)
    val dao = db.onDemand[MyDAO]
    val result: Int = db.inTransaction {
      handle: Handle => handle.attach[MyDAO].myQuery(123)
    }
    ```

To enable Scala integration for JDBI, you will need to add an extra dependency:

### SBT

```scala
libraryDependencies += "com.datasift.dropwizard.scala" %% "dropwizard-scala-jdbi" % "1.0.0-1"
```

### Maven

```xml
<dependency>
    <groupId>com.datasift.dropwizard.scala</groupId>
    <artifactId>dropwizard-scala-jdbi_${scala.version}</artifactId>
    <version>${dropwizard.scala.version}</version>
</dependency>
```

Validation
----------

  * Support for all JSR-303 and Hibernate Validator constraints on Scala types.
    In particular, support is added for `@NotEmpty` and `@Size` on Scala 
    collections. All other constraint annotations work on Scala types out of 
    the box.

  * Validation of Scala `case class` properties using JSR-303 and Hibernate 
    Validator constraints. To validate a `case class`, you will need to use the
    wrapper constraints defined in `com.datasift.dropwizard.scala.validation.constraints`:
    
  ```scala
  import com.datasift.dropwizard.scala.validation.constraints._
  
  class MyConfiguration extends Configuration {
    @NotEmpty val names: List[String] = Nil
    @Min(0) val age: Int = 20
  }
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

Test
----

This module provides some utilities for aiding testing with ScalaTest.
Note: this module is by far the least mature, and the API of its components is
subject to change. Comments, ideas and suggestions welcome.

See `core/src/test/**/ScalaApplicationSpecIT` for examples of all of these 
components in action.

  * `BeforeAndAfterMulti` - a utility trait that allows multiple functions to 
    be registered to run `before` and `after` tests, executing the `after`
    functions in the reverse order to their associated `before` functions.
    This behaves similarly to Dropwizard's lifecycle management, except it's 
    managing the lifecycle of test dependencies.

    All of the `*Test` utilities below require that your test class extend this
    trait.

  * `ApplicationTest` - runs tests in the context of a running Dropwizard 
    Application:

    ```scala
    val app =
      ApplicationTest(this, configFilePath) {
        MyApplication
      }
    ```

    The returned object contains the following utility methods to work with the
    application:

    * `configuration: Try[C]` - the application's configuration.
    * `application: Try[A]` - the application object itself.
    * `environment: Try[Environment]` - the appliction's `Environment`.
    * `server: Try[Server]` - the application's Jetty `Server`.
    * `newClient(name: String): Try[Client]` - a helper to construct a Jersey
      `Client` that connects to the application.

  * `MySQLTest` - runs tests in the context of a running MySQL server:

    ```scala
    val mysql = MySQLTest(this, dataSourceFactory.getUrl) {
      dataSourceFactory.build(new MetricRegistry, "test")
    }
    ```

    The returned object contains the following utility methods to work with the
    MySQL server:

    * `dataSource: Try[ManagedDataSource]` - the `DataSource` used to create 
      the database instance.
    * `baseDir: Try[File]` - the base directory for the MySQL server's data.

    Note: to use this object, you will need to add a dependency on
    `mysql:mysql-connector-mxj:5.0.12`.

  * `LiquibaseTest` - runs tests in the context of a database migration:

    ```scala
    val migrations = LiquibaseTest(
      this, LiquibaseTest.Config(migrationsFilePath)) {
        dataSourceFactory.build(new MetricRegistry, "migrations")
      }
    ```

    The returned object contains the following utility methods to work with the
    Liquibase context:

    * `dataSource: Try[ManagedDataSource]` - the `DataSource` used to connect 
      to the database instance.
    * `liquibase: Try[CloseableLiquibase]` - the Liquibase context that ran the
      migrations.

