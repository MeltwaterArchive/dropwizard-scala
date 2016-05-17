package com.datasift.dropwizard.scala

import java.io.File
import javax.ws.rs.client.Entity

import com.codahale.metrics.MetricRegistry
import com.datasift.dropwizard.scala.jdbi.JDBI
import com.datasift.dropwizard.scala.test.{LiquibaseTest, MySQLTest, ApplicationTest, BeforeAndAfterAllMulti}
import io.dropwizard.db.DataSourceFactory
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap
import org.scalatest.FlatSpec

import com.datasift.dropwizard.scala.validation.constraints._
import io.dropwizard.setup.Environment
import io.dropwizard.Configuration

import com.google.common.io.Resources

import javax.ws.rs._
import javax.ws.rs.core.{Form, MediaType}

import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean
import org.skife.jdbi.v2.sqlobject.{BindBean, SqlQuery, SqlUpdate, Bind}

import scala.beans.BeanProperty
import scala.util.{Try, Success}

case class ScalaTestConfiguration(
  @NotEmpty greeting: Option[String] = None,
  @NotEmpty @Size(max = 5) names: List[String] = Nil,
  @NotNull @Valid db: DataSourceFactory
) extends Configuration

@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/") class ScalaTestResource(db: TestDAO, greeting: String, names: List[String]) {

  @GET def greet = greetWithList(names)

  @GET @Path("/maybe")
  def greetOrNotFound(@QueryParam("name") name: Option[String]): Option[List[String]] =
    name.map(greeting.format(_)).map(List(_))

  @GET @Path("/option")
  def greetWithOption(@QueryParam("name") name: Option[String]): List[String] =
    name.map(greeting.format(_)).toList

  @GET @Path("/list")
  def greetWithList(@QueryParam("names") names: List[String]): List[String] =
    greetNames(names)

  @GET @Path("/set")
  def greetWithSet(@QueryParam("names") names: Set[String]): List[String] =
    greetNames(names)

  @GET @Path("/vector")
  def greetWithVector(@QueryParam("names") names: Vector[String]): List[String] =
    greetNames(names)

  @GET @Path("/seq")
  def greetWithSeq(@QueryParam("names") names: Seq[String]): List[String] =
    greetNames(names)

  @GET @Path("/complex")
  def complexQuery(@QueryParam("names") names: Set[java.math.BigDecimal]): Option[Int] =
    names.headOption.map(_ multiply new java.math.BigDecimal(2)).map(_.intValue)

  @GET @Path("/complex_scala")
  def complexQueryScala(@QueryParam("names") names: Set[BigDecimal]): Option[Int] =
    names.headOption.map(_ * BigDecimal(2)).map(_.toInt)

  @GET @Path("/bigint")
  def bigint(@QueryParam("int") int: BigInt): Int = int.intValue

  @GET @Path("/either")
  def either(@QueryParam("name") name: Either[String, Integer]): Either[Throwable, String] = {
    name match {
      case Left(v) => Right(greeting.format(v))
      case Right(v) => Left(new Exception("Int"))
    }
  }

  @GET @Path("/try")
  def tryA(@QueryParam("name") name: Option[String]): Try[String] = Try {
    greeting.format(name.get)
  }

  @POST @Path("/db/separate") @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  def insertSeparate(@FormParam("decimal") decimal: BigDecimal,
                     @FormParam("option") option: Option[String]): Int = {
    db.insert(decimal, option)
  }

  @POST @Path("/db/row")
  def insertRow(row: Row): Int = {
    db.insert(row)
  }

  @GET @Path("/db/row")
  def getRow: Option[Row] = {
    db.get()
  }

  private def greetNames(names: Iterable[String]): List[String] =
    names.map(greeting.format(_)).toList

}

class ScalaTestApp extends ScalaApplication[ScalaTestConfiguration] {
  import jdbi._
  def run(configuration: ScalaTestConfiguration, environment: Environment) {
    val dao = JDBI(environment, configuration.db, "test").daoFor[TestDAO]
    environment.jersey
      .register(new ScalaTestResource(dao, configuration.greeting.get, configuration.names))
  }
}

trait TestDAO {

  @SqlUpdate("INSERT INTO tbl (d, o) VALUES (:d, :o)")
  def insert(@Bind("d") x: BigDecimal,
             @Bind("o") y: Option[String]): Int

  @SqlUpdate("INSERT INTO tbl (d, o) VALUES (:row.d, :row.o)")
  def insert(@BindBean("row") row: Row): Int

  @SingleValueResult
  @SqlQuery("SELECT d, o FROM tbl")
  def get(): Option[Row]

  @SqlQuery("select d from tbl")
  def debug(): String
}

case class Row(@BeanProperty d: BigDecimal,
               @BeanProperty o: Option[String])

class ScalaApplicationSpecIT extends FlatSpec with BeforeAndAfterAllMulti {

  val fixture = "Homer" :: "Bart" :: "Lisa" :: Nil
  val dsFactory = new DataSourceFactory()
  dsFactory.setUrl("jdbc:mysql:mxj://localhost:3309/test?server.basedir=./target/mysql&createDatabaseIfNotExist=true&server.initialize-user=true")
  dsFactory.setDriverClass("com.mysql.jdbc.Driver")
  dsFactory.setUser("root")

  val db = MySQLTest(this, dsFactory.getUrl) {
    dsFactory.build(new MetricRegistry, "test")
  }

  val liquibase = LiquibaseTest(this, LiquibaseTest.Config(file = new File(Resources.getResource("migrations.xml").toURI).getAbsolutePath)) {
    dsFactory.build(new MetricRegistry, "migrations")
  }

  val app =
    ApplicationTest[ScalaTestConfiguration, ScalaTestApp](
      this, new File(Resources.getResource("test-conf.yml").toURI).getAbsolutePath) {
        new ScalaTestApp
      }

  lazy val client = app.newClient("test")

  def request() = for {
    client <- client
    server <- app.server
  } yield { client.target(server.getURI) }

  def request(target: String) = for {
    client <- client
    server <- app.server
  } yield { client.target(server.getURI.resolve(target)) }

  "GET /" should "greet with configured names" in {
    val expected = app.configuration
      .map(conf => conf.names.map(conf.greeting.getOrElse("%s").format(_)))
    val result = request().map(_.request().get(classOf[List[String]]))
    assert(result === expected)
  }

  "GET /list" should "not greet anyone when no names supplied" in {
    val expected = Success(List.empty[String])
    val result = request("/list").map {
      _.queryParam("names")
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[List[String]])
    }
    assert(result === expected)
  }

  it should "greet with supplied names" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)))
    val result = request("/list").map {
      _.queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[List[String]])
    }
    assert(result === expected)
  }

  "GET /seq" should "greet with supplied names" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toSeq)
    val result = request("/seq").map {
      _.queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Seq[String]])
    }
    assert(result === expected)
  }

  "GET /vector" should "greet with supplied names" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toVector)
    val result = request("/vector").map {
      _.queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Vector[String]])
    }
    assert(result === expected)
  }

  "GET /set" should "greet with supplied names" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toSet)
    val result = request("/set").map {
      _.queryParam("names", (fixture ++ fixture): _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Set[String]])
    }
    assert(result === expected)
  }

  "GET /option" should "greet with supplied name" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).headOption.toIterable)
    val result = request("/option").map {
      _.queryParam("name", fixture.head)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  it should "not greet when no name supplied" in {
    val expected = Success(Iterable.empty[String])
    val result = request("/option").map {
      _.queryParam("name", null)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  "GET /maybe" should "greet with supplied name" in {
    val expected = app.configuration
      .map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).headOption.toIterable)
    val result = request("/maybe").map {
      _.queryParam("name", fixture.head)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  it should "present Not Found error when no name supplied" in {
    val result = request("/maybe").map {
      _.queryParam("name", null)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result.isFailure)
    assert(result.recover { case t: NotFoundException => Nil}.isSuccess)
  }

  "GET /complex" should "yield results" in {
    val fixture: Set[java.math.BigDecimal] =
      Set(new java.math.BigDecimal(1), new java.math.BigDecimal(2))
    val expected = 2
    val result = request("/complex").map {
      _.queryParam("names", fixture.toSeq: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === Success(expected))
  }

  "GET /complex_scala" should "yield results" in {
    val fixture: Set[BigDecimal] = Set(BigDecimal(1), BigDecimal(2))
    val expected = 2
    val result = request("/complex_scala").map {
      _.queryParam("names", fixture.toSeq: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === Success(expected))
  }

  "GET /either" should "produce failure" in {
    val result = request("/either").map {
      _.queryParam("name", 2.toString)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result.isFailure)
  }

  it should "yield result" in {
    val expected = app.configuration
      .map(_.greeting.getOrElse("%s").format(fixture.head))
    val result = request("/either").map {
      _.queryParam("name", fixture.head)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result === expected)
  }

  "GET /bigint" should "yield the number" in {
    val fixture = BigInt(500)
    val expected = Success(500)
    val result = request("/bigint").map {
      _.queryParam("int", fixture.toString)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === expected)
  }

  "GET /try" should "yield the result on success" in {
    val expected = app.configuration
      .map(_.greeting.getOrElse("%s").format(fixture.head))
    val result = request("/try").map {
      _.queryParam("name", fixture.head)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result === expected)
  }

  it should "yield an error, on error" in {
    val result = request("/try").map {
      _.request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[InternalServerErrorException])
  }

  "POST /db/separate" should "write data" in {
    val form = new Form()
      .param("decimal", BigDecimal(12345.678).toString)
      .param("option", "Nick")
    val result = request("/db/separate").map {
      _.request(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.form(form), classOf[Int])
    }
    assert(result === Success(1))
  }

  "POST /db/row" should "write whole row" in {
    val result = request("/db/row").map {
      _.request(MediaType.APPLICATION_JSON)
        .post(Entity.json(Row(BigDecimal(12345.678), Option("Nick"))), classOf[Int])
    }

    assert(result === Success(1))
  }

  "GET /db/row" should "get whole row" in {
    val result = request("/db/row").map {
      _.request().get(classOf[Row])
    }

    assert(result === Success(Row(BigDecimal(12345.678), Option("Nick"))))
  }
}
