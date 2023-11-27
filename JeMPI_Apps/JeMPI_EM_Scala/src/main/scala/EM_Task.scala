import Fields.FIELDS
import Jaro.jaro
import Profile.profile
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.collection.parallel.immutable.ParVector

object EM_Task extends LazyLogging {

  private val BASE: Double = 2.0
  private val LOG_BASE: Double = Math.log(BASE)
  private val LAMBDA: Double = 1E-9
  private val LOG_LAMBDA: Double = Math.log(LAMBDA / (1.0 - LAMBDA)) / LOG_BASE
  private val JARO_THRESHOLD: Double = 0.92
  private val JARO_THRESHOLD_EM: Double = 0.98
  private val N_ITERATIONS = 20
  private val COL_REC_NUM = 0
  private val MIN_M = 0.4

  def run(interactions: ParVector[Array[String]], useRecursion: Boolean): Unit = {

    logger.info("BASE:              {}", BASE)
    logger.info("LOG_BASE:          {}", LOG_BASE)
    logger.info("LAMBDA:            {}", LAMBDA)
    logger.info("LOG_LAMBDA:        {}", LOG_LAMBDA)
    logger.info("JARO_THRESHOLD:    {}", JARO_THRESHOLD)
    logger.info("JARO_THRESHOLD_EM: {}", JARO_THRESHOLD_EM)
    logger.info("N_ITERATIONS:      {}", N_ITERATIONS)
    logger.info("COL_REC_NUM:       {}", COL_REC_NUM)

    logger.info("Compute perfect knowledge")
    val (tallies1, ms1) = profile(outerScan(isPairMatch1, interactions, useRecursion))
    val muList1 = computeMU(tallies1)
    FIELDS.zipWithIndex.foreach(x => printTalliesAndMU(x._1.name, tallies1.colTally(x._2), muList1(x._2)))
    logger.info(s"$ms1 ms")

    logger.info("Compute fields > 4")
    val (tallies2, ms2) = profile(outerScan(isPairMatch2(JARO_THRESHOLD), interactions, useRecursion))
    val muList2 = computeMU(tallies2)
    FIELDS.zipWithIndex.foreach(x => printTalliesAndMU(x._1.name, tallies2.colTally(x._2), muList2(x._2)))
    logger.info(s"$ms2 ms")

    val emMuList = nextIter(N_ITERATIONS, muList1, interactions, muList2.map(mu => if (mu.m > MIN_M) mu else MU(MIN_M, mu.u)), useRecursion)
    FIELDS.zipWithIndex.foreach(x => printMU(x._1.name, emMuList.apply(x._2), muList1.apply(x._2)))
    logger.debug("done")

  }

  @tailrec
  private def nextIter(n: Int, muList: Array[MU], interactions: ParVector[Array[String]], lockedU: Array[MU], useRecursion: Boolean): Array[MU] = {
    if (n == 0)
      muList
    else {
      logger.info(s"Compute using MU for $n")
      val (tallies, ms) = Profile.profile(outerScan(isPairMatch3(muList, JARO_THRESHOLD_EM),
                                                    interactions,
                                                    useRecursion))
      val muListImproved: Array[MU] = mergeMU(computeMU(tallies), lockedU).map(mu => if (mu.m > MIN_M) mu else MU(MIN_M, mu.u))
      Fields.FIELDS.zipWithIndex.foreach(x => printTalliesAndMU(x._1.name,
                                                                tallies.colTally(x._2),
                                                                muListImproved(x._2)))
      logger.info(s"$ms ms")
      nextIter(n - 1, muListImproved, interactions, lockedU, useRecursion)
    }
  }

  private def mergeMU(mSource: Array[MU], uSource: Array[MU]): Array[MU] = {
    mSource.zipWithIndex.map(x => MU(mSource.apply(x._2).m, uSource.apply(x._2).u))
  }

  private def computeMU(tallies: Tallies): Array[MU] = {
    tallies.colTally.map(tally => MU(tally.a / (tally.a + tally.b), tally.c / (tally.c + tally.d)))
  }

  private def printTalliesAndMU(label: String, tally: Tally, mu: MU): Unit = {
    logger.info(f"$label%-15s ${tally.a}%15.1f ${tally.b}%15.1f ${tally.c}%15.1f ${tally.d}%15.1f ->  ${mu.m}%9.7f, ${mu.u}%9.7f")
  }

  private def printMU(label: String, mu1: MU, mu2: MU): Unit = {
    val w1 = Math.log(mu1.m / mu1.u) / LOG_BASE
    val w2 = Math.log(mu2.m / mu2.u) / LOG_BASE
    logger.info(f"$label%-15s  ->  ${mu1.m}%9.7f, ${mu2.u}%9.7f    $w1%12.6f  $w2%12.6f       ${(w1 / w2) * 100.0}%9.3f%%")
  }

  private def isPairMatch1(left: Array[String], right: Array[String]): (Double, Double) = {
    if (left.apply(COL_REC_NUM).regionMatches(true, 4, right.apply(COL_REC_NUM), 4, 10))
      (1.0, 0.0)
    else
      (0.0, 1.0)
  }

  private def isPairMatch2(fieldThreshold: Double)(left: Array[String], right: Array[String]): (Double, Double) = {
    if (Array.range(1, left.length).map(idx => if (jaro(left.apply(idx), right.apply(idx)) > fieldThreshold) 1 else 0).sum >= 4)
      (1.0, 0.0)
    else
      (0.0, 1.0)
  }

  private def isPairMatch3(muList: Array[MU], fieldThreshold: Double)(left: Array[String], right: Array[String]): (Double, Double) = {

    def getPartialWeight(idx: Int): Double = {

      def getMU: (Double, Double) = {
        val m = muList.apply(idx).m
        val u = muList.apply(idx).u
        if (jaro(left.apply(idx+1), right.apply(idx+1)) > fieldThreshold)
          (m, u)
        else
          (1.0 - m, 1.0 - u)
      }

      val (m, u) = getMU
      Math.log(m / u) / LOG_BASE
    }

    val omega = LOG_LAMBDA + Array.range(0, muList.length)
                                  .map(idx => getPartialWeight(idx))
                                  .sum
    val odds = Math.pow(BASE, omega) // anti log
    val probability = odds / (1.0 + odds)
    (probability, 1.0 - probability)
  }

  private def outerScan(isMatch: (Array[String], Array[String]) => (Double, Double),
                        interactions: ParVector[Array[String]],
                        useRecursion: Boolean): Tallies = {

    def addTally(x: Tally, y: Tally): Tally = {
      Tally(x.a + y.a, x.b + y.b, x.c + y.c, x.d + y.d)
    }

    def addTallies(x: Tallies, y: Tallies): Tallies = {
      Tallies(Array.range(0, x.colTally.length).map(idx => addTally(x.colTally(idx), y.colTally(idx))))
    }

    def fieldsTallyContribution(left: Array[String], right: Array[String]): Tallies = {

      def fieldTallyContribution(matches: (Double, Double), col: Int): Tally = {
        if (left.apply(col).isEmpty || right.apply(col).isEmpty) {
          // TODO: CHECK IF THIS LOGIC IS CORRECT

          //  Tally(a=matches._1, c=matches._2)
          Tally(b = matches._1, d = matches._2)
          //  Tally(a = matches._1 / 2.0, b = matches._1 / 2.0, c = matches._2 / 2.0, d = matches._2 / 2.0)
        } else {
          val score = jaro(left.apply(col), right.apply(col))
          if (score > JARO_THRESHOLD)
            Tally(a = matches._1, c = matches._2)
          else
            Tally(b = matches._1, d = matches._2)
        }
      }

      val matches = isMatch(left, right)
      Tallies(FIELDS.map(field => fieldTallyContribution(matches, field.csvCol)))
    }

    def outerLoop1(interactions: ParVector[Array[String]]): Tallies = {

      def innerLoop1(left: Array[String]): Tallies = {
        val recNumber = left.apply(COL_REC_NUM)
        interactions
          .filter(right => recNumber.compareTo(right.apply(COL_REC_NUM)) < 0) // exclude (A,A) and (B,A)  ie only (A,B)
          .map(right => fieldsTallyContribution(left, right)) // Tallies for L,R pair
          .fold(Tallies()) { (x, y) => addTallies(x, y) } // sum of Tallies
      }

      interactions
        .map(left => innerLoop1(left))
        .fold(new Tallies) { (x, y) => addTallies(x, y) }
    }

    @tailrec
    def outerLoop2(acc: Tallies, left: Array[String], right: ParVector[Array[String]]): Tallies = {

      def innerLoop2(left: Array[String], interactions: ParVector[Array[String]]): Tallies = {

        interactions
          .map(right => fieldsTallyContribution(left, right))
          .fold(Tallies()) { (x, y) => addTallies(x, y) }
      }

      if (right.isEmpty) {
        acc
      } else {
        outerLoop2(addTallies(acc, innerLoop2(left, right)), right.head, right.tail)
      }

    }

    if (useRecursion) {
      // no filter required but cannot parallelize outer loop
      outerLoop2(new Tallies, interactions.head, interactions.tail)
    } else {
      // requires a filter but outer loop parallelized so still faster than outerLoop2
      outerLoop1(interactions)
    }

  }

  private case class Tally(a: Double = 0.0, b: Double = 0.0, c: Double = 0.0, d: Double = 0.0)

  private case class Tallies(colTally: Array[Tally] = FIELDS.map(_ => Tally()))

}
