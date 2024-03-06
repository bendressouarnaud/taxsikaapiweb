FROM openjdk:17
COPY build/libs/tasikaapiweb-0.0.1-SNAPSHOT.war tasikaapiweb-0.0.1-SNAPSHOT.war
ENTRYPOINT ["java", "-jar", "/tasikaapiweb-0.0.1-SNAPSHOT.war"]