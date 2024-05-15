package configuration

import java.io.{File, PrintWriter}

private object CustomFieldTallies {

  private val classLocation =
    "../JeMPI_LibShared/src/main/java/org/jembi/jempi/shared/models"
  private val customClassName = "CustomFieldTallies"
  private val packageSharedModels = "org.jembi.jempi.shared.models"

  def generate(config: Config): Unit =

    def fieldParameters(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 6}FieldTally ${fieldName},"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end fieldParameters

    def sumFields(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 36}this.${fieldName}.sum(r.${fieldName}),"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end sumFields

    def logFields(): String =
      config.demographicFields
        .map(f => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 6}logMU("${fieldName}", ${fieldName});"""
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end logFields

    def getFieldTally(): String =
      config.demographicFields.zipWithIndex
        .map((f, idx) => {
          val fieldName = Utils.snakeCaseToCamelCase(f.fieldName)
          s"""${" " * 36}getFieldTally(recordsMatch, left.fields.get($idx).value(),
             |${" " * 64}right.fields.get($idx).value()),""".stripMargin
        })
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    end getFieldTally

    val classFile: String =
      classLocation + File.separator + customClassName + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.print(
      s"""package $packageSharedModels;
         |
         |import org.apache.commons.lang3.StringUtils;
         |import org.apache.commons.text.similarity.JaroWinklerSimilarity;
         |import org.apache.logging.log4j.LogManager;
         |import org.apache.logging.log4j.Logger;
         |
         |public record $customClassName(
         |      ${fieldParameters()}) {
         |
         |   private static final Logger LOGGER = LogManager.getFormatterLogger(CustomFieldTallies.class);
         |   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
         |   private static final FieldTally A = new FieldTally(1L, 0L, 0L, 0L);
         |   private static final FieldTally B = new FieldTally(0L, 1L, 0L, 0L);
         |   private static final FieldTally C = new FieldTally(0L, 0L, 1L, 0L);
         |   private static final FieldTally D = new FieldTally(0L, 0L, 0L, 1L);
         |   public static final CustomFieldTallies.FieldTally FIELD_TALLY_SUM_IDENTITY = new CustomFieldTallies.FieldTally(0L, 0L, 0L, 0L);
         |   public static final CustomFieldTallies CUSTOM_FIELD_TALLIES_SUM_IDENTITY = new CustomFieldTallies(
         |      ${("FIELD_TALLY_SUM_IDENTITY," * config.demographicFields.length)
          .split(",")
          .mkString("", "," + sys.props("line.separator") + " " * 6, "")});
         |
         |   private static FieldTally getFieldTally(
         |      final boolean recordsMatch,
         |      final String left,
         |      final String right) {
         |      if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
         |         return FIELD_TALLY_SUM_IDENTITY;
         |      }
         |      final var fieldMatches = JARO_WINKLER_SIMILARITY.apply(left.toLowerCase(), right.toLowerCase()) >= 0.97;
         |      if (recordsMatch) {
         |         if (fieldMatches) {
         |            return A;
         |         } else {
         |            return B;
         |         }
         |      } else {
         |         if (fieldMatches) {
         |            return C;
         |         } else {
         |            return D;
         |         }
         |      }
         |   }
         |
         |   private static void logMU(
         |         final String tag,
         |         final CustomFieldTallies.FieldTally fieldTally) {
         |      LOGGER.debug("%-15s  %,.5f %,.5f",
         |                   tag,
         |                   fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
         |                   fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
         |   }
         |
         |   public static CustomFieldTallies map(
         |         final boolean recordsMatch,
         |         final DemographicData left,
         |         final DemographicData right) {
         |      return new CustomFieldTallies(${getFieldTally()});
         |   }
         |
         |   public void logFieldMU() {
         |      LOGGER.debug("Tally derived M&U's");
         |      ${logFields()};
         |   }
         |
         |   public CustomFieldTallies sum(final CustomFieldTallies r) {
         |      return new CustomFieldTallies(${sumFields()});
         |   }
         |
         |   public record FieldTally(
         |         Long a,
         |         Long b,
         |         Long c,
         |         Long d) {
         |
         |      FieldTally sum(final FieldTally r) {
         |         return new FieldTally(this.a + r.a,
         |                               this.b + r.b,
         |                               this.c + r.c,
         |                               this.d + r.d);
         |      }
         |
         |   }
         |
         |}
         |""".stripMargin
    )
    writer.flush()
    writer.close()
  end generate

}
