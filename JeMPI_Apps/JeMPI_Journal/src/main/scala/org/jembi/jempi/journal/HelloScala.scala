package org.jembi.jempi.journal

import org.apache.logging.log4j.{LogManager, Logger}

class HelloScala {

  private val LOGGER: Logger = LogManager.getLogger(classOf[HelloScala])

  def hello(): String = {
    LOGGER.debug("{}", "HELLO FROM SCALA")
    "HelloScala(class)"
  }

}

object HelloScala {

  def hallo(): String = {
    "HalloScala(object)"
  }

  def hello(): String = {
    "HelloScala(object)"
  }

}