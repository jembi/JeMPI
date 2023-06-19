package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public final class AppUtils implements Serializable {

   public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                                                      .registerModule(new JavaTimeModule());
   private static final Logger LOGGER = LogManager.getLogger(AppUtils.class);
   @Serial
   private static final long serialVersionUID = 1L;

   private AppUtils() {
      OBJECT_MAPPER.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls((Nulls.SET)));
   }

   static String getResourceFileAsString(final String fileName) throws IOException {

      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      try (InputStream is = classLoader.getResourceAsStream(fileName)) {
         if (is == null) {
            return null;
         }
         try (InputStreamReader isr = new InputStreamReader(is);
              BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
         }
      }
   }

   public static AppUtils getInstance() {
      return UtilsSingletonHolder.INSTANCE;
   }

   public static boolean isNullOrEmpty(final Collection<?> c) {
      return c == null || c.isEmpty();
   }

   public static boolean isNullOrEmpty(final Map<?, ?> m) {
      return m == null || m.isEmpty();
   }

   public static String quotedValue(final String field) {
      if (StringUtils.isBlank((field))) {
         return '"' + StringUtils.EMPTY + '"';
      } else {
         return '"' + field + '"';
      }
   }

   public static String getNames(final CustomDemographicData demographicData) {
      ArrayList<String> names = new ArrayList<>();

      for (Method method : CustomDemographicData.class.getMethods()) {
         if ((method.getName().toLowerCase().contains("name"))) {
            try {
               final var name = (String) method.invoke(demographicData);
               if (!StringUtils.isBlank(name)) {
                  names.add(name.trim());
               }
            } catch (IllegalAccessException | InvocationTargetException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            }
         }
      }
      if (names.isEmpty()) {
         return null;
      } else {
         return StringUtils.join(names, ",");
      }
   }

   @Serial
   private Object readResolve() {
      return getInstance();
   }

   private static class UtilsSingletonHolder {
      public static final AppUtils INSTANCE = new AppUtils();
   }
}
