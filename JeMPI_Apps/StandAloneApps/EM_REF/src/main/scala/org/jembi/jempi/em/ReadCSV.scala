package org.jembi.jempi.em

import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParVector

object ReadCSV extends LazyLogging {

  def readCsv(fields: ArraySeq[Field], csvFileName: String): ParVector[Array[String]] = {

    def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = {
      try {
        f(resource)
      } finally {
        resource.close()
      }
    }

    val rows = ArrayBuffer[Array[String]]()
    using(io.Source.fromFile(csvFileName)) { source =>
      for (line <- source.getLines().drop(1)) {
        val cols = line.split(",").map(_.trim)
//        logger.info(f"${cols.length}%d ${FIELDS.length}%d")
        if (cols.length == fields.length)
          rows += cols.tail :+ ""
        else if (cols.length == fields.length - 1)
          rows += cols.tail :+ "" :+ ""
        else {
          rows += cols.tail
        }
      }
    }
    new ParVector(rows.toVector)
  }

}
