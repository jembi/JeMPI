
import Fields.FIELDS

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParVector
import scala.language.reflectiveCalls

object ReadCSV {

  def readCsv(csvFileName: String): ParVector[Array[String]] = {

    def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B = {
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
        if (cols.length == FIELDS.length)
          rows += cols :+ ""
        else
          rows += cols
      }
    }
    new ParVector(rows.toVector)
  }

}
