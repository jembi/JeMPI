ARG JAVA_VERSION=21.0.1-tem

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD Linker-1.0-SNAPSHOT-spring-boot.jar /app/Linker-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server -XX:MaxRAMPercentage=80 -jar /app/Linker-1.0-SNAPSHOT-spring-boot.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
