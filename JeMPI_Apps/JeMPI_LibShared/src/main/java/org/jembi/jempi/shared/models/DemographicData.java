package org.jembi.jempi.shared.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.API_CONFIG;

public class DemographicData {

   private static final Logger LOGGER = LogManager.getLogger(DemographicData.class);

   public final List<DemographicData.Field> fields;

   public DemographicData() {
      fields = new ArrayList<>();
   }

   public DemographicData(final DemographicData demographicData) {
      fields = demographicData.fields.stream().toList();
   }

   public DemographicData(final List<DemographicData.Field> fields) {
      this.fields = fields;
   }

   public static CustomDemographicData.CustomDemographicDataAPI fromDemographicData(final DemographicData demographicData) {
      var obj = new CustomDemographicData.CustomDemographicDataAPI();
      var cls = obj.getClass();
      demographicData.fields.forEach(f -> {
         try {
            var classField = cls.getDeclaredField(f.tag());
            classField.setAccessible(true);
            classField.set(obj, f.value());
            classField.setAccessible(false);
         } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      });
      return obj;
   }

   public static DemographicData fromCustomDemographicData(final CustomDemographicData.CustomDemographicDataAPI customDemographicData) {
      final var cls = customDemographicData.getClass();
      return new DemographicData(
            API_CONFIG.demographicDataFields
                  .stream()
                  .map(f -> {
                     try {
                        var classField = cls.getDeclaredField(f.getLeft());
                        return new Field(f.getLeft(), classField.get(customDemographicData).toString());
                     } catch (NoSuchFieldException | IllegalAccessException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                        return new Field(f.getLeft(), null);
                     }
                  })
                  .toList());
   }

   public DemographicData clean() {
      return new DemographicData(
            fields.stream()
                  .map(x -> new DemographicData.Field(x.tag,
                                                      x.value.trim()
                                                             .toLowerCase()
                                                             .replaceAll("\\W", "")))
                  .toList());
   }

   public record Field(
         String tag,
         String value) {
   }

}
