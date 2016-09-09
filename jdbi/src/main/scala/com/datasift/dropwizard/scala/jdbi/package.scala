package com.datasift.dropwizard.scala

import scala.reflect._

import org.skife.jdbi.v2._
import org.skife.jdbi.v2.sqlobject.mixins.Transactional
import org.skife.jdbi.v2.tweak.HandleCallback

/** Global definitions and implicits for JDBI. */
package object jdbi {

  implicit final def JDBIWrapper(db: DBI) = new JDBIWrapper(db)

  /** Provides idiomatic Scala enhancements to the JDBI API.
    *
    * Examples -
    *
    *   dbi.open[DAO] to open a handle and attach a new sql object of the specified type to that handle
    *
    *   dbi.daoFor[DAO] to create a new sql object which will obtain and release connections from this dbi instance, as it needs to,
    *     and can, respectively
    *
    *   When in scope, you can create transactions using for comprehension. For instance -
    *     {{{
    *      for {
    *       transaction <- dbi
    *       dao <- transaction.dao[MyDao]
    *     } yield {
    *       dao.myFunction(v1, v2)
    *     }
    *     }}}
    *
    * @param db the [[org.skife.jdbi.v2.DBI]] instance to wrap.
    */
  class JDBIWrapper private[jdbi](db: DBI) {

    /** Creates a typed DAO instance.
     *
     * @tparam T type of the DAO to create.
     * @return a DAO instance for the specified type.
     */
    def open[T : ClassTag]: T = db.open[T](classTag[T].runtimeClass.asInstanceOf[Class[T]])

    /** Creates an on-demand typed DAO instance.
      *
      * @tparam T type of the DAO to create.
      * @return an on-demand DAO instance for the specified type.
      */
    def daoFor[T : ClassTag]: T = db.onDemand[T](classTag[T].runtimeClass.asInstanceOf[Class[T]])

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](f: (Handle, TransactionStatus) => A): A = {
      db.inTransaction(new TransactionCallback[A] {
        def inTransaction(handle: Handle, status: TransactionStatus): A = f(handle, status)
      })
    }

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](f: Handle => A): A = {
      db.inTransaction(new TransactionCallback[A] {
        def inTransaction(handle: Handle, status: TransactionStatus): A = f(handle)
      })
    }

    /** Executes the given function within a transaction of the given isolation level.
      *
      * @tparam A the return type of the function to execute.
      * @param isolation the isolation level for the transaction.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](isolation: TransactionIsolationLevel)
                        (f: (Handle, TransactionStatus) => A): A = {
      db.inTransaction(isolation, new TransactionCallback[A] {
        def inTransaction(handle: Handle, status: TransactionStatus): A = f(handle, status)
      })
    }

    /** Executes the given function within a transaction of the given isolation level.
      *
      * @tparam A the return type of the function to execute.
      * @param isolation the isolation level for the transaction.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](isolation: TransactionIsolationLevel)
                        (f: Handle => A): A = {
      db.inTransaction(isolation, new TransactionCallback[A] {
        def inTransaction(handle: Handle, status: TransactionStatus): A = f(handle)
      })
    }

    def map[A](f: Handle => A): A = inTransaction(f)

    def flatMap[A](f: Handle => A): A = map(f)

    def foreach(f: Handle => Unit): Unit = map(f)

    /** Applies the given function with a DBI [[org.skife.jdbi.v2.Handle]].
      *
      * @tparam A the return type of the function to apply.
      * @param f the function to apply the handle to.
      * @return the result of applying the function.
      * @throws Exception if an Exception is thrown by the function.
      */
    def withHandle[A](f: Handle => A): A = {
      db.withHandle(new HandleCallback[A] {
        def withHandle(handle: Handle): A = f(handle)
      })
    }
  }

  implicit final def HandleWrapper(handle: Handle) = new HandleWrapper(handle)

  /** Provides idiomatic Scala enhancements to the JDBI API.
    *
    * @param handle the [[org.skife.jdbi.v2.Handle]] instance to wrap.
    */
  class HandleWrapper private[jdbi] (handle: Handle) {

    /** Creates a typed DAO instance attached to this [[org.skife.jdbi.v2.Handle]].
      *
      * @tparam A type of the DAO to create.
      * @return a DAO instance for the specified type.
      */
    def attach[A : ClassTag]: A = {
      handle.attach(classTag[A].runtimeClass.asInstanceOf[Class[A]])
    }

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](f: Handle => A): A = {
      handle.inTransaction(new TransactionCallback[A] {
        def inTransaction(conn: Handle, status: TransactionStatus): A = f(conn)
      })
    }

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](f: (Handle, TransactionStatus) => A): A = {
      handle.inTransaction(new TransactionCallback[A] {
        def inTransaction(conn: Handle, status: TransactionStatus): A = f(conn, status)
      })
    }

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param isolation the isolation level for the transaction.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](isolation: TransactionIsolationLevel)
                        (f: Handle => A): A = {
      handle.inTransaction(isolation, new TransactionCallback[A] {
        def inTransaction(conn: Handle, status: TransactionStatus): A = f(conn)
      })
    }

    /** Executes the given function within a transaction.
      *
      * @tparam A the return type of the function to execute.
      * @param isolation the isolation level for the transaction.
      * @param f the function to execute within the transaction.
      * @return the result of the function.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[A](isolation: TransactionIsolationLevel)
                        (f: (Handle, TransactionStatus) => A): A = {
      handle.inTransaction(isolation, new TransactionCallback[A] {
        def inTransaction(conn: Handle, status: TransactionStatus): A = f(conn, status)
      })
    }

    def dao[A: ClassTag] =
      new HandleDaoWrapper(handle, classTag[A].runtimeClass.asInstanceOf[Class[A]])
  }

  class HandleDaoWrapper[A] private [jdbi](handle: Handle, clazz: Class[A]) {

    def map[B](f: A => B): B = f(handle.attach(clazz))

    def flatMap[B](f: A => B): B = map(f)

    def foreach(f: A => Unit): Unit = map(f)
  }

  implicit final def TransactionalWrapper[A <: Transactional[A]](transactional : A) =
    new TransactionalWrapper[A](transactional)

  /** Provides enhancements to the Dropwizard jDBI API for transactional DAOs.
    *
    * @param transactional the [[org.skife.jdbi.v2.sqlobject.mixins.Transactional]] object to wrap.
    */
  class TransactionalWrapper[A <: Transactional[A]] private[jdbi] (transactional: A) {

    /** Executes the given function within a transaction of the given isolation level.
      *
      * @tparam B the type of the result of the function being executed.
      * @param isolation the isolation level for the transaction.
      * @param f the function on this object to execute within the transaction.
      * @return the result of the function being executed.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[B](isolation: TransactionIsolationLevel)
                        (f: A => B): B = {
      transactional.inTransaction[B](isolation, new Transaction[B, A] {
        def inTransaction(tx: A, status: TransactionStatus): B = f(tx)
      })
    }

    /** Executes the given function within a transaction of the given isolation level.
      *
      * @tparam B the type of the result of the function being executed.
      * @param isolation the isolation level for the transaction.
      * @param f the function on this object to execute within the transaction.
      * @return the result of the function being executed.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[B](isolation: TransactionIsolationLevel)
                        (f: (A, TransactionStatus) => B): B = {
      transactional.inTransaction[B](isolation, new Transaction[B, A] {
        def inTransaction(tx: A, status: TransactionStatus): B = f(tx, status)
      })
    }

    /** Executes the given function within a transaction.
      *
      * @tparam B the type of the result of the function being executed.
      * @param f the function on this object to execute within the transaction.
      * @return the result of the function being executed.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[B](f: A => B): B = {
      transactional.inTransaction[B](new Transaction[B, A] {
        def inTransaction(tx: A, status: TransactionStatus): B = f(tx)
      })
    }


    /** Executes the given function within a transaction.
      *
      * @tparam B the type of the result of the function being executed.
      * @param f the function on this object to execute within the transaction.
      * @return the result of the function being executed.
      * @throws Exception if an Exception is thrown by the function, the transaction will be
      *                   rolled-back.
      */
    def inTransaction[B](f: (A, TransactionStatus) => B): B = {
      transactional.inTransaction[B](new Transaction[B, A] {
        def inTransaction(tx: A, status: TransactionStatus): B = f(tx, status)
      })
    }
  }
}
