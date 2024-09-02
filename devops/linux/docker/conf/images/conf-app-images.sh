JAVA_VERSION=21.0.3
JAVA_VERSION_X=${JAVA_VERSION}_9

# https://hub.docker.com/_/eclipse-temurin/tags
export JAVA_BASE_IMAGE=eclipse-temurin:${JAVA_VERSION_X}-jre-alpine

JEMPI_HUB_NAMESPACE=jempi

export ASYNC_RECEIVER_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-async-receiver
export ASYNC_RECEIVER_IMAGE=async_receiver:1.0-SNAPSHOT
export ASYNC_RECEIVER_JAR=AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar

export SYNC_RECEIVER_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-sync-receiver
export SYNC_RECEIVER_IMAGE=sync_receiver:1.0-SNAPSHOT
export SYNC_RECEIVER_JAR=SyncReceiver-1.0-SNAPSHOT-spring-boot.jar

export ETL_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-etl
export ETL_IMAGE=etl:1.0-SNAPSHOT
export ETL_JAR=ETL-1.0-SNAPSHOT-spring-boot.jar

export CONTROLLER_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-controller
export CONTROLLER_IMAGE=controller:1.0-SNAPSHOT
export CONTROLLER_JAR=Controller-1.0-SNAPSHOT-spring-boot.jar

export EM_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-em
export EM_IMAGE=em:1.0-SNAPSHOT
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar

export EM_SCALA_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-em-scala
export EM_SCALA_IMAGE=em_scala:1.0-SNAPSHOT
export EM_SCALA_JAR=em-scala-fatjar-1.0.jar

export LINKER_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-linker
export LINKER_IMAGE=linker:1.0-SNAPSHOT
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar

export API_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-api
export API_IMAGE=api:1.0-SNAPSHOT
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar

export API_KC_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-api-kc
export API_KC_IMAGE=apikc:1.0-SNAPSHOT
export API_KC_JAR=API_KC-1.0-SNAPSHOT-spring-boot.jar

export UI_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-ui
export UI_IMAGE=ui:1.0-SNAPSHOT

export BOOTSTRAPPER_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-bootstrapper
export BOOTSTRAPPER_IMAGE=bootstrapper:1.0-SNAPSHOT
export BOOTSTRAPPER_JAR=Bootstrapper-1.0-SNAPSHOT-spring-boot.jar

export BACKUP_RESTORE_API_HUB_IMAGE=$JEMPI_HUB_NAMESPACE-backuprestoreapi:1.0-SNAPSHOT
export BACKUP_RESTORE_API_IMAGE=backuprestoreapi:1.0-SNAPSHOT
export BACKUP_RESTORE_API_JAR=JeMPI_BackupRestoreAPI-1.0-SNAPSHOT-spring-boot.jar