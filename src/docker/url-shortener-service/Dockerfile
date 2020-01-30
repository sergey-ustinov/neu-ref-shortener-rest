FROM openjdk:12.0.2-oracle

ADD target/service-distribution/lib /usr/share/jvmservice/lib
ADD target/service-distribution/neu-ref-shortener-1.0-SNAPSHOT.jar /usr/share/jvmservice/service.jar

CMD java $JAVA_OPTS -jar /usr/share/jvmservice/service.jar