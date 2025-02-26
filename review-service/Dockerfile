FROM openjdk:23-slim as build

ARG JAR_FILE=target/*.jar

COPY ./target/*.jar app.jar

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf /app.jar)

FROM openjdk:23-slim

VOLUME /tmp

ARG DEPENDENCY=/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8084

ENTRYPOINT ["java","-cp","app:app/lib/*","maksim.reviewsservice.ReviewsServiceApplication"]