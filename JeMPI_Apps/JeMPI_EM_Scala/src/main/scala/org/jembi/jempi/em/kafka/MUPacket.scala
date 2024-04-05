package org.jembi.jempi.em.kafka

import org.jembi.jempi.em.Probability

import scala.collection.immutable.ArraySeq

case class MUPacket(
    tag: String,
    linkMuPacket: ArraySeq[Probability],
    validateMuPacket: ArraySeq[Probability],
    matchMuPacket: ArraySeq[Probability]
)

object CustomMU {

  def fromArraySeq(
      tag: String,
      muSeqLink: ArraySeq[Probability],
      muSeqValidate: ArraySeq[Probability],
      muSeqMatch: ArraySeq[Probability]
  ): MUPacket =
    MUPacket(
      tag,
      muSeqLink,
      muSeqValidate,
      muSeqMatch
    )

}
