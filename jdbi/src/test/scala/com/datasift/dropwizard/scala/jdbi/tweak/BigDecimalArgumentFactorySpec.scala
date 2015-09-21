package com.datasift.dropwizard.scala.jdbi

import com.datasift.dropwizard.scala.ApplicationHarness
import com.datasift.dropwizard.scala.ScalaTestApp
import com.google.common.io.Resources
import io.dropwizard.db.DataSourceFactory
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.util.BigDecimalMapper
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

import scala.collection.JavaConversions._


class BigDecimalSpec extends FlatSpec 
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  val app = ApplicationHarness(new ScalaTestApp, Resources.getResource("test-conf.yml").getPath)

  private var dbi: DBI = _
  private var handle: Handle = _

  override def beforeAll() { 
    val hsqlConfig = new DataSourceFactory()
    hsqlConfig.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis())
    hsqlConfig.setUser("testuser")
    hsqlConfig.setDriverClass("org.h2.Driver")
    hsqlConfig.setValidationQuery("SELECT 1")

    dbi = JDBI(app.environment.toOption.get, hsqlConfig, "hsql")
    handle = dbi.open()
  }

  override def beforeEach() { 
    handle.createCall("DROP TABLE bdtest IF EXISTS").invoke()
    handle.createCall("CREATE TABLE bdtest (id varchar(100) primary key, value decimal(15,5))").invoke()
  }

  override def afterAll() { handle.close(); app.shutdown() }

  "Scala's BigDecimal" should "work when bound to a SQL query" in {
    handle.createStatement("INSERT INTO bdtest VALUES (?, ?)")
          .bind(0, "first")
          .bind(1, BigDecimal("2.00012"))
          .execute()

    val values = handle.createQuery("SELECT value FROM bdtest WHERE id = ?").bind(0, "first").map(BigDecimalMapper.FIRST)
    assert(values.list.head == BigDecimal("2.00012").bigDecimal)
  }
}
