package net.nicktelford.dropwizard.scala

import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import net.nicktelford.dropwizard.scala.validation.constraints._
import io.dropwizard.setup.{Environment, Bootstrap}
import io.dropwizard.lifecycle.ServerLifecycleListener
import io.dropwizard.cli.ServerCommand
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.{Configuration, Application}

import com.google.common.io.Resources
import com.google.common.collect.ImmutableMap
import com.sun.jersey.api.client.Client
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.server.Server

import javax.ws.rs._
import javax.ws.rs.core.MediaType

import scala.concurrent.duration._
import scala.util.{Try, Success}

case class ScalaTestConfiguration(
  @NotEmpty greeting: Option[String] = None,
  @NotEmpty @Size(max = 5) names: List[String] = Nil
) extends Configuration

@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/") class ScalaTestResource(greeting: String, names: List[String]) {

  @GET def greet: List[String] = greetWithNames(names)

  @POST def greetWithNames(names: List[String]) = names.map(greeting.format(_))
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
      Thread.sleep(delay.toMillis)
    }
    f
  }

  def retryWithDelay[A](attempts: Int, initialDelay: Duration, maxDelay: Duration)
                       (f: => A): Try[A] = retry(attempts, 0) { attempt =>
    if (attempt > 1) {
      Thread.sleep(math.min(math.pow(initialDelay.toMillis, attempt).toLong, maxDelay.toMillis))
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
    retryWithDelay(5, Duration(1, SECONDS)) {
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
    val result = app.request { (client, server) => client.resource(server.getURI).get(classOf[Array[String]]).toList }
    assert(result.isSuccess)
    assert(result === expected)
  }

  "POST /" should "not greet anyone when no names supplied" in {
    val expected = Success(List.empty[String])
    val result = app.request { (client, server) => client.resource(server.getURI).`type`(MediaType.APPLICATION_JSON).post(classOf[Array[String]], List.empty[String]).toList }
    result.recover {
      case t => throw new RuntimeException(t)
    }
    assert(result === expected)
  }

  it should "greet with supplied names" in {
    val fixture = "Michael" :: "Andrew" :: "Lisa" :: Nil
    val expected = app.configuration.map(conf => fixture.map(conf.greeting.getOrElse("%s").format(_)))
    val result = app.request { (client, server) => client.resource(server.getURI).`type`(MediaType.APPLICATION_JSON).post(classOf[Array[String]], fixture).toList }
    assert(result.isSuccess)
    assert(result === expected)
  }

}
