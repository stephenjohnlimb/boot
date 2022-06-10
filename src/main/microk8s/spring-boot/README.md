## An explicit build of a spring boot app
Rather than just use the maven build mechanism `mvn spring-boot:build-image` I
thought I'd have a go at creating a spring boot docker image based on alpine.

### How and where to run the docker file

You need to be in the 'root' of this project and then run the following command:
```
docker build -f src/main/microk8s/spring-boot/Dockerfile -t spring-boot .
```
This will then use the [Dockerfile](Dockerfile) which is pretty simple:
```
FROM openjdk:17-jdk-alpine
LABEL maintainer="Steve Limb"

EXPOSE 8080
COPY target/boot-0.0.1-SNAPSHOT.jar boot-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/boot-0.0.1-SNAPSHOT.jar"]
```

Again you'll now have an image in your local repository, you can push that over 
from **primary** (assuming that's where you did your build) into the local
repository in microk8s-vm.

On your **primary** vm
```
docker tag spring-boot:latest 172.19.167.170:32000/spring-boot:latest
docker push 172.19.167.170:32000/spring-boot:latest
kubectl apply -f src/main/microk8s/spring-boot/spring-boot-for-k8s.yml

# Now list the services to get the host port
kubectl get services

# spring-boot-service   NodePort    10.152.183.48   <none>        9095:31205/TCP   19s
```

Interestingly, I forgot to expose port 8080 initially, so to check this on an image you can use:
```
docker inspect --format='{{.Config.ExposedPorts}}' spring-boot:latest
```

