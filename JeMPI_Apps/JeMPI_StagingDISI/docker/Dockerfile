ARG JAVA_VERSION

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD StagingDISI-1.0-SNAPSHOT-spring-boot.jar /app/StagingDISI-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server --enable-preview -XX:MaxRAMPercentage=80 -XX:+UseZGC -jar /app/StagingDISI-1.0-SNAPSHOT-spring-boot.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
