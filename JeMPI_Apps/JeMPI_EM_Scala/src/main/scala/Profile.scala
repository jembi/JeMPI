object Profile {

  def profile[R](code: => R, t: Long = System.nanoTime) = (code, ((System.nanoTime - t) / 1000) / 1000.0)

}
