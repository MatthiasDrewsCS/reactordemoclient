# Performance WebClient vs. RestTemplate

## Start Application

Run Client [Application](src/main/java/app/Application.java) and Server Application

## Setup Thread Monitoring

`watch -n 5 curl http://localhost:7070/actuator/metrics/jvm.threads.live`

`watch -n 5 curl http://localhost:7070/actuator/metrics/tomcat.threads.current`

## Fire Requests

[JMeter](run.jmx)