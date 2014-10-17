package scala.util

import scala.util.control.ControlThrowable

sealed abstract class Try[+A] {

  def map[B](f: A => B): Try[B]
  def flatMap[B](f: A => Try[B]): Try[B]
  def isSuccess: Boolean
  def isFailure: Boolean
  def recover[B >: A](pf: PartialFunction[Throwable, B]): Try[B]
}

object Try {

  def apply[A](r: => A): Try[A] = try Success(r) catch {
    case NonFatal(t) => Failure(t)
  }
}

object NonFatal {

  def apply(t: Throwable): Boolean = t match {
    case _: StackOverflowError => true
    case _: VirtualMachineError |
         _: ThreadDeath |
         _: InterruptedException |
         _: LinkageError |
         _: ControlThrowable => false
    case _ => true
  }

  def unapply(t: Throwable): Option[Throwable] = if (apply(t)) Some(t) else None
}

case class Success[+A](value: A) extends Try[A] {
  def map[B](f: A => B): Try[B] = Try(f(value))
  def flatMap[B](f: A => Try[B]): Try[B] =
    try f(value)
    catch {
      case NonFatal(e) => Failure(e)
    }
  def isSuccess = true
  def isFailure = false
  def recover[B >: A](pf: PartialFunction[Throwable, B]): Try[B] = this
}
case class Failure[+A](t: Throwable) extends Try[A] {
  def map[B](f: A => B): Try[B] = this.asInstanceOf[Try[B]]
  def flatMap[B](f: A => Try[B]): Try[B] = this.asInstanceOf[Try[B]]
  def isSuccess = false
  def isFailure = true
  def recover[B >: A](pf: PartialFunction[Throwable, B]): Try[B] =
    try {
      if (pf.isDefinedAt(t)) Try(pf(t)) else this
    } catch {
      case NonFatal(e) => Failure(e)
    }
}
