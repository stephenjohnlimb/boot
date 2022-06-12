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

## Environment variables and property files

If you look in [HelloController.java](../../java/com/tinker/boot/HelloController.java)
you will see that the code looks for an environment variable.
```
public String index() throws InterruptedException
	{
		var injectedByEnvironment = System.getenv("TEST_ENV_VAR");
		Thread.sleep(new Random().nextInt(1000));
		return "Greetings from Spring Boot! Checking an env var is [" + injectedByEnvironment + "]";
	}
```

Now we're going to use a kubernetes manifest and a **ConfigMap** to set that environment variable.
But note there are a couple of bits of abstraction here.

The application is looking for an environment variable called **TEST_ENV_VAR**.

I've now modified [the manifest for the spring-boot app](spring-boot-for-k8s.yml) to include the following
```
env:
  # Define the environment variable
  - name: TEST_ENV_VAR # Notice that the case is different here
    # from the key name in the ConfigMap.
    valueFrom:
      configMapKeyRef:
        name: spring-boot-map # The ConfigMap this value comes from.
        key: check_value # The key to fetch, this will get mapped into TEST_ENV_VAR.
```

This bit of configuration in the manifest, is stating that there will be an environment
variable called **TEST_ENV_VAR** and it's value will come from a (yet to be defined) 
**ConfigMap**, and it will be 'keyed' of a value called 'check_value'.

### ConfigMap
The configuration map can have a variety of bits of configuration within it. For now,
I'll just focus on the environment variables.

Take a look in [the example config map](spring-boot-example1.yml), specifically looks at:
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: spring-boot-map
data:
  # property-like keys; each key maps to a simple value
  check_value: "Injected Value"
```

Once, this **configMap** manifest has been applied `kubectl apply -f src/main/microk8s/spring-boot/spring-boot-example1.yml`
and the spring boot app deployed `kubectl apply -f src/main/microk8s/spring-boot/spring-boot-for-k8s.yml`, the value will be
available.
So now `http://192.168.64.2:30599` will result in the following being displayed:
```
Greetings from Spring Boot! Checking an env var is [Injected Value]
```

#### What's point of all that?
Basically it means that you have decoupled the application code from its configuration.
This means that now we can create any number of **configMap** manifest files and select
the one we want to use separately from the application.
This will become more useful once we move on to use **helm**, as that enables us to use
manifest files in template form.

### Property files
You may be noticed the following configuration in [the example config map](spring-boot-example1.yml).
```
# file-like keys
  check.properties: |
    color.good=purple
    color.bad=yellow
```

You may have also noticed this:
```
...
       volumeMounts:
          - name: config
            mountPath: "/config"
            readOnly: true
      volumes:
        # You set volumes at the Pod level, then mount them into containers inside that Pod
        - name: config
          configMap:
            # Provide the name of the ConfigMap you want to mount.
            name: spring-boot-map
            # An array of keys from the ConfigMap to create as files
            items:
              - key: "check.properties"
                path: "check.properties"
```

This is a mechanism that enables small sets of property values to be made available as
a file to an application in a known location.

This will create a file `/config/check.properties` with contents:
```
color.good=purple
color.bad=yellow
```

You can check this with the following command (use the name of your deployed pod):
```
kubectl get pods
# NAME                                   READY   STATUS    RESTARTS   AGE
# spring-boot-for-k8s-7b559c7989-jdlrd   1/1     Running   0          4h35m
kubectl exec --stdin --tty spring-boot-for-k8s-7b559c7989-jdlrd -- cat /config/check.properties
```

Now it is possible to use those properties from within your deployed spring-boot application.
