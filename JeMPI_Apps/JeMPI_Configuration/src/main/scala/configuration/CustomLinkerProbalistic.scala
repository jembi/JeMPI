package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerProbalistic {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val custom_className = "CustomLinkerProbabilistic"
  private val packageText = "org.jembi.jempi.linker"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList = for (
      t <- config.fields.filter(f => f.m.isDefined && f.u.isDefined)
    ) yield t

    writer.println(s"""package $packageText;""")
    if (muList.length == 0) {
      writer.println()
      writer.println(
        s"""
           |import org.jembi.jempi.shared.models.CustomMU;
           |import org.jembi.jempi.shared.models.CustomEntity;
           |import org.jembi.jempi.shared.models.CustomGoldenRecord;
           |
           |public class $custom_className {
           |
           |  public static float probabilisticScore(final CustomGoldenRecord goldenRecord,
           |                                         final CustomEntity customEntity) {
           |    return 0.0F;
           |  }
           |
           |  public static void updateMU(final CustomMU mu) {
           |  }
           |
           |  public static void checkUpdatedMU() {
           |  }
           |
           |  static CustomMU getMU() {
           |    return new CustomMU(null);
           |  }
           |
           |}""".stripMargin)
    } else {
      writer.println(
        s"""
           |import org.apache.commons.lang3.StringUtils;
           |import org.apache.commons.text.similarity.JaroWinklerSimilarity;
           |import org.apache.logging.log4j.LogManager;
           |import org.apache.logging.log4j.Logger;
           |import org.jembi.jempi.shared.models.CustomMU;
           |import org.jembi.jempi.shared.models.CustomEntity;
           |import org.jembi.jempi.shared.models.CustomGoldenRecord;
           |
           |import static java.lang.Math.log;
           |
           |public class $custom_className {
           |
           |   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerProbabilistic.class);
           |   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
           |   private static final double LOG2 = java.lang.Math.log(2.0);
           |   private static Fields updatedFields = null;
           |
           |   private $custom_className() {}
           |
           |   private static float limitProbability(final float p) {
           |      if (p > 1.0F - 1E-5F) {
           |         return 1.0F - 1E-5F;
           |      } else if (p < 1E-5F) {
           |         return 1E-5F;
           |      }
           |      return p;
           |   }
           |
           |   private static float fieldScore(final boolean match, final float m, final float u) {
           |      if (match) {
           |         return (float) (log(m / u) / LOG2);
           |      }
           |      return (float) (log((1.0 - m) / (1.0 - u)) / LOG2);
           |   }
           |
           |   private static float fieldScore(final String left, final String right, final Field field) {
           |      return fieldScore(JARO_WINKLER_SIMILARITY.apply(left, right) > 0.92, field.m, field.u);
           |   }
           |
           |   private static CustomMU.Probability getProbability(final Field field) {
           |      return new CustomMU.Probability(field.m(), field.u());
           |   }
           |
           |   public static void checkUpdatedMU() {
           |      if (updatedFields != null) {
           |         LOGGER.info("Using updated MU values: {}", updatedFields);
           |         currentFields = updatedFields;
           |         updatedFields = null;
           |      }
           |   }
           |
           |   private record Field(float m, float u, float min, float max) {
           |      Field {
           |         m = limitProbability(m);
           |         u = limitProbability(u);
           |         min = fieldScore(false, m, u);
           |         max = fieldScore(true, m, u);
           |      }
           |
           |      Field(final float m, final float u) {
           |         this(m, u, 0.0F, 0.0F);
           |      }
           |
           |   }
           |
           |   private static void updateMetricsForStringField(final float[] metrics,
           |                                                   final String left, final String right,
           |                                                   final Field field) {
           |      final float MISSING_PENALTY = 0.925F;
           |      if (StringUtils.isNotBlank(left) && StringUtils.isNotBlank(right)) {
           |         metrics[0] += field.min;
           |         metrics[1] += field.max;
           |         metrics[2] += fieldScore(left, right, field);
           |      } else {
           |         metrics[3] *= MISSING_PENALTY;
           |      }
           |   }
           |""".stripMargin)

      writer.println("   static CustomMU getMU() {")
      writer.println("      return new CustomMU(")
      muList.zipWithIndex.foreach((mu, idx) => {
        writer.print(" " * 9 + s"getProbability(currentFields.${Utils.snakeCaseToCamelCase(mu.fieldName)})")
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(
            """);
              |   }
              |""".stripMargin)
      })


      writer.print("   private record Fields(")
      muList.zipWithIndex.foreach((mu, idx) => {
        if (idx == 0)
          writer.print("Field ")
        else
          writer.print(" " * 10 + "Field ")
        end if
        writer.print(Utils.snakeCaseToCamelCase(mu.fieldName))
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(") {}")
          writer.println()
        end if
      })

      writer.println("   private static Fields currentFields =")
      writer.print("      new Fields(")
      var margin = 0
      muList.zipWithIndex.foreach((field, idx) => {
        val m: Double = field.m.get
        val u: Double = field.u.get
        writer.print(" " * margin + s"new Field(${m}F, ${u}F)")
        if (idx + 1 < muList.length)
          writer.println(",")
          margin = 17
        else
          writer.println(");")
      })
      writer.println()
      writer.println(
        """   public static float probabilisticScore(final CustomGoldenRecord goldenRecord, final CustomEntity
          |   customEntity) {
          |      // min, max, score, missingPenalty
          |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
      muList.zipWithIndex.foreach((field, _) => {
        writer.println(" " * 6 + "updateMetricsForStringField(metrics,")
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(" " * 34 + s"goldenRecord.$fieldName(), customEntity.$fieldName(), currentFields" +
                         s".$fieldName);")
      })
      writer.println(" " * 6 + "return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];")
      writer.println(" " * 3 + "}")
      writer.println()
      writer.println("   public static void updateMU(final CustomMU mu) {")
      muList.zipWithIndex.foreach((field, idx) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        if (idx == 0)
          writer.print(" " * 6 + s"if (mu.$fieldName().m() > mu.$fieldName().u()")
        else
          writer.print(" " * 10 + s"&& mu.$fieldName().m() > mu.$fieldName().u()")
        end if
        if (idx + 1 < muList.length)
          writer.println()
        else
          writer.println(") {")
        end if
      })
      writer.println(" " * 9 + "updatedFields = new Fields(")
      muList.zipWithIndex.foreach((field, idx) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(" " * 12 + s"new Field(mu.$fieldName().m(), mu.$fieldName().u())")
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(");")
        end if

      })
      writer.println(" " * 6 + "}")
      writer.println("   }")
      writer.println()
      writer.println("}")
    }
    writer.flush()
    writer.close()
  }

}
