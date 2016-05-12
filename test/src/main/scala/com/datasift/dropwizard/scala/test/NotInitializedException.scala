package com.datasift.dropwizard.scala.test

import scala.util.control.NoStackTrace

case object NotInitializedException extends Exception with NoStackTrace
