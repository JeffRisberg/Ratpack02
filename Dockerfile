FROM anapsix/alpine-java:8_jdk
COPY target/ratpack02-jar-with-dependencies.jar /home/ratpack02-jar-with-dependencies.jar
CMD ["java", "-jar", "/home/ratpack02-jar-with-dependencies.jar"]

