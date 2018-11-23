package com.datasift.dropwizard.scala.jdbi.tweak

import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{eq => equalTo}
import com.datasift.dropwizard.scala.jdbi.`package`.JDBIWrapper
import org.skife.jdbi.v2.TransactionIsolationLevel
import org.skife.jdbi.v2.TransactionStatus
import org.skife.jdbi.v2.Handle

class JDBIWrapperSpec extends FlatSpec with MockitoSugar {

  val factory = new BigDecimalArgumentFactory

  "JDBIWrapper.inTransactionWithIsolation" should
    "be able to use isolation levels" in {

    // This is ambiguous:
    // mock[JDBIWrapper].inTransaction(TransactionIsolationLevel.SERIALIZABLE) {
    //   (h: Handle, status: TransactionStatus) =>
    //     assert(true)
    // }

    mock[JDBIWrapper].inTransactionWithIsolation(TransactionIsolationLevel.SERIALIZABLE) {
      (h: Handle, status: TransactionStatus) =>
        assert(true)
    }
  }
}
