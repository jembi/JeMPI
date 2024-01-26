package org.jembi.jempi.em.kafka

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
