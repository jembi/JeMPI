ARG JAVA_VERSION

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD Controller-1.0-SNAPSHOT-spring-boot.jar /app/Controller-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server -XX:MaxRAMPercentage=80 -jar /app/Controller-1.0-SNAPSHOT-spring-boot.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
