# Local dockerization
This scenario allows you to launch all required components as virtual infrastructure.
This approach assumes that you want to use all components in a virtualization manner, each per it's own docker container.
All components will be launched on the localhost in the same subnet. 

*All following commands must be executed from the root directory of the project (where pom.xml is located)*

### 1). Building the project
```
mvn clean install package
```

### 2). Building of docker container with the service
```
docker build -f src/docker/url-shortener-service/Dockerfile -t neueda-shortener-service-img:latest .
```

### 3). Starting up the mongo service
```
docker-compose up
```

### 4). Execute mongo migration script
```
docker cp src/scripts/create-schema.js neueda-mongo:/create-schema.js
docker exec -it neueda-mongo /bin/bash
mongo < create-schema.js
exit
```

### 5). Launching the docker container with the service for the first time
```
docker run -a STDOUT --name neueda-service -p 8022:8022/tcp --ip 172.19.0.5 -e "MONGODB_URL=mongodb://172.19.0.4:27017" --net neueda-virtual neueda-shortener-service-img:latest
```

### 6). Shutting down the service / Launching the service again (Optional)
```
docker stop neueda-service
docker start neueda-service
```

### 7). Building of docker container with the Swagger service
```
docker build -f src/docker/url-shortener-swagger/Dockerfile -t neueda-shortener-swagger-img:latest .
```

### 8). Launching Swagger service with description of API and REST client abilities
```
docker run -a STDOUT --name neueda-swagger -p 8023:8080/tcp --ip 172.19.0.6 --net neueda-virtual neueda-shortener-swagger-img:latest
```
