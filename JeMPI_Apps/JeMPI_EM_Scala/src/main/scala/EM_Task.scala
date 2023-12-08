import Fields.FIELDS
import Jaro.jaro
import Profile.profile
import Utils._
import com.typesafe.scalalogging.LazyLogging

import java.lang.Math.log
import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq
import scala.collection.parallel.immutable.ParVector

object EM_Task extends LazyLogging {

  def run(interactions: ParVector[ArraySeq[String]]): ArraySeq[MU] = {

    val interactions_ = interactions.map(i => i.tail)

    //    @tailrec
    //    def randomlyChooseIndexes(size: Int, soFar: Set[Int], remaining: Int): Set[Int] = {
    //      if (remaining == 0) soFar
    //      else {
    //        val nextValue = Random.nextInt(size)
    //        if (soFar.contains(nextValue)) {
    //          randomlyChooseIndexes(size, soFar, remaining)
    //        } else {
    //          randomlyChooseIndexes(size, soFar + nextValue, remaining - 1)
    //        }
    //      }
    //    }
    //
    //    val randIndexes = randomlyChooseIndexes(interactions_.size, Set[Int](), (interactions_.size * 2) / 4)
    //    val randInteractions: ParVector[ArraySeq[String]] = new ParVector(randIndexes.map(idx => interactions_(idx))
    //                                                                                 .toVector)
    //    val (tallies2, ms1) = profile(scan(Utils.isPairMatch2(0.92), randInteractions))
    //    val lockedU = computeMU(tallies2)
    //    FIELDS.zipWithIndex.foreach(x => Utils.printTalliesAndMU(x._1.name, tallies2.colTally(x._2), lockedU(x._2)))
    //    logger.info(s"$ms1 ms")

    val (gamma, ms2) = profile(
      Gamma.getGamma2(
        Map[String, Long](),
        interactions_.head,
        interactions_.tail
      )
    )
    logger.info(s"$ms2 ms")
    val initialMU = for {
      _ <- FIELDS
    } yield MU(m = 0.8, u = 0.0001)
    runEM(0, initialMU, gamma)
  }

  @tailrec
  private def runEM(
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
        gamma.toVector.map(x => x._1 -> (matchAsInts(x._1), x._2)).toMap
      val mapGammaMetrics =
        gamma_.map(x => x._1 -> computeGammaMetrics(x._2._1, x._2._2))
      val tallies = mapGammaMetrics.values
        .map(x => x.tallies)
        .fold(Tallies())((x, y) => addTallies(x, y))
      val newMU = computeMU(tallies)
      FIELDS.zipWithIndex.foreach(x =>
        printTalliesAndMU(x._1.name, tallies.colTally(x._2), newMU(x._2))
      )
      runEM(iterations + 1, newMU /*mergeMU(newMU, currentMU)*/, gamma)
    }
  }

  private def scan(
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
          val score = jaro(left.apply(col), right.apply(col))
          if (score > JARO_THRESHOLD)
            Tally(a = split.matched, c = split.unmatched)
          else
            Tally(b = split.matched, d = split.unmatched)
        }
      }

      val split = isMatch(left, right)
      Tallies(
        FIELDS.map(field => tallyFieldContribution(split, field.csvCol - 1))
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
          .fold(Tallies()) { (x, y) => addTallies(x, y) }
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

    outerLoop(new Tallies, interactions.head, interactions.tail)
  }

}
