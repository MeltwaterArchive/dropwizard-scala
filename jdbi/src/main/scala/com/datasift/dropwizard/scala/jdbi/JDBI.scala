package com.datasift.dropwizard.scala.jdbi

import io.dropwizard.db.DataSourceFactory
import io.dropwizard.setup.Environment
import io.dropwizard.jdbi.DBIFactory
import org.skife.jdbi.v2._

import com.datasift.dropwizard.scala.jdbi.tweak.{BigDecimalArgumentFactory, OptionContainerFactory, OptionArgumentFactory, IterableContainerFactory}

/** Factory object for [[org.skife.jdbi.v2.DBI]] instances. */
object JDBI {

  /** Creates a [[org.skife.jdbi.v2.DBI]] from the given configuration.
    *
    * The name of this instance will be the JDBC URL of the database.
    *
    * @param env environment to manage the database connection lifecycle.
    * @param conf configuration for the database connection.
    * @return a configured and managed [[org.skife.jdbi.v2.DBI]] instance.
    */
  def apply(env: Environment, conf: DataSourceFactory): DBI = {
    apply(env, conf, conf.getUrl)
  }

  /** Creates a [[org.skife.jdbi.v2.DBI]] from the given configuration.
    *
    * @param env environment to manage the database connection lifecycle.
    * @param conf configuration for the database connection.
    * @param name the name of this DBI instance.
    * @return a configured and managed [[org.skife.jdbi.v2.DBI]] instance.
    */
  def apply(env: Environment, conf: DataSourceFactory, name: String): DBI = {
    val dbi = new DBIFactory().build(env, conf, name)

    // register scala type factories
    dbi.registerArgumentFactory(new BigDecimalArgumentFactory)
    dbi.registerArgumentFactory(new OptionArgumentFactory(conf.getDriverClass))
    dbi.registerContainerFactory(new OptionContainerFactory)
    dbi.registerContainerFactory(new IterableContainerFactory[scala.collection.Seq])
    dbi.registerContainerFactory(new IterableContainerFactory[scala.collection.Set])

    dbi
  }
}
