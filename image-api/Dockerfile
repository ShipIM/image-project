FROM openjdk:17-oracle
ARG JAR_FILE=./image-api/target/*.jar
COPY ${JAR_FILE} image-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/image-api.jar"]