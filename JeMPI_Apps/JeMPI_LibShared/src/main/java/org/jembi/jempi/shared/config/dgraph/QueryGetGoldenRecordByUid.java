package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class QueryGetGoldenRecordByUid {

   private QueryGetGoldenRecordByUid() {
   }

   private static String formattedDemographicField(final int idx) {
      return String.format(Locale.ROOT, "      GoldenRecord.demographic_field_%02d", idx);
   }

   private static String formattedUniqueGoldenRecordField(final UniqueGoldenRecordField field) {
      return String.format(Locale.ROOT, "      GoldenRecord.%s", field.fieldName());
   }

   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedAdditionalNodes(final AdditionalNode additionalNode) {
      return "      GoldenRecord." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "         uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "         " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "      }";
   }

   public static String create(final JsonConfig jsonConfig) {
      final var demoGraphicFields = IntStream.range(0, jsonConfig.demographicFields().size())
                                             .mapToObj(QueryGetGoldenRecordByUid::formattedDemographicField)
                                             .collect(Collectors.joining(System.lineSeparator()));
      return "query goldenRecordByUid($uid: string) {"
             + System.lineSeparator()
             + "   all(func: uid($uid)) {"
             + System.lineSeparator()
             + "      uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetGoldenRecordByUid::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueGoldenRecordFields()
                         .stream()
                         .map(QueryGetGoldenRecordByUid::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demoGraphicFields
//             + jsonConfig.demographicFields()
//                         .stream()
//                         .map(QueryGetGoldenRecordByUid::formattedDemographicField)
//                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "   }"
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
