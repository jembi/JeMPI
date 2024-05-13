package org.jembi.jempi.em

import com.typesafe.scalalogging.LazyLogging
import Jaro.jaro

import scala.collection.immutable.ArraySeq

object Utils extends LazyLogging {

  val BASE: Double = 2.0
  val LOG_BASE: Double = Math.log(BASE)
  val LAMBDA: Double = 1.0 / 2_000_000.0
  val LOG_LAMBDA: Double = Math.log(LAMBDA / (1.0 - LAMBDA)) / LOG_BASE
  val JARO_THRESHOLD: Double = 0.92
  val MAX_EM_ITERATIONS = 100
  val GAMMA_TAG_MISSING: Int = 0
  val GAMMA_TAG_NOT_EQUAL: Int = 1
  val GAMMA_TAG_EQUAL: Int = 2
  val GAMMA_TAG_MISSING_STR: String = GAMMA_TAG_MISSING.toString
  val GAMMA_TAG_NOT_EQUAL_STR: String = GAMMA_TAG_NOT_EQUAL.toString
  val GAMMA_TAG_EQUAL_STR: String = GAMMA_TAG_EQUAL.toString
  val LOCK_U = false
  private val MIN_U: Double = 1e-10
  private val MAX_M: Double = 1.0 - 1e-10

  def printTalliesAndMU(label: String, tally: Tally, mu: Probability): Unit = {
    logger.info(
      f"$label%-15s ${tally.a}%15.1f ${tally.b}%15.1f ${tally.c}%15.1f ${tally.d}%15.1f ->  ${mu.m}%9.7f, ${mu.u}%9.7f"
    )
  }

  def printMU(label: String, mu: Probability): Unit = {
    logger.info(f"$label%-15s ->  ${mu.m}%9.7f, ${mu.u}%9.7f")
  }

  def mergeMU(mSource: ArraySeq[Probability], uSource: ArraySeq[Probability]): ArraySeq[Probability] = {
    mSource.zipWithIndex.map(x =>
      Probability(mSource.apply(x._2).m, uSource.apply(x._2).u)
    )
  }

  def computeMU(tallies: Tallies): ArraySeq[Probability] = {
    tallies.colTally.map(tally =>
      Probability(
        m = Math.min(tally.a / (tally.a + tally.b), MAX_M),
        u = Math.max(tally.c / (tally.c + tally.d), MIN_U)
      )
    )
  }

  /*
  def isPairMatch1(
      left: ArraySeq[String],
      right: ArraySeq[String]
  ): ContributionSplit = {
    if (
      left
        .apply(Utils.COL_REC_NUM)
        .regionMatches(true, 4, right.apply(Utils.COL_REC_NUM), 4, 10)
    ) {
      ContributionSplit(1.0, 0.0)
    } else {
      ContributionSplit(0.0, 1.0)
    }
  }
  */

  def isPairMatch2(
      fieldThreshold: Double
  )(left: ArraySeq[String], right: ArraySeq[String]): ContributionSplit = {
    if (
      Array
        .range(0, left.length)
        .map(idx =>
          if (jaro(left.apply(idx), right.apply(idx)) > fieldThreshold) 1 else 0
        )
        .sum >= 4
    ) {
      ContributionSplit(1.0, 0.0)
    } else {
      ContributionSplit(0.0, 1.0)
    }
  }

  def isPairMatch3(
                    muSeq: ArraySeq[Probability],
                    fieldThreshold: Double
  )(left: ArraySeq[String], right: ArraySeq[String]): ContributionSplit = {

    val omega = muSeq.zipWithIndex.foldLeft(LOG_LAMBDA)((acc, v) =>
      acc + (if (jaro(left.apply(v._2), right.apply(v._2)) > fieldThreshold) {
               Math.log(v._1.m / v._1.u) / LOG_BASE
             } else {
               Math.log((1.0 - v._1.m) / (1.0 - v._1.u)) / LOG_BASE
             })
    )
    val odds = Math.pow(Utils.BASE, omega) // anti log
    val probability = odds / (1.0 + odds)
    ContributionSplit(probability, 1.0 - probability)
  }

  def addTallies(x: Tallies, y: Tallies): Tallies = {

    def addTally(x: Tally, y: Tally): Tally = {
      Tally(x.a + y.a, x.b + y.b, x.c + y.c, x.d + y.d)
    }

    Tallies(
      ArraySeq
        .range(0, x.colTally.length)
        .map(idx => addTally(x.colTally(idx), y.colTally(idx)))
    )
  }
  def camelCaseToSnakeCase(name: String): String = "[A-Z\\d]".r.replaceAllIn(
    name,
    { m =>
      "_" + m.group(0).toLowerCase()
    }
    )

  def snakeCaseToCamelCase(name: String): String = "_([a-z\\d])".r.replaceAllIn(
    name,
    { m =>
      m.group(1).toUpperCase()
    }
    )



}
