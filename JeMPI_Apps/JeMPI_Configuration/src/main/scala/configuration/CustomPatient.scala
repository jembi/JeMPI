package configuration

import java.io.{File, PrintWriter}

private object CustomPatient {

  private val classLocation = "../JeMPI_Shared/src/main/java/org/jembi/jempi/shared/models"
  private val packageText = "org.jembi.jempi.shared.models"
  private val customClassNameDemographicData = "CustomDemographicData"

  def generateDemographicData(fields: Array[Field]): Unit =
    val classFile: String = classLocation + File.separator + customClassNameDemographicData + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageText;
         |
         |import com.fasterxml.jackson.annotation.JsonInclude;
         |
         |@JsonInclude(JsonInclude.Include.NON_NULL)
         |public class $customClassNameDemographicData {
         |""".stripMargin)
    fields.zipWithIndex.foreach {
      case (field, idx) =>
        writer.print(s"""${" " * 3}public final """)
        val typeString = field.fieldType
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(typeString + " " + fieldName)
        writer.println(";")
//        writer.println(if (idx + 1 < fields.length) ";" else ") {")
    }

    writer.println(
      s"""
         |}
         |""".stripMargin)

    writer.flush()
    writer.close()
  end generateDemographicData
}

/*
package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {
   public final String auxId;
   public final String givenName;
   public final String familyName;
   public final String gender;
   public final String dob;
   public final String city;
   public final String phoneNumber;
   public final String nationalId;

   public String getAuxId() {
      return auxId;
   }

   public String getGivenName() {
      return givenName;
   }

   public String getFamilyName() {
      return familyName;
   }

   public String getGender() {
      return gender;
   }

   public String getDob() {
      return dob;
   }

   public String getCity() {
      return city;
   }

   public String getPhoneNumber() {
      return phoneNumber;
   }

   public String getNationalId() {
      return nationalId;
   }

   public CustomDemographicData() {
      this(null, null, null, null, null, null, null, null);
   }

   public CustomDemographicData(
         final String auxId,
         final String givenName,
         final String familyName,
         final String gender,
         final String dob,
         final String city,
         final String phoneNumber,
         final String nationalId) {
      this.auxId = auxId;
      this.givenName = givenName;
      this.familyName = familyName;
      this.gender = gender;
      this.dob = dob;
      this.city = city;
      this.phoneNumber = phoneNumber;
      this.nationalId = nationalId;
   }

}
*/
