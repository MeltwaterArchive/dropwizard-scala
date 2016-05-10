package com.datasift.dropwizard.scala

import javax.ws.rs.client.Client

import io.dropwizard.util.Duration
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import com.datasift.dropwizard.scala.validation.constraints._
import io.dropwizard.setup.{Environment, Bootstrap}
import io.dropwizard.lifecycle.ServerLifecycleListener
import io.dropwizard.cli.ServerCommand
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.{Configuration, Application}

import com.google.common.io.Resources
import com.google.common.collect.ImmutableMap
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.server.Server

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
  def either(@QueryParam("name") name: Either[String, Integer]): String = {
    name match {
      case Left(v) => greeting.format(v)
      case Right(v) => v.toString
    }
  }

  private def greetNames(names: Iterable[String]): List[String] =
    names.map(greeting.format(_)).toList

}

class ScalaTestApp extends ScalaApplication[ScalaTestConfiguration] {
  def run(configuration: ScalaTestConfiguration, environment: Environment) {
    environment.jersey().register(new ScalaTestResource(configuration.greeting.get, configuration.names))
  }
}

object ApplicationHarness {

  def retry[A](attempts: Int)
              (f: => A): Try[A] = retry(attempts, 0)(_ => f)

  @annotation.tailrec
  private def retry[A](attempts: Int, attempt: Int)
                      (f: (Int) => A): Try[A] = Try(f(attempt)) match {
    case success: Success[A] => success
    case _ if attempts > 1 => retry(attempts - 1, attempt + 1)(f)
    case failure => failure
  }

  def retryWithDelay[A](attempts: Int, delay: Duration)
                       (f: => A): Try[A] = retry(attempts, 0) { attempt =>
    if (attempt > 1) {
      Thread.sleep(delay.toMilliseconds)
    }
    f
  }

  def retryWithDelay[A](attempts: Int, initialDelay: Duration, maxDelay: Duration)
                       (f: => A): Try[A] = retry(attempts, 0) { attempt =>
    if (attempt > 1) {
      Thread.sleep(math.min(math.pow(initialDelay.toMilliseconds, attempt).toLong, maxDelay.toMilliseconds))
    }
    f
  }
}

case class ApplicationHarness[C <: Configuration](app: Application[C], configPath: String) {

  import ApplicationHarness._

  private lazy val application = {
    var jettyServer: Server = null
    var environment: Environment = null
    var configuration: Configuration = null
    val bootstrap = new Bootstrap(app) {
      override def run(conf: C, env: Environment) {
        super.run(conf, env)
        environment = env
        configuration = conf
        env.lifecycle().addServerLifecycleListener(new ServerLifecycleListener {
          def serverStarted(server: Server) {
            jettyServer = server
          }
        })
      }
    }
    app.initialize(bootstrap)
    val command = new ServerCommand[C](app)
    val namespace = new Namespace(ImmutableMap.of("file", configPath))
    command.run(bootstrap, namespace)
    retryWithDelay(5, Duration.seconds(1)) {
      jettyServer match {
        case null => throw new RuntimeException("Jetty Server failed to start.")
        case x if !x.isRunning => throw new RuntimeException("Jetty Server started but is not running.")
        case x => (x, environment, configuration.asInstanceOf[C])
      }
    }
  }

  lazy val client: Try[Client] = environment.map(new JerseyClientBuilder(_).build("client"))

  def server = application.map(_._1)

  def environment = application.map(_._2)

  def configuration = application.map(_._3)

  def request[T](f: Client => T): Try[T] = client.map(f)

  def request[T](f: (Client, Server) => T): Try[T] = for {
    client <- client
    server <- server
  } yield f(client, server)

  def shutdown() {
    server.map(_.stop())
  }
}

class ScalaApplicationSpecIT extends FlatSpec with BeforeAndAfterAll {

  val fixture = Array("Nick", "Chris", "Fiona")

  val app = ApplicationHarness(new ScalaTestApp, Resources.getResource("test-conf.yml").getPath)

  override def beforeAll() { app.server; app.client }

  override def afterAll() { app.shutdown() }

  "GET /" should "greet with configured names" in {
    val expected = app.configuration.map(conf => conf.names.map(conf.greeting.getOrElse("%s").format(_)))
    val result = app.request { (client, server) =>
      client
        .target(server.getURI)
        .request()
        .get(classOf[Array[String]])
        .toList
    }
    assert(result === expected)
  }

  "GET /list" should "not greet anyone when no names supplied" in {
    val expected = Success(List.empty[String])
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/list"))
        .queryParam("names")
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[List[String]])
    }
    result.recover {
      case t => throw new RuntimeException(t)
    }
    assert(result === expected)
  }

  "GET /list" should "greet with supplied names" in {
    val fixture = "Michael" :: "Andrew" :: "Lisa" :: Nil
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)))
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/list"))
        .queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[List[String]])
    }
    assert(result === expected)
  }

  "GET /seq" should "greet with supplied names" in {
    val fixture = List("Michael", "Andrew", "Lisa")
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toSeq)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/seq"))
        .queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Seq[String]])
    }
    assert(result === expected)
  }

  "GET /vector" should "greet with supplied names" in {
    val fixture = List("Michael", "Andrew", "Lisa")
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toVector)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/vector"))
        .queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Vector[String]])
    }
    assert(result === expected)
  }

  "GET /set" should "greet with supplied names" in {
    val fixture = List("Michael", "Andrew", "Lisa", "Lisa")
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toSet)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/set"))
        .queryParam("names", fixture: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Set[String]])
    }
    assert(result === expected)
  }

  "GET /option" should "greet with supplied name" in {
    val fixture = Option("Nick")
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toIterable)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/option"))
        .queryParam("name", fixture.orNull)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  "GET /option with no name" should "not greet" in {
    val fixture: Option[String] = None
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toIterable)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/option"))
        .queryParam("name", fixture.orNull)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  "GET /maybe" should "greet with supplied name" in {
    val fixture = Option("Nick")
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toIterable)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/maybe"))
        .queryParam("name", fixture.orNull)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result === expected)
  }

  "GET /maybe with no name" should "present Not Found error" in {
    val fixture: Option[String] = None
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)).toIterable)
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/maybe"))
        .queryParam("name", fixture.orNull)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Iterable[String]])
    }
    assert(result.isFailure)
    assert(result.recover { case t: NotFoundException => Nil}.isSuccess)
  }

  "GET /complex" should "yield results" in {
    val fixture: Set[java.math.BigDecimal] = Set(new java.math.BigDecimal(1), new java.math.BigDecimal(2))
    val expected = 2
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/complex"))
        .queryParam("names", fixture.toSeq: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === Success(expected))
  }

  "GET /complex_scala" should "yield results" in {
    val fixture: Set[BigDecimal] = Set(BigDecimal(1), BigDecimal(2))
    val expected = 2
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/complex_scala"))
        .queryParam("names", fixture.toSeq: _*)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === Success(expected))
  }

  "GET /either" should "yield right side" in {
    val fixture = 2
    val expected = "2"
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/either"))
        .queryParam("name", fixture.toString)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result === Success(expected))
  }

  "GET /either" should "yield left side" in {
    val fixture = "Nick"
    val expected = app.configuration.map(_.greeting.getOrElse("%s").format(fixture))
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/either"))
        .queryParam("name", fixture)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[String])
    }
    assert(result === expected)
  }

  "GET /bigint" should "yield the number" in {
    val fixture = BigInt(500)
    val expected = 500
    val result = app.request { (client, server) =>
      client
        .target(server.getURI.resolve("/bigint"))
        .queryParam("int", fixture.toString)
        .request(MediaType.APPLICATION_JSON)
        .get(classOf[Int])
    }
    assert(result === Success(expected))
  }
}
