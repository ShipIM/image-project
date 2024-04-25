FROM openjdk:17-oracle
ARG JAR_FILE=./filter-gray/target/*.jar
COPY ${JAR_FILE} filter-gray.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/filter-gray.jar"]