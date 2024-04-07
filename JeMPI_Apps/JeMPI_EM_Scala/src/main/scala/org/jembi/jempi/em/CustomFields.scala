package org.jembi.jempi.em

import scala.collection.immutable.ArraySeq

object CustomFields {

  private val COL_GIVEN_NAME = 0
  private val COL_FAMILY_NAME = 1
  private val COL_GENDER = 2
  private val COL_DOB = 3
  private val COL_CITY = 4
  private val COL_PHONE_NUMBER = 5
  private val COL_NATIONAL_ID = 6

  val FIELDS: ArraySeq[Field] = ArraySeq(
    Field("givenName", COL_GIVEN_NAME),
    Field("familyName", COL_FAMILY_NAME),
    Field("gender", COL_GENDER),
    Field("dob", COL_DOB),
    Field("city", COL_CITY),
    Field("phoneNumber", COL_PHONE_NUMBER),
    Field("nationalId", COL_NATIONAL_ID)
  )

  val LINK_COLS: ArraySeq[Int] = ArraySeq(

  )

  val VALIDATE_COLS: ArraySeq[Int] = ArraySeq(
    COL_GIVEN_NAME,
    COL_FAMILY_NAME,
    COL_GENDER,
    COL_DOB,
    COL_CITY,
    COL_PHONE_NUMBER,
    COL_NATIONAL_ID
  )

  val MATCH_COLS: ArraySeq[Int] = ArraySeq(
    COL_GIVEN_NAME,
    COL_FAMILY_NAME,
    COL_GENDER,
    COL_DOB,
    COL_CITY,
    COL_PHONE_NUMBER
  )

}

