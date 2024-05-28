package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.nio.file.FileSystems;

public final class Config {

   public static final String CONFIG_FILE = "config.json";
   public static final JsonConfig JSON_CONFIG;
   public static final FieldsConfig FIELDS_CONFIG;
   public static final InputInterfaceConfig INPUT_INTERFACE_CONFIG;
   public static final ApiConfig API_CONFIG;
   public static final LinkerConfig LINKER_CONFIG;
   public static final DGraphConfig DGRAPH_CONFIG;

   static {
      final var separator = FileSystems.getDefault().getSeparator();
      final var filePath = "%sapp%sconf_system%s%s".formatted(separator, separator, separator, CONFIG_FILE);
      JSON_CONFIG = JsonConfig.fromJson(filePath);
      FIELDS_CONFIG = new FieldsConfig(JSON_CONFIG);
      INPUT_INTERFACE_CONFIG = new InputInterfaceConfig(JSON_CONFIG);
      API_CONFIG = new ApiConfig(JSON_CONFIG);
      LINKER_CONFIG = new LinkerConfig(JSON_CONFIG);
      DGRAPH_CONFIG = new DGraphConfig(JSON_CONFIG);
   }

   private Config() {
   }

}