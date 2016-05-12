package com.datasift.dropwizard.scala

import javax.ws.rs.client.Client

import com.datasift.dropwizard.scala.test.{ApplicationTest, BeforeAndAfterAllMulti}
import io.dropwizard.util.Duration
import org.scalatest.FlatSpec

import com.datasift.dropwizard.scala.validation.constraints._
import io.dropwizard.setup.Environment
import io.dropwizard.Configuration

import com.google.common.io.Resources

import javax.ws.rs._
import javax.ws.rs.core.MediaType

import scala.util.{Try, Success}

case class ScalaTestConfiguration(
  @NotEmpty greeting: Option[String] = None,
  @NotEmpty @Size(max = 5) names: List[String] = Nil
) extends Configuration

@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/") class ScalaTestResource(greeting: String, names: List[String]) {

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

  private def greetNames(names: Iterable[String]): List[String] =
    names.map(greeting.format(_)).toList

}

class ScalaTestApp extends ScalaApplication[ScalaTestConfiguration] {
  def run(configuration: ScalaTestConfiguration, environment: Environment) {
    environment.jersey().register(new ScalaTestResource(configuration.greeting.get, configuration.names))
  }
}

class ScalaApplicationSpecIT extends FlatSpec with BeforeAndAfterAllMulti {

  val fixture = "Homer" :: "Bart" :: "Lisa" :: Nil

  val app =
    ApplicationTest[ScalaTestConfiguration, ScalaTestApp](
      this, Resources.getResource("test-conf.yml").getPath) {
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
}
