package org.jembi.jempi.libmpi.dgraph;

import java.nio.file.FileSystems;

final class TestConstants {

   private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

   static final String CONFIG_FILE_11 = "src%stest%sresources%s%s"
         .formatted(SEPARATOR, SEPARATOR, SEPARATOR, "config-reference-link-dp.json");

   static final String CONFIG_FILE_12 = "src%stest%sresources%s%s"
         .formatted(SEPARATOR, SEPARATOR, SEPARATOR, "config-reference-link-d-validate-dp-match-dp.json");

   private TestConstants() {
   }

}
