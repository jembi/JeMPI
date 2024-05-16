package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AuxGoldenRecordField;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateGoldenRecordFields {

   private MutationCreateGoldenRecordFields() {
   }

   private static String formattedGoldenRecordField(final DemographicField field) {
//      final var name = field.fieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = field.indexGoldenRecord() == null
            ? ""
            : field.indexGoldenRecord();
      return "GoldenRecord.%-25s    %-10s%-35s.".formatted(field.scFieldName() + ":", type, index);
   }

   private static String formattedUniqueGoldenRecordField(final AuxGoldenRecordField field) {
      final var name = field.scFieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = "";
      return String.format(Locale.ROOT, "GoldenRecord.%-29s%-10s%-35s.", name, type, index);
   }

   private static String formattedAdditionalGoldenRecordNodes(final AdditionalNode additionalNode) {
      final var name = AppUtils.camelToSnake(additionalNode.nodeName()) + ":";
      return String.format(Locale.ROOT,
                           "GoldenRecord.%-29s%-10s%-35s.",
                           name.startsWith("_")
                                 ? name.substring(1)
                                 : name,
                           "[uid]", "@reverse");
   }

   public static String create(final JsonConfig jsonConfig) {
      final var demographicFields =
            jsonConfig.demographicFields().stream()
                      .map(MutationCreateGoldenRecordFields::formattedGoldenRecordField)
                      .collect(Collectors.joining(System.lineSeparator()));
      return jsonConfig.additionalNodes()
                       .stream()
                       .map(MutationCreateGoldenRecordFields::formattedAdditionalGoldenRecordNodes)
                       .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxGoldenRecordFields()
                         .stream()
                         .map(MutationCreateGoldenRecordFields::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
             + System.lineSeparator()
             + String.format("GoldenRecord.%-29s%-10s%-35s.", "interactions:", "[uid]", "@reverse")
             + System.lineSeparator();

   }

}
