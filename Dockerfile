FROM eclipse-temurin:25-jre-alpine

COPY ./target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=70 -XshowSettings $JAVA_OPTS -jar app.jar"]
