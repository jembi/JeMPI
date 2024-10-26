package org.jembi.jempi.bootstrapper.data.utils;

public class DataBootstraperConsts {
   protected DataBootstraperConsts() { }
   public static final String KAFKA_BOOT_STRAP_CONFIG_JSON = "/data/kafka/kafkaBootStrapConfig.json";
   public static final String POSTGRES_INIT_SCHEMA_AUDIT_DB = "/data/postgres/audit-schema.sql";
   public static final String POSTGRES_INIT_SCHEMA_NOTIFICATION_DB = "/data/postgres/notifications-schema.sql";
   public static final String POSTGRES_INIT_SCHEMA_MPI_DB = "/data/postgres/mpi-schema.sql";
   public static final String POSTGRES_INIT_SCHEMA_USERS_DB = "/data/postgres/users-schema.sql";
}
