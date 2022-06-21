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

#### Port and IP addresses
There are different ways of providing ingress from your local machine into the cluster.

Originally my `spring-boot-for-k8s.yml` had the following:
```
apiVersion: v1
kind: Service
metadata:
  labels:
    app: spring-boot-for-k8s
  name: spring-boot-service
spec:
  type: NodePort
  ports:
  - port: 9095
    protocol: TCP
    targetPort: 8080
  selector:
    app: spring-boot-for-k8s
```

I then altered it to be:

```
apiVersion: v1
kind: Service
metadata:
  labels:
    app: spring-boot-for-k8s
  name: spring-boot-service
spec:
  type: LoadBalancer
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 8080
  selector:
    app: spring-boot-for-k8s
```

Now I get this:
```
kubectl get services
# NAME                  TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)        AGE
# kubernetes            ClusterIP      10.152.183.1     <none>          443/TCP        70d
# spring-boot-service   LoadBalancer   10.152.183.187   192.168.64.50   80:30275/TCP   10m
```

Notice that now an external IP address has been provided, I've also altered the port to use 80,
this is because I now have a dedicated host based IP address available to me.

To get this to work, I did have to alter my microk8s configuration by enabling a few more add-ons.
```
# For my MAC I used
microk8s enable ingress metallb:192.168.64.50-192.168.64.100

# For windows I used
microk8s enable ingress metallb:172.24.174.50-172.24.174.100

# This really just comes down to how the virtual machines and networking is setup via microk8s
```

On my Windows PC I get these results:
```
kubectl get services
# NAME                  TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)          AGE
# kubernetes            ClusterIP      10.152.183.1     <none>          443/TCP          15d
# spring-boot-service   LoadBalancer   10.152.183.231   172.24.174.50   80:32029/TCP     11d
```

This is the key bit to enabling the `LoadBalancer` **type** in the **Service** definition.

Hopefully, now you can see where that new IP address of `192.168.64.50/172.24.174.50` came from. It's from the 
`metallb` add-on - specifically from the pool of IP addresses I gave it. But the range needs to fit how your
host machine works with microk8s.

Now I can actually go to `http://192.168.64.50` (on my Mac) or `http://172.24.174.50` (on windows)
with a browser and get the spring boot response.

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
variable called **TEST_ENV_VAR** and its value will come from a (yet to be defined) 
**ConfigMap**, and it will be 'keyed' of a value called 'check_value'.

### ConfigMap
The configuration map can have a variety of bits of configuration within it. For now,
I'll just focus on the environment variables.

I'll now define the [`configMap`](spring-boot-example1.yml), specifically it looks like:
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

You can check the configmap has deployed with `kubectl get configmaps`.

So now `http://{YOUR_MICROKS-MV_IP}:30599` will result in the following being displayed:
```
Greetings from Spring Boot! Checking an env var is [Injected Value]
```

#### What's point of all that?
Basically it means that you have decoupled the application code and its configuration.
This means that now we can create any number of **configMap** manifest files and select
the one we want to use separately from the application.
This will become more useful once we move on to use **helm**, as that enables us to use
manifest files in template form.

### Property files
You may have noticed the following configuration in [the example config map](spring-boot-example1.yml).
```
# file-like keys
  check.properties: |
    color.good=purple
    color.bad=yellow
```

You may have also noticed this in [spring-boot deployment](spring-boot-for-k8s.yml):
```
...
       volumeMounts:
          - name: config
            mountPath: "/config"
            readOnly: true
      volumes:        
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

This will look like a file called `/config/check.properties` with contents:
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

## Updating the Spring Boot App
Now there is a mechanism to provide a set of properties in a _file_ we can update the
spring-boot application to read those.

I've modified the Controller and added `CheckPropertiesExample`.
```
@RestController
public class HelloController {

	@Autowired
	private CheckPropertiesExample checkPropertiesExample;

	/**
	 * Now map for a REST GET and also time and count the calls.
	 */
	@GetMapping("/")
	@Counted(value = "greeting.count", description = "Number of times GET request is made")
	@Timed(value = "greeting.time", description = "Time taken to return greeting")
	public String index() throws InterruptedException
	{
		var currentDir = System.getProperty("user.dir");

		var injectedByEnvironment = System.getenv("TEST_ENV_VAR");
		Thread.sleep(new Random().nextInt(1000));
		return currentDir +": Greetings from Spring Boot! Checking an env var is [" + injectedByEnvironment + "] from properties " + checkPropertiesExample.getValues();
	}
}
```

Here is that `CheckPropertiesExample` java class:
```
@Configuration
@PropertySource("classpath:check.properties")
@PropertySource(value = "file:config/check.properties", ignoreResourceNotFound = true)
public class CheckPropertiesExample {

  @Autowired
  private Environment env;

  public List<String> getValues()
  {
    return List.of(env.getProperty("color.good"), env.getProperty("color.bad"));
  }
}
```

Also added in a file called `resources/check.properties` with values:
```
#Place holder with some default values
color.good=green
color.bad=red
```

So we will use this as a 'fall back', that's why that `@PropertySource("classpath:check.properties")` is defined
first. Then we use the `@PropertySource(value = "file:config/check.properties", ignoreResourceNotFound = true)`.

So it is the second directive that is optional and when we hook that in via the `configMap`
we should get the values 'purple and 'yellow'. But if the file/configMap is not
wired in then, we will just stick with the defaults.

### Application.properties
With Spring-boot it is also possible to ensure that the application is shutdown gracefully.
To do this you need to add some additional configuration to `application.properties`.

Add this to the configMap `spring-boot-map`
```
application.properties: |
    server.shutdown=graceful
    management.endpoints.web.exposure.include=*
```

It is then necessary to modify the `deployment configuration` with:
```
...
        volumeMounts:
            - name: check-config
            mountPath: "/config/check.properties"
            subPath: "check.properties"
            readOnly: true
            - name: application-config
            mountPath: "/config/application.properties"
            subPath: "application.properties"
            readOnly: true
    
    volumes:
    # You set volumes at the Pod level, then mount them into containers inside that Pod
    - name: check-config
    configMap:
    # Provide the name of the ConfigMap you want to mount.
    name: spring-boot-map
    # An array of keys from the ConfigMap to create as files
    items:
      - key: "check.properties"
        path: "check.properties"
    - name: application-config
    configMap:
    # Provide the name of the ConfigMap you want to mount.
    name: spring-boot-map
    # An array of keys from the ConfigMap to create as files
    items:
      - key: "application.properties"
        path: "application.properties"
```
While this is a bit more verbose (i.e. it seems to be necessary to reference 'application.properties' quite a bit),
it does mean that we have now configured the spring-boot application for a gracefully shut down.

So now do a redeployment of the spring-boot application:
```
kubectl apply -f src/main/microk8s/spring-boot/spring-boot-for-k8s.yml
```

You can do a running log check with:
```
kubectl get pods                                    
# NAME                                   READY   STATUS    RESTARTS   AGE
# spring-boot-for-k8s-59b8c49c55-t97ts   1/1     Running   0          15h

kubectl logs -f spring-boot-for-k8s-59b8c49c55-t97ts
```

Now in a separate shell session, you can stop/delete the deployment and check the running logs to ensure
that the shutdown is graceful.
```
kubectl delete deployment spring-boot-for-k8s
```

Now check that shell session where you were following the logs, you should see:
```
# 2022-06-15 08:47:25.917  INFO 1 --- [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
# 2022-06-15 08:47:25.957  INFO 1 --- [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
```


#### Note
You can get really carried away with optional defaults, layers of fall backs.
Experience tells me that you need to temper your enthusiasm for this, you also need to
drive for consistency in your approach (hard with bigger teams).

While 'Environment Variables' are very good for injecting configuration settings and values,
there are in effect a little like **global variables/constants**. It is very easy for these
to be misused and abused.

The defaults, possible sources of properties and environment variables need to be tightly controlled
and well documented. This is especially true to enable devops/operations teams to understand how to
deploy dockerized applications.

## Summary
This short example of setting up and configuration a spring-boot application and building a docker image is fairly straight forward.
It also shows how you can inject both environment variables and small configuration directives in the form of 
configuration items that can be consumed as 'files'.

It has not covered 'secrets' or 'persistent volumes'.
