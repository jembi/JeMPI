package org.jembi.jempi.shared;

import java.util.HashMap;

public class TestUtilities {

    private static HashMap<String, String> getDefaultVariables() {
        HashMap<String, String> defaultVariables = new HashMap<String, String>();

        defaultVariables.put("POSTGRESQL_IP", "127.0.0.1");
        defaultVariables.put("POSTGRESQL_PORT", "5432");
        defaultVariables.put("POSTGRESQL_USER", "postgres");
        defaultVariables.put("POSTGRESQL_PASSWORD", "");
        defaultVariables.put("POSTGRESQL_DATABASE", "jempi");

        defaultVariables.put("KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1");
        defaultVariables.put("KAFKA_APPLICATION_ID", "aId");

        defaultVariables.put("DGRAPH_HOSTS", "127.0.0.1");
        defaultVariables.put("DGRAPH_PORTS", "5080");

        defaultVariables.put("LINKER_IP", "127.0.0.1");
        defaultVariables.put("LINKER_HTTP_PORT", "6000");

        defaultVariables.put("API_KC_HTTP_PORT", "9088");
        defaultVariables.put("LOG4J2_LEVEL", "DEBUG");

        defaultVariables.put("JEMPI_SESSION_SECURE", "true");
        defaultVariables.put("JEMPI_SESSION_SECRET", "c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrt");
        defaultVariables.put("JEMPI_SESSION_DOMAIN_NAME", "aDomain");
        return defaultVariables;

    }

    public static void SetTestsRunEnvironment(HashMap<String, String> variables){
        HashMap<String, String> allVariables = TestUtilities.getDefaultVariables();
        allVariables.putAll(variables);

        for (HashMap.Entry<String, String> entry: allVariables.entrySet()){
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
