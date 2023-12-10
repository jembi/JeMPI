ARG JAVA_VERSION

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD Bootstrapper-1.0-SNAPSHOT-spring-boot.jar /app/Bootstrapper-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java --enable-preview -XX:MaxRAMPercentage=80 -XX:+UseZGC -jar /app/Bootstrapper-1.0-SNAPSHOT-spring-boot\n" > /bootstrapper.sh

RUN chmod +x /bootstrapper.sh
