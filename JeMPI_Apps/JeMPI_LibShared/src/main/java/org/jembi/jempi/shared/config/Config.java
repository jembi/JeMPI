package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;

public final class Config {

   public static final String CONFIG_FILE = "config-reference.json";
   //   public static final String CONFIG_FILE = "config-reference-link-d-validate-dp-match-dp.json";
   public static final InputInterfaceConfig INPUT_INTERFACE_CONFIG;
   public static final ApiConfig API_CONFIG;
   public static final LinkerConfig LINKER_CONFIG;
   public static final DGraphConfig DGRAPH_CONFIG;

   static {
      final var jsonConfig = JsonConfig.fromJson(CONFIG_FILE);
      INPUT_INTERFACE_CONFIG = new InputInterfaceConfig(jsonConfig);
      API_CONFIG = new ApiConfig(jsonConfig);
      LINKER_CONFIG = new LinkerConfig(jsonConfig);
      DGRAPH_CONFIG = new DGraphConfig(jsonConfig);
   }

   private Config() {
   }

}
