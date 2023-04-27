FROM eclipse-temurin
WORKDIR app-taskify
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]