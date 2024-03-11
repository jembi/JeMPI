package org.jembi.jempi.em.kafka


import org.jembi.jempi.em.MU

import scala.collection.immutable.ArraySeq

case class CustomMU(
  tag: String,
  customLinkMU: CustomLinkMU,
  customValidateMU: CustomValidateMU,
  customMatchMU: CustomMatchMU
)

case class CustomLinkMU(
  givenName: Probability,
  familyName: Probability,
  gender: Probability,
  dob: Probability,
  city: Probability,
  phoneNumber: Probability,
  nationalId: Probability
)

case class CustomValidateMU(
  
)

case class CustomMatchMU(
  
)

object CustomMU {

  def fromArraySeq(tag: String, muSeqLink: ArraySeq[MU], muSeqValidate: ArraySeq[MU], muSeqMatch: ArraySeq[MU]): CustomMU =
    CustomMU(
      tag,
      CustomLinkMU(
        Probability(muSeqLink.apply(0).m, muSeqLink.apply(0).u),
        Probability(muSeqLink.apply(1).m, muSeqLink.apply(1).u),
        Probability(muSeqLink.apply(2).m, muSeqLink.apply(2).u),
        Probability(muSeqLink.apply(3).m, muSeqLink.apply(3).u),
        Probability(muSeqLink.apply(4).m, muSeqLink.apply(4).u),
        Probability(muSeqLink.apply(5).m, muSeqLink.apply(5).u),
        Probability(muSeqLink.apply(6).m, muSeqLink.apply(6).u)
      ),
      CustomValidateMU(
        
      ),
      CustomMatchMU(
        
      )
    )

}

