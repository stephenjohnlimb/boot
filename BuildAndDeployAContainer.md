## Building/Deploying an Image via a Dockerfile

I've covered quite a bit on how to get Multipass/Microk8s up a running on either a Windows PC or a Mac.
This also covered pulling an image (nginx) and pushing it into the Microk8s internal repository and how to use
spring-boot to create a docker image of a spring-boot app.

But here I'm going to focus more on the `Dockerfile` itself and alter the content in the standard `nginx` image by
using it and extending it to create a new image.

### The Docker file
So for this example I am going to create a minimal docker file this is based on the nginx image.
That is a key concept in docker, basically you are best building layers on top of other images. This
not only means you just focus on extending the bits you need, it means that layers may already exist
and therefore do not need to be downloaded.

The [Dockerfile](src/main/microk8s/nginx/Dockerfile) is very simple, in fact this it below:
```
### We will build off the standard docker hub nginx image version 1.22
FROM nginx:1.22
LABEL maintainer="Steve Limb"

### Now add in some configuration file
COPY install /

### EOF
```

We could have added additional components by using:
```
RUN set -x ; apt ...
```

But for this to work you need to know the basis of the operating system your base image was created on.

In this example I've just used the docker instruction to `copy install /`. This means copy all of what is in the
[`install`](src/main/microk8s/nginx/install) directory on to root. You will see this means I overwrite the
[index.html](src/main/microk8s/nginx/install/usr/share/nginx/html/index.html) file.

So now when this docker image spins up it will use my content, rather than the default content provided.

### Building the new image
OK so we have the Dockerfile and some content, lets now build a fresh image.
```
# cd to src\boot\src\main\microk8s\nginx
docker build -t nginxk8s .
```

This will pull the niginx image (version 1.22) down and then put this new layer one top of it and call it
`nginxk8s` it will be pushed into the local repository, where you can see it with `docker images`.

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