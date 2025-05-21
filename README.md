Terminal instructions:

mvn java:exec -- will run the javalin server
mvn ck-mvn:metrics -- runs ck to generate csv files

http://localhost:7070/swagger -- enter in browser to access swagger UI once Javalin server is running


To run jars:

java -jar target/api-gateway.jar
java -jar target/property-service.jar
java -jar target/analytics-service.jar