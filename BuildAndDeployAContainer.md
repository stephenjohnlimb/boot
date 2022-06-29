## Building/Deploying an Image via a Dockerfile

I've covered quite a bit on how to get Multipass/Microk8s up a running on either a Windows PC or a Mac.
This also covered pulling an image (nginx) and pushing it into the Microk8s internal repository and how to use
spring-boot to create a docker image of a spring-boot app.

But here I'm going to focus more on the `Dockerfile` itself and alter the content in the standard `nginx` image by
using it; thereby extending it to create a new image.

There is a much more detailed [spring boot example](src/main/microk8s/spring-boot/README.md),
this not only covers creating a Dockerfile, but also includes how to use `configMaps` for
environment variables and property files.

### The Docker file
So for this example I am going to create a minimal docker file, this is based on the nginx image.
That is a key concept in docker, basically you are best building layers on top of other images. This
not only means you just focus on extending the bits you need, it means that layers may already exist
and therefore do not need to be downloaded.

The [Dockerfile](src/main/microk8s/nginx/Dockerfile) is very simple, in fact this it below:
```
### We will build off the standard docker hub nginx image version 1.22
FROM nginx:1.22
LABEL maintainer="Steve Limb"

### Now add in some configuration files
COPY install /

### EOF
```

We could have added additional components by using:
```
RUN set -x ; apt ...
```

But for this to work you need to know the basis of the operating system your base image was created on.
So if the basis was 'alpine' you'd use **apk** for 'debian' based you would use **apt**, so you could add in
'snap' if that was built into your base image.

In this example I've just used the docker instruction to `copy install /`. This means copy all of what is in the
[`install`](src/main/microk8s/nginx/install) directory on to root. You will see this means I overwrite the
[index.html](src/main/microk8s/nginx/install/usr/share/nginx/html/index.html) file.
The important part to understand with copy it is recursive and basically will copy all the files and the
directory structure from your workspace into the docker image you are creating.

So now when this docker image spins up it will use my content, rather than the default content provided.

There is more to docker files than this, like putting applications on (like java Jar files) and setting the
entry point. In the case of Java setting the memory configuration for example.

### Building the new image
OK so we have the Dockerfile and some content, lets now build a fresh image.
```
# Using a command prompt 'bash' or 'powershell'
# cd to src\boot\src\main\microk8s\nginx
docker build -t nginxk8s .
```

This will pull the niginx image (version 1.22) down and then put this new layer one top of it and call it
`nginxk8s`; it will be pushed into the local repository, where you can see it by using `docker images`.

Now you can run it up locally if you wish (i.e. without deployment to microk8s yet).

The docker command to run that image up and expose it on port 8080.

The `-it` make this interactive, so you can see the output on the console, the `-rm` means remove the container
when the docker process complete.
```
# 
docker run -it --rm -p 8080:80 --name nginxk8s nginxk8s
```

Now go to your browser and enter `http://localhost:8080` and you should see:
```
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Nginx for Microk8s</title>
</head>
<body>
    <h2>Content overridden with another layer</h2>
    <p>
        Any sort of content could go in here.
    </p>
    <h3>Configuration</h3>
    <p>
        If desirable we could also override the nginx configuration in /etc/nginx/sites-enabled.
    </p>
</body>
</html>
```

This is the content from [index.html](src/main/microk8s/nginx/install/usr/share/nginx/html/index.html).

Tidying up:
```
docker ps -a
# bc776e0cea64   nginxk8s   "/docker-entrypoint.…"   20 minutes ago   Up 20 minutes   0.0.0.0:8080->80/tcp   nginxk8s
docker stop nginxk8s
```

### Another Spring example
[Readme](src/main/microk8s/spring-boot/README.md) for more details on dockerizing a spring boot app.

#### Taking stock
While this section has been quite short (intentionally), this page has covered:
- Using an existing docker image as a base using 'FROM'
- Adding content using 'COPY'
- Creating the image
- Running that image up just using docker itself - rather than deploying into microk8s

### Additional Dockerfile instructions
The [docker reference](https://docs.docker.com/engine/reference/builder/) provides full details on all
the instructions available. But I've added a few key ones below; with some explanations.
- ENV SOME_NAME=SOMEVALUE - this is useful for defining a value once and then using it in other instructions
- RUN SOME_COMMAND - the important point here is that this is run in the new layer you are building
- LABEL - just provides a form of meta data
- EXPOSE SOME_PORT - for web services this is critical so that TCP/UDP traffic can be received
- ADD/COPY - used to copy files from your local machine into the new layer you are creating
- ENTRYPOINT [SOME_COMMAND, SOME_OPTIONS] - this is the actual command to start running your application
- VOLUME [SOME_DIRECTORY] - marks the directory as being a location where externally host directories can be mounted
- USER SOME_USER:SOME_GROUP - important for running as non-root, create a user and run main application as that user
- WORKDIR SOME_PATH - this is like changing directory (cd) to a specific directory before running commands

There are more commands, but I've found these to be the most important.

## Deploying nginxk8s to microk8s
While it might be a bit repetitive, as I've already covered this [before](K8s.md), I'm going to reinforce it again.
First get a bash session on your **primary**; that's the vm with docker installed, the microk8s-vm is running on
**192.168.64.2** in a vm.

So starting from the host powershell/bash (and assuming you have the src directory mounted)
```
# get on to primary
multipass shell primary
cd src/boot/src/main/microk8s/nginx/

# Build the image
docker build -t nginxk8s .

# Tag that image ready to push into the microk8s-vm registry
docker tag nginxk8s:latest 192.168.64.2:32000/nginxk8s:primary

# Now push it from the local registry to the microk8s-vm registry
docker push 192.168.64.2:32000/nginxk8s:primary

# Now lets plan to deploy in the 'test' namespace
kubectl config use-context test

# Do a simple deployment via command line
kubectl create deployment nginx-for-k8s --image=localhost:32000/nginxk8s:primary

# Expose this as a service
kubectl expose deployment nginx-for-k8s --port 9095 --target-port 80 --selector app=nginx-for-k8s --type NodePort --name nginxk8s-service

# Let's have a look at what port it is exposed on
kubectl get services
# nginxk8s-service   NodePort   10.152.183.20   <none>        9095:30891/TCP   9s
 
```
Now on you host machine, you can access that port via the `microk8s-vm` i.e. `http://192.168.64.2:30891/`
on a Windows host you can actually use `http://microk8s-vm.mshome.net:30891/` as an entry is made in the hosts.ics file
for you when the multipass virtual machines are started.

## Summary
Again recapping, you can see that once you've gone through the process of setting up multipass with microk8s, there is really
not much difference in effort in deploying a docker image directly with docker, or deploying into microk8s.

So far I've only used `kubectl` with commandline parameters I've not used a configuration file with `kubectl apply -f ...`

To look at an existing deployment/service you can just use the following:
```
kubectl get deployment nginx-for-k8s -o yaml
kubectl get service nginxk8s-service -o yaml
```
From that you can see the form and layout of a configuration file that could be `applied`.

So rather than using multiple `kubectl` commands, you could just use a file
[nginx-for-k8s.yml](src/main/microk8s/nginx/nginx-for-k8s.yml) the contents are shown below:
```
apiVersion: v1
kind: Service
metadata:
  labels:
    app: nginx-for-k8s
  name: nginxk8s-service
spec:
  type: NodePort
  ports:
  - port: 9095
    protocol: TCP
    targetPort: 80
  selector:
    app: nginx-for-k8s
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx-for-k8s
  name: nginx-for-k8s
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx-for-k8s
  template:
    metadata:
      labels:
        app: nginx-for-k8s
    spec:
      containers:
      - image: localhost:32000/nginxk8s:primary
        name: nginxk8s
        ports:
        - containerPort: 80
```

The key point here, is that this now enables you to capture this as _code_ and this is declarative.