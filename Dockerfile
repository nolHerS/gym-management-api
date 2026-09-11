FROM eclipse-temurin:26-jdk AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

ARG MAVEN_PROFILE=
RUN chmod +x mvnw && if [ -n "$MAVEN_PROFILE" ]; then ./mvnw -q -P"$MAVEN_PROFILE" -DskipTests package; else ./mvnw -q -DskipTests package; fi

FROM eclipse-temurin:26-jre

WORKDIR /app
COPY --from=build /workspace/target/gym-management-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
