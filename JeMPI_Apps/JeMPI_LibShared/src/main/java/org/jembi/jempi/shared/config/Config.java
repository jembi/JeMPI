package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;

public final class Config {

   //   public static final String CONFIG_FILE = "config-reference.json";
   public static final String CONFIG_FILE = "config-reference-link-d-validate-dp-match-dp.json";
   public static final JsonConfig JSON_CONFIG;
   public static final FieldsConfig FIELDS_CONFIG;
   public static final InputInterfaceConfig INPUT_INTERFACE_CONFIG;
   public static final ApiConfig API_CONFIG;
   public static final LinkerConfig LINKER_CONFIG;
   public static final DGraphConfig DGRAPH_CONFIG;

   static {
      JSON_CONFIG = JsonConfig.fromJson(CONFIG_FILE);
      FIELDS_CONFIG = new FieldsConfig(JSON_CONFIG);
      INPUT_INTERFACE_CONFIG = new InputInterfaceConfig(JSON_CONFIG);
      API_CONFIG = new ApiConfig(JSON_CONFIG);
      LINKER_CONFIG = new LinkerConfig(JSON_CONFIG);
      DGRAPH_CONFIG = new DGraphConfig(JSON_CONFIG);
   }

   private Config() {
   }

}
