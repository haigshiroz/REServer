Terminal instructions:

mvn java:exec -- will run the javalin server
mvn ck-mvn:metrics -- runs ck to generate csv files

http://localhost:7070/swagger -- enter in browser to access swagger UI once Javalin server is running


Run jars within their own terminal or it will not work:

java -jar target/property-service.jar
java -jar target/analytics-service.jar
java -jar target/api-gateway.jar

kafka-console-consumer --bootstrap-server localhost:9092 --topic property-views --from-beginning --property print.key=true