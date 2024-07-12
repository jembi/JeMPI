package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public final class AppUtils implements Serializable {

   public static final ObjectMapper OBJECT_MAPPER =
         new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).registerModule(new JavaTimeModule());

   @Serial
   private static final long serialVersionUID = 1L;
   static Long autoIncrement = 0L;

   private AppUtils() {
      OBJECT_MAPPER.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls((Nulls.SET)));
   }

   static String getResourceFileAsString(final String fileName) throws IOException {

      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      try (InputStream is = classLoader.getResourceAsStream(fileName)) {
         if (is == null) {
            return null;
         }
         try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
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

   public static <T> T defaultIfFalsy(final T value, final T defaultValue) {
      // Similar to Objects.requireNonNullElse but for broader "falsy" values
      if (value == null) {
          return defaultValue;
      }
      if (value instanceof String && ((String) value).isEmpty()) {
          return defaultValue;
      }
      if (value instanceof Number && ((Number) value).doubleValue() == 0) {
          return defaultValue;
      }
      if (value instanceof Boolean && !((Boolean) value)) {
          return defaultValue;
      }
      if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
         return defaultValue;
      }
      if (value.getClass().isArray() && java.lang.reflect.Array.getLength(value) == 0) {
         return defaultValue;
      }
      return value;
   }

   public static String quotedValue(final String field) {
      if (StringUtils.isBlank((field))) {
         return '"' + StringUtils.EMPTY + '"';
      } else {
         return '"' + field + '"';
      }
   }

   public static String applyFunction(final String func) {
      return switch (func) {
         case "AppUtils::autoGenerateId" -> Long.toString(++autoIncrement);
         default -> null;
      };
   }

   public static String camelToSnake(final String str) {
      final var s = str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   public static String snakeToCamelCase(final String str) {
      String[] words = str.split("_");
      StringBuilder result = new StringBuilder(words[0]);
      for (int i = 1; i < words.length; i++) {
         result.append(words[i].substring(0, 1).toUpperCase()).append(words[i].substring(1));
      }
      return result.toString();
   }

   public static String timeStamp() {
      final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
      final var now = LocalDateTime.now();
      final var stanDate = dtf.format(now);
      return stanDate;
   }

   @Serial
   private Object readResolve() {
      return getInstance();
   }

   private static class UtilsSingletonHolder {
      public static final AppUtils INSTANCE = new AppUtils();
   }

}
