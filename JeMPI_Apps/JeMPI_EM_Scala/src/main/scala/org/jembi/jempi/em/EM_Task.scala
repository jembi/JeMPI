package org.jembi.jempi.em

import com.typesafe.scalalogging.LazyLogging
import org.jembi.jempi.em.CustomFields.FIELDS
import org.jembi.jempi.em.Utils._

import java.lang.Math.log
import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq
import scala.collection.parallel.immutable.ParVector
import scala.util.Random

object EM_Task extends LazyLogging {

  def run(
      xxxCols: ArraySeq[Int],
      interactions: ParVector[ArraySeq[String]]
  ): ArraySeq[MU] = {

    val (gamma, ms2) = Profile.profile(
      Gamma.getGamma(
        xxxCols,
        Map[String, Long](),
        interactions.head,
        interactions.tail
      )
    )
    logger.info(s"$ms2 ms")

    if (LOCK_U) {
      @tailrec
      def randomlyChooseIndexes(
          size: Int,
          soFar: Set[Int],
          remaining: Int
      ): Set[Int] = {
        if (remaining == 0) soFar
        else {
          val nextValue = Random.nextInt(size)
          if (soFar.contains(nextValue)) {
            randomlyChooseIndexes(size, soFar, remaining)
          } else {
            randomlyChooseIndexes(size, soFar + nextValue, remaining - 1)
          }
        }
      }

      val randIndexes = randomlyChooseIndexes(
        interactions.size,
        Set[Int](),
        Math.min(20_000, (interactions.size * 2) / 4)
      )
      val randInteractions: ParVector[ArraySeq[String]] = new ParVector(
        randIndexes.map(idx => interactions(idx)).toVector
      )
      val (tallies2, ms1) = Profile.profile(
        scan(xxxCols, isPairMatch2(0.92), randInteractions)
      )
      val lockedU = computeMU(tallies2)
      FIELDS.zipWithIndex.foreach(x =>
        printTalliesAndMU(
          x._1.name,
          tallies2.colTally(x._2),
          lockedU(x._2)
        )
      )
      logger.info(s"$ms1 ms")
      runEM(xxxCols, 0, lockedU.map(x => MU(0.8, x.u)), gamma)
    } else {
      runEM(
        xxxCols,
        0,
        for { _ <- FIELDS } yield MU(m = 0.8, u = 0.0001),
        gamma
      )
    }
  }

  @tailrec
  private def runEM(
      xxxCols: ArraySeq[Int],
      iterations: Int,
      currentMU: ArraySeq[MU],
      gamma: Map[String, Long]
  ): ArraySeq[MU] = {

    case class GammaMetrics(
        matches: Array[Int],
        count: Long,
        weight: Double,
        odds: Double,
        probability: Double,
        tallies: Tallies
    ) {}

    def computeGammaMetrics(matches: Array[Int], count: Long): GammaMetrics = {
      val w = matches.zipWithIndex
        .map(matchResult => {
          val m = currentMU.apply(matchResult._2).m
          val u = currentMU.apply(matchResult._2).u
          matchResult._1 match {
            case GAMMA_TAG_NOT_EQUAL => log((1.0 - m) / (1.0 - u)) / LOG_BASE
            case GAMMA_TAG_EQUAL     => log(m / u) / LOG_BASE
            case _                   => 0.0
          }
        })
        .fold(LOG_LAMBDA)(_ + _)
      val odds = Math.pow(BASE, w)
      val probability = Math.max(1e-10, odds / (1.0 + odds))
      val tallies: Tallies = Tallies(
        ArraySeq.unsafeWrapArray(
          matches.zipWithIndex.map(m =>
            m._1 match {
              case GAMMA_TAG_NOT_EQUAL =>
                Tally(b = probability * count, d = (1.0 - probability) * count)
              case GAMMA_TAG_EQUAL =>
                Tally(a = probability * count, c = (1.0 - probability) * count)
              case _ => Tally()
            }
          )
        )
      )
      GammaMetrics(matches, count, w, odds, probability, tallies)
    }

    def matchAsInts(x: String): Array[Int] = {
      x.slice(1, x.length - 1)
        .split(',')
        .map(y =>
          y.trim() match {
            case GAMMA_TAG_EQUAL_STR     => GAMMA_TAG_EQUAL
            case GAMMA_TAG_NOT_EQUAL_STR => GAMMA_TAG_NOT_EQUAL
            case GAMMA_TAG_MISSING_STR   => GAMMA_TAG_MISSING
          }
        )
    }

    logger.info(s"iteration: $iterations")
    if (iterations >= MAX_EM_ITERATIONS) {
      currentMU
    } else {
      if (iterations == 2) {
        logger.info("break")
      }
      val gamma_ =
        gamma.toVector
          .map(x => x._1 -> (matchAsInts(x._1), x._2))
          .toMap
      val mapGammaMetrics =
        gamma_.map(x => x._1 -> computeGammaMetrics(x._2._1, x._2._2))
      val tallies = mapGammaMetrics.values
        .map(x => x.tallies)
        .fold(new Tallies(xxxCols.length))((x, y) => addTallies(x, y))
      val newMU = computeMU(tallies)
      for (i <- xxxCols.indices) {
        printTalliesAndMU(
          FIELDS.apply(xxxCols.apply(i)).name,
          tallies.colTally(i),
          newMU(i)
        )
      }
      if (LOCK_U) {
        runEM(xxxCols, iterations + 1, mergeMU(newMU, currentMU), gamma)
      } else {
        runEM(xxxCols, iterations + 1, newMU, gamma)
      }
    }
  }

  private def scan(
      xxxCols: ArraySeq[Int],
      isMatch: (ArraySeq[String], ArraySeq[String]) => ContributionSplit,
      interactions: ParVector[ArraySeq[String]]
  ): Tallies = {

    def tallyFieldsContribution(
        left: ArraySeq[String],
        right: ArraySeq[String]
    ): Tallies = {

      def tallyFieldContribution(split: ContributionSplit, col: Int): Tally = {
        if (left.apply(col).isEmpty || right.apply(col).isEmpty) {
          Tally(b = split.matched, d = split.unmatched)
        } else {
          val score = Jaro.jaro(left.apply(col), right.apply(col))
          if (score > JARO_THRESHOLD)
            Tally(a = split.matched, c = split.unmatched)
          else
            Tally(b = split.matched, d = split.unmatched)
        }
      }

      val split = isMatch(left, right)
      Tallies(
        FIELDS.map(field => tallyFieldContribution(split, field.csvCol))
      )
    }

    @tailrec
    def outerLoop(
        acc: Tallies,
        left: ArraySeq[String],
        right: ParVector[ArraySeq[String]]
    ): Tallies = {

      def innerLoop(
          left: ArraySeq[String],
          interactions: ParVector[ArraySeq[String]]
      ): Tallies = {
        interactions
          .map(right => tallyFieldsContribution(left, right))
          .fold(new Tallies(xxxCols.length)) { (x, y) => addTallies(x, y) }
      }

      if (right.isEmpty) {
        acc
      } else {
        outerLoop(
          addTallies(acc, innerLoop(left, right)),
          right.head,
          right.tail
        )
      }

    }

    outerLoop(new Tallies(xxxCols.length), interactions.head, interactions.tail)
  }

}
