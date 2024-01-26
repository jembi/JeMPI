package org.jembi.jempi.em.kafka


import org.jembi.jempi.em.MU

import scala.collection.immutable.ArraySeq

case class CustomMU(
    tag: String,
    givenName: Probability,
    familyName: Probability,
    gender: Probability,
    dob: Probability,
    city: Probability,
    phoneNumber: Probability,
    nationalId: Probability
)

object CustomMU {

    def fromArraySeq(tag: String, muSeq: ArraySeq[MU]): CustomMU =
        CustomMU(
            tag,
            Probability(muSeq.apply(0).m, muSeq.apply(0).u),
            Probability(muSeq.apply(1).m, muSeq.apply(1).u),
            Probability(muSeq.apply(2).m, muSeq.apply(2).u),
            Probability(muSeq.apply(3).m, muSeq.apply(3).u),
            Probability(muSeq.apply(4).m, muSeq.apply(4).u),
            Probability(muSeq.apply(5).m, muSeq.apply(5).u),
            Probability(muSeq.apply(6).m, muSeq.apply(6).u)
        )

}

