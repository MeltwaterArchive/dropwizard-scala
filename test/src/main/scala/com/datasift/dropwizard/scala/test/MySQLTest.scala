package com.datasift.dropwizard.scala.test

import java.io.{IOException, File}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, SimpleFileVisitor, Path, Files}

import com.mysql.management.driverlaunched.ServerLauncherSocketFactory
import io.dropwizard.db.ManagedDataSource

import scala.util.{Failure, Try}

object MySQLTest {

  private val UriRegex =
    "^jdbc:mysql:mxj://[^:]+:\\d+/[^?]+?.*server\\.basedir=([^&]+).*$".r

  def apply(suite: BeforeAndAfterAllMulti, connectionURI: => String)
           (newDataSource: => ManagedDataSource): MySQLTest =
    new MySQLTest(suite, connectionURI)(newDataSource)
}

class MySQLTest(suite: BeforeAndAfterAllMulti, connectionURI: => String)
               (newDataSource: => ManagedDataSource) {

  import MySQLTest.UriRegex

  var _dataSource: Try[ManagedDataSource] = Failure(NotInitializedException)
  var _baseDir: Try[File] = Failure(NotInitializedException)

  def dataSource: Try[ManagedDataSource] = _dataSource
  def baseDir: Try[File] = baseDir

  suite.beforeAll {
    _dataSource = Try(newDataSource)
    _baseDir = Try(connectionURI match {
      case UriRegex(dir) => new File(dir)
    })
    _dataSource.foreach(_.getConnection)
  }

  suite.afterAll {
    _baseDir.foreach { baseDir =>
      if (!ServerLauncherSocketFactory.shutdown(baseDir, null)) {
        deleteRecursively(baseDir.toPath)
      }
    }
  }

  private def deleteRecursively(path: Path) {
    Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path,
                             attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path,
                                      ex: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }
}
