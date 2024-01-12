package org.jembi.jempi.em

object Profile {

  def profile[R](code: => R, t: Long = System.nanoTime): (R, Double) =
    (code, ((System.nanoTime - t) / 1000) / 1000.0)

}
