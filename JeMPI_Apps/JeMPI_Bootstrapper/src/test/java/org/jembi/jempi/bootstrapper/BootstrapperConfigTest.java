package org.jembi.jempi.bootstrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BootstrapperConfigTest {
    @Test
    @Order(1)
    public void testItErrorWhenConfigNotAvailable(){
        Assertions.assertThrows(ConfigException.class, () -> {
            BootstrapperConfig.create(null, LogManager.getLogger());
        });
    }

    public void assertPropsCorrect(BootstrapperConfig config){
        assertEquals("127.0.0.1", config.POSTGRESQL_IP);
        assertEquals(5432, config.POSTGRESQL_PORT);
        assertEquals("postgres", config.POSTGRESQL_USER);
        assertEquals("", config.POSTGRESQL_PASSWORD);
        assertEquals("jempi", config.POSTGRESQL_DATABASE);

        assertEquals("127.0.0.1", config.KAFKA_BOOTSTRAP_SERVERS);
        assertEquals("aId", config.KAFKA_APPLICATION_ID);

        assertArrayEquals(new String[]{"127.0.0.1"}, config.DGRAPH_ALPHA_HOSTS);
        assertArrayEquals(new int[]{5080}, config.DGRAPH_ALPHA_PORTS);
    }
    @Test
    @Order(3)
    public void testItCanLoadConfigFromEnvironment(){
        System.setProperty("POSTGRESQL_IP", "127.0.0.1");
        System.setProperty("POSTGRESQL_PORT", "5432");
        System.setProperty("POSTGRESQL_USER", "postgres");
        System.setProperty("POSTGRESQL_PASSWORD", "");
        System.setProperty("POSTGRESQL_DATABASE", "jempi");

        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1");
        System.setProperty("KAFKA_APPLICATION_ID", "aId");

        System.setProperty("DGRAPH_HOSTS", "127.0.0.1");
        System.setProperty("DGRAPH_PORTS", "5080");
        ConfigFactory.systemProperties();

        assertPropsCorrect(BootstrapperConfig.create(null, LogManager.getLogger()));
    }
    @Test
    @Order(2)
    public void testCanLoadConfigFromPath() throws IOException, InterruptedException {
        File file = File.createTempFile( "config", "conf");
        file.deleteOnExit();

        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("""
                POSTGRESQL_IP=127.0.0.1
                POSTGRESQL_PORT=5432
                POSTGRESQL_USER=postgres
                POSTGRESQL_PASSWORD=""
                POSTGRESQL_DATABASE=jempi
                KAFKA_BOOTSTRAP_SERVERS=127.0.0.1
                KAFKA_APPLICATION_ID=aId
                DGRAPH_HOSTS=127.0.0.1
                DGRAPH_PORTS=5080
                """);
        bufferedWriter.close();

        assertPropsCorrect(BootstrapperConfig.create(file.getPath(), LogManager.getLogger()));

    }
}
