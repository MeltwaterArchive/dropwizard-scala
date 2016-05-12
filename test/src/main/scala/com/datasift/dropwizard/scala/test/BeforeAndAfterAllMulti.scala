package com.datasift.dropwizard.scala.test

import org.scalatest.{Suite, BeforeAndAfterAll}

import collection.mutable

trait BeforeAndAfterAllMulti extends BeforeAndAfterAll { this: Suite =>

  private var before: mutable.Buffer[() => Unit] = mutable.Buffer.empty
  private var after: mutable.Buffer[() => Unit] = mutable.Buffer.empty

  def beforeAll(f: => Unit): Unit = {
    before.append(() => f)
  }

  def afterAll(f: => Unit): Unit = {
    after.prepend(() => f)
  }

  override def beforeAll(): Unit = {
    before.foreach(_())
  }

  override def afterAll(): Unit = {
    after.foreach(_())
  }
}
