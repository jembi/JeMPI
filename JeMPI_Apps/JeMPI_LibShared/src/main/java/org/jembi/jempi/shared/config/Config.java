package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.io.File;
import java.nio.file.*;

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
      final String configDir = System.getenv("SYSTEM_CONFIG_DIRS");
      Path filePath = Paths.get(""); // Start with an empty path
      // Create ubuntuFilePath
      Path ubuntuFilePath = new File(String.format("%sapp%sconf_system%s%s", separator, separator, separator, CONFIG_FILE)).toPath();
      // Check if ubuntuFilePath exists
      if (Files.exists(ubuntuFilePath)) {
         filePath = ubuntuFilePath;
      } else {
      // If ubuntuFilePath does not exist, assign the alternative path for windows
         filePath = Paths.get(configDir, CONFIG_FILE);
      }
      JSON_CONFIG = JsonConfig.fromJson(String.valueOf(filePath));
      FIELDS_CONFIG = new FieldsConfig(JSON_CONFIG);
      INPUT_INTERFACE_CONFIG = new InputInterfaceConfig(JSON_CONFIG);
      API_CONFIG = new ApiConfig(JSON_CONFIG);
      LINKER_CONFIG = new LinkerConfig(JSON_CONFIG);
      DGRAPH_CONFIG = new DGraphConfig(JSON_CONFIG);
   }

   private Config() {
   }

}
