JAVA_VERSION=17.0.6
JAVA_VERSION_X=${JAVA_VERSION}_10

# https://hub.docker.com/_/eclipse-temurin/tags
export JAVA_BASE_IMAGE=eclipse-temurin:${JAVA_VERSION_X}-jre

export ASYNC_RECEIVER_IMAGE=async_receiver:1.0-SNAPSHOT
export ASYNC_RECEIVER_JAR=AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar

export SYNC_RECEIVER_IMAGE=sync_receiver:1.0-SNAPSHOT
export SYNC_RECEIVER_JAR=SyncReceiver-1.0-SNAPSHOT-spring-boot.jar

export PREPROCESSOR_IMAGE=preprocessor:1.0-SNAPSHOT
export PREPROCESSOR_JAR=PreProcessor-1.0-SNAPSHOT-spring-boot.jar

export CONTROLLER_IMAGE=controller:1.0-SNAPSHOT
export CONTROLLER_JAR=Controller-1.0-SNAPSHOT-spring-boot.jar

export EM_IMAGE=em:1.0-SNAPSHOT
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar

export LINKER_IMAGE=linker:1.0-SNAPSHOT
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar

export API_IMAGE=api:1.0-SNAPSHOT
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar
