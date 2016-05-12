package com.datasift.dropwizard.scala.test

import javax.ws.rs.client.{WebTarget, Client}

import io.dropwizard.cli.ServerCommand
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.lifecycle.ServerLifecycleListener
import io.dropwizard.setup.{Bootstrap, Environment}
import io.dropwizard.{Application, Configuration}
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.server.Server

import scala.util.{Failure, Try}
import scala.collection.JavaConverters._

object ApplicationTest {

  def apply[C <: Configuration, A <: Application[C]]
           (suite: BeforeAndAfterAllMulti,
            configPath: String,
            args: Map[String, AnyRef] = Map.empty)
           (newApp: => A): ApplicationTest[C, A] =
    new ApplicationTest[C, A](suite, configPath, args)(newApp)

}

class ApplicationTest[C <: Configuration, A <: Application[C]]
                     (suite: BeforeAndAfterAllMulti,
                      configPath: String,
                      args: Map[String, AnyRef] = Map.empty)
                     (newApp: => A) {

  private var _configuration: Try[C] = Failure(NotInitializedException)
  private var _application: Try[A] = Failure(NotInitializedException)
  private var _environment: Try[Environment] = Failure(NotInitializedException)
  private var _server: Try[Server] = Failure(NotInitializedException)

  def configuration: Try[C] = _configuration
  def application: Try[A] = _application
  def environment: Try[Environment] = _environment
  def server: Try[Server] = _server

  def newClient(name: String): Try[Client] =
    environment.map(new JerseyClientBuilder(_).build(name))

  suite.beforeAll {
    Try(newApp).foreach { app =>
      _application = Try(app)
      val bootstrap = new Bootstrap[C](app) {
        override def run(configuration: C, environment: Environment): Unit = {
          _environment = Try(environment)
          _configuration = Try(configuration)
          super.run(configuration, environment)
          environment.lifecycle.addServerLifecycleListener(
            new ServerLifecycleListener {
              override def serverStarted(server: Server): Unit = {
                _server = Try(server)
              }
            }
          )
        }
      }

      app.initialize(bootstrap)
      val command = new ServerCommand[C](app)
      val ns = new Namespace(Map[String, AnyRef]("file" -> configPath).asJava)
      command.run(bootstrap, ns)
    }
  }

  suite.afterAll {
    server.foreach(_.stop())
  }
}
