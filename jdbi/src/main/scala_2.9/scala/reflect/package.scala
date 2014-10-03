package scala

package object reflect {

  def classTag[A : Manifest] = manifest[A]

  type Class[A] = java.lang.Class[A]
  type ClassTag[A] = Manifest[A]

  implicit final def manifestCompat[A](m: Manifest[A]) = new {
    def runtimeClass = m.erasure
  }
}

