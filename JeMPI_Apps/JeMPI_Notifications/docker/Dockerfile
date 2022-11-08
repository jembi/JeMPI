FROM eclipse-temurin:17.0.4.1_1-jre

ADD Notifications-1.0-SNAPSHOT-spring-boot.jar /app/Notifications-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server --enable-preview -XX:MaxRAMPercentage=80 -XX:+UseZGC -jar /app/Notifications-1.0-SNAPSHOT-spring-boot.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
