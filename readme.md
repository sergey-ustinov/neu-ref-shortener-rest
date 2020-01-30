# Project description
This is simple service that allows you to shorten URL links to their more compact representation and perform redirects to the source URL addresses by the shortened links.
In case of local dockerization there will be available following servers :
- Service itself located at http://localhost:8022
- Swagger-UI component with description of API specification located at http://localhost:8023
- MongoDB standalone server located at localhost:27017

Service has two types of available API :
- public */api/v1* (fully described in Swagger, RESTful)
- private management */mgmt* (described below, non RESTful)

# Private management API
Management API allows you to build some statistic reports.
This API is under simple Spring authentication and for accessing its endpoints you will need to authenticate with the following credentials :

Username: user
<br>
Password is generated each time when you restart service. It can be found at the service log in the log record starting with 'Using generated security password'

Available management endpoints :
- /mgmt/creation?date=YYYY-MM-DD allows you to build 'Created records per second' statistic report
- /mgmt/year?year=YYYY allows you to build 'Total created records per month' statistic report

'Created records per second' statistic report allows you to view quantity of created references per second in the DB on a selected date.
It has the following JSON structure :
```
{
    'min': int32,
    'max': int32,
    'avg': double,
    'total': int32    
}
```
min - it's just a marker that describes a single request that has been executed during the selected date;<br>
max - this metric describes a maximum bulk request size that has been executed during the selected date;<br>
avg - this metric describes average quantity of creating records per second at the moments of server activity;<br>
total - total created records on a selected date.

'Total created records per month' statistic report allows you to view total sum of created references grouped by month on a selected year.
It has the following JSON structure :
```
[{
    'year': int32,
    'month': int32,
    'total': int32
}, ...]
```

# Local dockerization
This scenario allows you to launch all required components as virtual infrastructure.
This approach assumes that you want to use all components in a virtualization manner, each per its own docker container.
All components will be launched on the localhost in the same subnet. 

*All following commands must be executed from the root directory of the project (where pom.xml is located)*

### 1). Building the project
```
mvn clean install package
```

### 2). Building of docker container with the service
```
docker build -f src/docker/url-shortener-service/Dockerfile -t neu-ref-shortener-service-img:latest .
```

### 3). Starting up the mongo service
```
docker-compose up
```

### 4). Execute mongo migration script
```
docker cp src/scripts/create-schema.js neu-ref-mongo:/create-schema.js
docker exec -it neu-ref-mongo /bin/bash
mongo < create-schema.js
exit
```

### 5). Launching the docker container with the service for the first time
```
docker run -a STDOUT --name neu-ref-shortener-service -p 8022:8022/tcp --ip 172.19.0.5 -e "MONGODB_URL=mongodb://172.19.0.4:27017" --net neu-net-virtual neu-ref-shortener-service-img:latest
```

### 6). Building of docker container with the Swagger service
```
docker build -f src/docker/url-shortener-swagger/Dockerfile -t neu-ref-shortener-swagger-img:latest .
```

### 7). Launching Swagger service with description of API and REST client abilities
```
docker run -a STDOUT --name neu-ref-shortener-swagger -p 8023:8080/tcp --ip 172.19.0.6 --net neu-net-virtual neu-ref-shortener-swagger-img:latest
```

### (Optional) Shutting down the service / Launching the service again
```
docker stop neu-ref-shortener-service
docker start neu-ref-shortener-service
```

### (Optional) Full docker clean up
```
docker stop neueda-swagger
docker stop neueda-service
docker stop neueda-mongo

docker rm neueda-swagger
docker rm neueda-service
docker rm neueda-mongo

docker network rm neueda-virtual

docker rmi neueda-shortener-swagger-img
docker rmi neueda-shortener-service-img
docker rmi swaggerapi/swagger-ui
docker rmi mongo
```