package com.datasift.dropwizard.scala.test

import java.util.Date

import io.dropwizard.db.ManagedDataSource
import io.dropwizard.migrations.CloseableLiquibase

import scala.util.{Failure, Try}

object LiquibaseTest {

  case class Config(file: String = "migrations.xml",
                    contexts: Seq[String] = Seq.empty)
}

class LiquibaseTest(suite: BeforeAndAfterAllMulti,
                    config: LiquibaseTest.Config)
                   (newDataSource: => ManagedDataSource) {

  private var _dataSource: Try[ManagedDataSource] =
    Failure(NotInitializedException)
  private var _liquibase: Try[CloseableLiquibase] =
    Failure(NotInitializedException)

  def dataSource: Try[ManagedDataSource] = _dataSource
  def liquibase: Try[CloseableLiquibase] = _liquibase

  suite.beforeAll {
    _dataSource = Try(newDataSource)
    _liquibase = _dataSource
      .flatMap(ds => Try(new CloseableLiquibase(ds, config.file)))

    _liquibase.foreach(_.update(config.contexts.mkString(",")))
  }

  suite.afterAll {
    _liquibase.foreach { liquibase =>
      liquibase.rollback(new Date(0), config.contexts.mkString(","))
      liquibase.close()
    }
  }

}
