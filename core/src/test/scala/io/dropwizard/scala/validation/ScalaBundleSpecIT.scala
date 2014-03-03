package io.dropwizard.scala.validation

import org.specs2.mutable._

import io.dropwizard.scala.ScalaBundle
import io.dropwizard.scala.validation.constraints._
import io.dropwizard.{Configuration, Application}
import io.dropwizard.setup.{Environment, Bootstrap}
import io.dropwizard.lifecycle.ServerLifecycleListener
import com.google.common.io.Resources
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.jetty.server.Server
import io.dropwizard.cli.ServerCommand
import net.sourceforge.argparse4j.inf.Namespace
import com.google.common.collect.ImmutableMap
import javax.ws.rs.{Path, GET}

case class ScalaTestConfiguration(
  @JsonProperty("greeting") @NotEmpty greeting: Option[String] = None,
  @JsonProperty("names") @NotEmpty @Size(max = 5) names: List[String] = Nil
) extends Configuration

@Path("/") class ScalaTestResource(greeting: String, names: List[String]) {
  @GET def greet: List[String] = names.map(greeting.format(_))
}

class ScalaTestApp extends Application[ScalaTestConfiguration] {
  def initialize(bootstrap: Bootstrap[ScalaTestConfiguration]) {
    bootstrap.addBundle(new ScalaBundle)
  }

  def run(configuration: ScalaTestConfiguration, environment: Environment) {
    environment.jersey().register(new ScalaTestResource(configuration.greeting.get, configuration.names))
  }
}

class ScalaBundleSpecIT extends Specification {

  val testConfigPath = Resources.getResource("test-conf.yml").getPath

  "ScalaTestApp" in {
    var jettyServer: Option[Server] = None
    val app = new ScalaTestApp
    val bootstrap = new Bootstrap[ScalaTestConfiguration](app) {
      override def run(configuration: ScalaTestConfiguration, env: Environment) {
        env.lifecycle().addServerLifecycleListener(new ServerLifecycleListener {
          def serverStarted(server: Server) {
            jettyServer = Option(server)
          }
        })
      }
    }
    app.initialize(bootstrap)
    val command = new ServerCommand[ScalaTestConfiguration](app)
    val namespace = new Namespace(ImmutableMap.of("file", testConfigPath))
    command.run(bootstrap, namespace)

    "application starts successfully" in {
      jettyServer must beSome.like {
        case server => try {
          var i = 0
          while (i < 5 && !server.isRunning) { Thread.sleep(1000); i += 1 }
          server.isRunning
        } finally {
          server.stop()
        }
      }
    }
  }
}
