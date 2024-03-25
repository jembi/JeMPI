package org.jembi.jempi.shared.models;

import java.util.ArrayList;
import java.util.List;

public class DemographicData {

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

   public static CustomDemographicData.CustomDemographicDataAPI fromCustomDemographicData(final DemographicData demographicData) {
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

   public DemographicData clean() {
      return new DemographicData(fields.stream()
                                       .map(x -> new DemographicData.Field(x.tag,
                                                                           x.value.trim().toLowerCase().replaceAll("\\W", "")))
                                       .toList());
   }

   public record Field(
         String tag,
         String value) {
   }

}
