JAVA_VERSION=17.0.7
JAVA_VERSION_X=${JAVA_VERSION}_7

# https://hub.docker.com/_/eclipse-temurin/tags
export JAVA_BASE_IMAGE=eclipse-temurin:${JAVA_VERSION_X}-jre

export EM_IMAGE=em:1.0-SNAPSHOT
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar

export LINKER_IMAGE=linker:1.0-SNAPSHOT
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar

export API_IMAGE=api:1.0-SNAPSHOT
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar

export API_KC_IMAGE=apikc:1.0-SNAPSHOT
export API_KC_JAR=API_KC-1.0-SNAPSHOT-spring-boot.jar

export UI_IMAGE=ui:1.0-SNAPSHOT
