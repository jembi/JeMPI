package org.jembi.jempi.shared.config;

public class PostgresConfig {
    private final String ip;
    private final int port;
    private final String db;
    private final String user;
    private final String password;

    public PostgresConfig(final String ip, final int port, final String db, final String user, final String password) {
        this.ip = ip;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
    }

    // Existing getters...

    /**
     * Generates the JDBC URL for connecting to the PostgreSQL database.
     *
     * @return A string representing the JDBC URL.
     */
    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", ip, port, db);
    }

    // Add a static factory method to create PostgresConfig from environment variables
    public static PostgresConfig fromEnv() {
        String ip = System.getenv("POSTGRES_IP");
        int port = Integer.parseInt(System.getenv("POSTGRES_PORT"));
        String db = System.getenv("POSTGRES_DB");
        String user = System.getenv("POSTGRES_USER");
        String password = System.getenv("POSTGRES_PASSWORD");

        return new PostgresConfig(ip, port, db, user, password);
    }
}
