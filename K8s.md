## Kubernetes (K8s and specifically MicroK8s and Multipass)

Why even use Kubernetes? Well if you don't really to; **don't**! But the minute you get a
number of separate (truly separately deployable) services. Have several _teams_ working on
each service in a **devops** manner; by this I mean that teams that consist of a full stack capability. This
includes monitoring and owning the running service. Then you're going to have lots of these services and
will need to have somewhere to deploy them.

Also as a developer, you may need to lots of addition 'stuff' running so that you can actually try your code out.
The more you need to have running, the more likely you will need some form of container to put it all in.

This only really works well **at scale**. So the main benefit is not always that the live deployment of a containerized
application scales (which it does); but that the development process can scale.

But testing and proving interfaces are compatible with this model is key. That's why I've looked into PACT.
While an interface may be compatible, experience shows that you need a degree of fluidity. By this I mean XML XSD
schemas are a little too firm, as are JSON 'schemas'.
What PACT focuses on, is what is actually used in terms of the data exchange.
So you can ship more information in a data structure and not break the interface.

### MicroK8S

But as I plan to do some work with kubernetes; I'll have a go with [microk8s on windows](https://ubuntu.com/tutorials/install-microk8s-on-window)
and [microk8s on MacOS](https://ubuntu.com/tutorials/install-microk8s-on-mac-os). There are other
solutions like minikube and k3s, but I thought I'd have a go with microk8s.

To do this you need to remember that Kubernetes is really very linux focussed. First, we need to really
give ourselves the ability and knowledge to work with linux virtual machines.
So quite a bit of the initial focus here is on Multipass.

#### Set up on Windows
Just follow the instructions in [this link](https://ubuntu.com/tutorials/install-microk8s-on-windows) for installation.

Microk8s on Windows and MacOs both use **Multipass**; so in short it uses virtualization to set up an Ubuntu
machine with Microk8s on it. It also adds in a few commands on your host machine (either Windows or Mac).
These are:
- multipass
- microk8s

So to get a shell session on your microk8s-vm you can use the following command:
- multipass shell microk8s-vm

To issue kubectl commands from your host you must use:
- microk8s kubectl ...

Now to get around that endless typing you can adopt aliases or functions in powershell (Windows).
Here are the functions I use (very short 'k', 'p' and 's').
Aliases (for bash):
```
alias k='microk8s kubectl'
alias p='multipass shell primary'
alias s='multipass shell microk8s-vm'
```

Powershell functions (if you want to stay in powershell rather than use bash):
```
function k {
	microk8s kubectl @args
}

function p {
	multipass shell primary
}

function s {
	multipass shell microk8s-vm
}

```

On Windows you will get some additional entries in `C:\WINDOWS\System32\drivers\etc\hosts.ics`:
```
172.19.167.170 microk8s-vm.mshome.net # 2022 6 3 15 9 49 33 141
172.19.161.255 primary.mshome.net # 2022 6 3 15 9 49 33 135
```
These will be based on the virtual machine names you employed.

#### Set up on MacOs
Just follow the instructions in [this link](https://ubuntu.com/tutorials/install-microk8s-on-mac-os), again this
uses virtualization and employs multipass and microk8s.

Again you can just use `aliases` to shorten the commands above and put them in your `.bashrc`.

#### Common multipass commands
It is very useful to be able to mount a local filesystem from the host into the virtual machine. This can be done
with the following command:
```
multipass mount src microk8s-vm:/home/ubuntu/src

multipass umount microk8s-vm
```
This would mount a local directory called `src` into the virtual machine in a directory `/home/ubuntu/src`, this can
be unmounted with the `umount` option. But once you have mounted once, even if the vm is stopped and restarted your mount
is remounted.

Now lets say we need a new vm for a little experiment; we can use the following command:

```
multipass launch --name primary 20.04 --disk 20G
```

For more details on multipass with the `launch` option use: `multipass help launch`.
You can remove this vm when you are done with it using:
```
multipass delete primary
multipass purge
```

You can list the virtual machines you have and their IP addresses (these change on restarts) with:
```
multipass list
```

#### Using Docker in 'primary'
Let's say you want to build docker images from within your 'primary' vm.

There is a separate [set of examples that show building and deploying a container](BuildAndDeployAContainer.md).

You can do that by installing docker or buildAh etc.
```
# You can also install with snap - but apt maybe be better
# sudo snap install docker
# sudo groupadd docker

sudo apt update
sudo apt install docker.io
sudo usermod -a -G docker ubuntu
exit

# Now maybe mount a local file system and log in again
multipass mount src primary:/home/ubuntu/src
multipass shell primary

# You can then try the following
docker run -it ubuntu bash
# then exist from that docker within the vm.

# Now navigate to where you have a docker file and trigger a build
# There is a full worked example of this in the link above.
docker build -t just-alpine .
docker images

# You will then see the list of docker images.
```

If you wanted Java 17/maven then follow these instructions.
```
sudo apt install openjdk-17-jdk openjdk-17-jre

# You need a fairly current version of maven to work with Java 17
curl -o maven-2-8-6.tar.gz https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
sudo tar -xzvf maven-2-8-6.tar.gz -C/opt
rm maven-2-8-6.tar.gz

echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> ~/.bashrc
echo "export MAVEN_HOME=/opt/apache-maven-3.8.6"  >> ~/.bashrc
echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >> ~/.bashrc 
```

This means that you can actually do full builds with files from your workspace but actually in an ubuntu virtual machine.
For example:
```
cd ~/src/boot
mvn spring-boot:build-image
```
Assuming your project has the mvnw wrapper, you can then do a full build of a spring boot app and get a docker image out,
directly. I'm not sure that I'm that keen on this spring-boot image building process, seems a bit 'magic' and also
seems to be pulling down the known world to do a build.

## Kubectl
This next part is really all about `kubectl` - this is just a short list of the command options, `kubectl` really is a bit
of a beast. But then Kubernetes has a number of aspects to it that need managing.

I've tried to put the commands in some sort of order that is logical for me.

#### Common kubectl commands and setup

Clearly with microk8s you can use the `microk8s kubectl` command directly from the host (I use aliases or functions),
but if you want to use kubectl from within a fairly current ubuntu, you can just use the following:

```
sudo snap install kubectl --classic
mkdir ~.kube
```

But how does that specific virtual machine (host) with kubectl 'know' which kubernetes cluster to look at and work with?

It is necessary to set up a `~/.kube/config` file. But what content?
Use the command `microk8s config` from your host machine, then save that content in `~/.kube/config`.
You'll need update this as and when you restart the microk8s virtual machine.

So typically from your host and a vm called `primary` you can use this command:

```
microk8s config | multipass transfer - primary:.kube/config
```

To see what images that are in the local registry in the `microk8s-vm` you can use from the host machine:
```
microk8s ctr images ls
```

So this pulls the configuration out of the `microk8s-vm` cluster and then pipes it into a multipass command that
transfers stdin content into the `primary` vm and then stores it in a file called `~/.kube/config.

So now if you have a session on `primary` you can just use:

```
kubectl get pods
#or if you have put  alias k='kubectl' in your .bashrc
k get pods
```

### Basic Kubernetes concepts

When a Kubernetes _cluster_ is created it is created with a number of **nodes**; these are the 'machines' that
will actually run your applications (microservices).

When you do your application builds and  package your application into an 'image/container' (maybe a docker image), it
has your application but also a thin layer of operating system and additional components you configured.

For `kubectl` to be able to manipulate and configure actions within the kubernetes _cluster_; that _cluster_ needs
what is called a _control plane_.

This _control plane_ communicates with software running on the **node**; that is called a 'Kubelet'.

Finally, a number of 'images/containers' can be bundled and placed into a **pod**, that **pod** is then deployed on
one or more **nodes**. Now your application can actually run. It is also possible to create **namespaces** for the
**pods** to run in.

It is the **deployment** that defines the characteristics of how a pod behaves. 

Those applications can then be exposed via **services** which expose ports in a configurable manner.

You can get `kubectl` command line completion when using bash with:
```
echo "source <(kubectl completion bash)" >> ~/.bashrc
```

#### Some Kubectl commands

Now the basic `kubectl` commands involved in getting information about the cluster concepts above.

```
# To view the kubernetes configuration
kubectl config view

# A quick overview of the cluster
kubectl get all

# Show the nodes employed in a cluster
kubectl get nodes

# For lots of detail on loading of a node (in this case microk8s-vm)
kubectl describe nodes microk8s-vm

# Resource use by a specific node
kubectl top node microk8s-vm

# List out all the name spaces
kubectl get namespaces --show-labels
 
# Show all the pods that are running in all namespaces
# For microk8s with a number of services this will show
# container-registry, default, kube-system, linkerd, monitoring
kubectl get pods --all-namespaces

# You can get more information about the pod with the following
kubectl get pods -o wide

# You can query based on labels
kubectl get pods --show-labels

# Then get the pod you are looking for
kubectl get pods --selector=app=spring-boot

# It is possible get loads of deployment pod details
# In this case for a pod based on a previous deployment of a spring-boot application
kubectl get pod spring-boot-5dcb4777d7-bq2ks -o yaml

# To get lots of details on a running pod
kubectl describe pods spring-boot-5dcb4777d7-bq2ks

# Now services, a service name - will also be registered within kube-dns
kubectl get services
```

So take stock of those commands above, they focus on:
- Overall configuration
- The Nodes
- The Pods
- The Namespaces
- The Services

To recap.
- Nodes are the machine stuff runs on.
- Pods have one or more containers/images running in them
- Services expose the applications in the images running in pods on the nodes via TCP ports.
- Pods/Services are bound into a namespace


#### Some Kubectl commands more focused on runtime

Some additional `kubectl` commands that will be useful, these are more for run time.

```
# Get the logs out of a pod - remember kubectl get pods --selector=app=spring-boot
# Will give you the full pod name - used below.
kubectl logs spring-boot-5dcb4777d7-bq2ks

# To keep 'following' the logs
kubectl logs -f spring-boot-5dcb4777d7-bq2ks

# To execute a command on a pod
kubectl exec spring-boot-5dcb4777d7-bq2ks -- ls /

If you really need a bash session on a pod
kubectl exec --stdin --tty spring-boot-5dcb4777d7-bq2ks -- /bin/bash

# Or if you want to copy a file onto a pod (called junk here)
kubectl cp junk spring-boot-5dcb4777d7-bq2ks:/tmp

# To get loads of info out about the cluster
kubectl cluster-info dump
```

To control and alter a node, i.e. should a node be taken out for maintenance.
```
# No longer schedulable for work
kubectl cordon microk8s-vm

# Drain of all processing ready to take off line
kubectl drain microk8s-vm
```
## Working with Images and Microk8s

I have done a more complete and full [worked example](BuildAndDeployAContainer.md) of working with images and
deploying them in microk8s. But here is a short primer first.

### One way of getting images into microk8s

It's not actually necessary to have the images/container loaded into the microk8s registry, you can just
use images from docker hub or other registries. Clearly then you will need to provide some form of authentication
credentials to microk8s to allow it to pull those images from a remote registry (maybe I'll look at this later).

But for local development of code and images, working on your own development machine and being able to do all
this locally really speeds development up. You could just run the image in docker, unless you are deploying
multiple images/containers or trying out something like prometheus/grafana. Or maybe you need a 'server' running
kafka and lots of other infrastructural type stuff. Then employing microk8s makes a bit more sense.

There are several ways to get docker images into the local registry in microk8s - which is ideal for development
and local testing.

```
# First on the host machine see what is enabled
microk8s status

# Then enable things like registry, dns, linkerd
microk8s enable registry, dns

# You can also check with, you should see container-registry listed
kubectl get pods --all-namespaces

# If you enable prometheus you can get get metrics gathered
microk8s enable prometheus
```

If you want to take a look at those metrics on you local browser, you can
use the following (from your host machine)
```
kbectl port-forward -n monitoring service/prometheus-k8s --address 0.0.0.0 9090:9090
```

This just gives a temporary port mapping, from your local machine on port 9090 to the
prometheus service running in microk8s.

Note if you want to get a shell session on to this pod you can with:
```
kubectl exec --stdin --tty -n monitoring prometheus-k8s-0 -- /bin/sh

# Then you can have a look at the prometheus.yml
more /etc/prometheus/prometheus.yml
```

Have a look at the deployment configuration:
```
kubectl get deployments -n monitoring
kubectl describe deployment -n monitoring prometheus-adapter
kubectl describe deployment -n monitoring prometheus-operator
```

It is also possible to get access to the 'grafana' instance running in microk8s as well:
```
kubectl port-forward -n monitoring service/grafana --address 0.0.0.0 3000:3000
```

Then just use a local browser on your host machine on `http://localhost:3000` and use
`admin\admin` as the username and password. You will then have to set up some graphs and charts.

With this, a local docker/container registry will be made available on port 32000.

Now let's use that separate **primary** vm that was set up earlier and has docker installed.
```
# Lets just get a version of nginx down from docker.io
docker pull nginx

# Show that is now in the registry in the vm
docker images
```

Now if we want to push that (or any other image - even one we've created), we need to tag it.
```
# This is how we tag an image in this local registry
docker tag nginx:latest 172.19.167.170:32000/nginx:primary

# Or we could pull a specific version
docker pull nginx:1.22

# Then tag that
docker tag nginx:1.22 172.19.167.170:32000/nginx:1.22
```

You may be thinking why use that IP address in there and what's it for?
We need either a hostname (microk8s-vm.mshome.net in Windows) or the actual IP address or how to connect to
that registry.
Tag the image in our local repo (because we plan to push that image out to that host later).

The next step is to push those tagged images out to that microk8s-vm (on 172.19.167.170).
```
# Push the tagged image over
docker push 172.19.167.170:32000/nginx:1.22

# But you'll probably have an issue here, something like
# 'Get "https://172.19.167.170:32000/v2/": http: server gave HTTP response to HTTPS client'
```

You will need to edit the `/etc/docker/daemon.json` file and add the following:
```
{
          "insecure-registries" : ["172.19.167.170:32000"]
}
```

Then restart docker and push again:
```
sudo systemctl restart docker

# OR sudo snap restart docker if snap was used to install docker
docker push 172.19.167.170:32000/nginx:1.22
```

Now clearly if you used 'Kaniko' or 'BuildAh' you may not have these issues (you may have different ones).

But now you have either pulled or created an image in one machines repository, tagged it and then pushed it out
into the microk8s local repository.

### Listing the images in a repository

If we were using docker as set up in the **primary** vm we'd just use `docker images` from a bash shell in
**primary**.

But we'd like to see what images are in the local registry in the microk8s vm. This uses _containerd_ to hold those images.
So from a host command line you can use:
```
# This is the command to interact with containerd
microk8s ctr help

# To get the images
microk8s ctr images ls
```

#### Deploying that image
Here is a quick command line to deploy that nginx image we pushed from a local docker repository in
**primary** into **microk8s-vm** registry.

But this time I'm also going to expose the running application as a service.

On the **primary** vm we set up earlier you can use the following commands:
```
# Remember this is kubectl talking with the control plan inside kubernetes on microk8s-vm
# So localhost:32000 is actually inside microk8s-vm i.e. its local registry
kubectl create deployment nginx-try --image=localhost:32000/nginx:1.22

#Now check if that image is running in a pod on a node
kubectl get pods

# Or just check that pod
kubectl get pods --selector=app=nginx-try

# Get full details
kubectl describe pods nginx-try-5697dbcbf5-vvmzx

# A bit more detail about that deployment
kubectl describe deployments nginx-try

# We can now expose this running pod as a service
kubectl expose deployment nginx-try --port 9095 --target-port 80 --selector app=nginx-try --type NodePort --name nginx-service

#Now lets have a look at that service
kubectl get services

# You should see something like:
# nginx-service   NodePort    10.152.183.182   <none>        9095:31145/TCP   3s
```

So now, not only is the image running in a pod on a node, we have also exposed the nginx port of **80** on port 9095.
But also via host port 31145!

**So what does this mean? How can I see something**

OK, let's recap again.
- We've taken nginx version 1.22 and pushed into microk8s (local) registry
- We've then created a simple **deployment** called **nginx-try** and run that up in a pod
- This means that nginx in that pod is listening on port 80 (that's how it was build by the developer)
- Now by using **kubectl expose deployment nginx-try** we've exposed port 80 via port 9095.
- The important bits here are **--port 9095 --target-port 80** and **type NodePort**
- Finally, we've given it a name of **nginx-service**

You may be expecting to be able to go to your local host browser and enter:
- _http://localhost:80_
- Or _http://localhost:9095_

Or something like that, **but no I'm afraid not**. It's a bit more complex than that.
Kubernetes has its own internal networking stuff going on. So lets un-pick it a bit.

Here is an example from my machine.
```
# Lets get the services
kubectl get services

# NAME            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
# kubernetes      ClusterIP   10.152.183.1     <none>        443/TCP          2d23h
# nginx-service   NodePort    10.152.183.182   <none>        9095:31145/TCP   3s
```

So now if I get a bash session on `microk8s-vm` (that's where the Kubernetes cluster is running), now I can see if
I can see the nginx welcome page. For this I need to use the `CLUSTER-IP` of **10.152.183.182**, this will
be available to me in the Kubernetes cluster (but only within that cluster). I could have just used **ClusterIP**
and it would still have been exposed within the cluster on IP **10.152.183.182**, but not exposed on the host.

Now I can try a `curl http://10.152.183.182:9095` and I get this:
```
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

So success, if I was wiring lots of services together I'd be good to go. But as I used **type NodePort**
I have actually asked Kubernetes to expose this out to the host. For this Kubernetes has used a dynamic port number
(in this case 31145).

So you are probably thinking great, _http://localhost:31145_ will now work. Well **No!** That's because I'm running
microk8s-vm inside a virtual machine via multipass.

Now that virtual machine currently has an IP address of **172.19.167.170** so to use a local browser and see the same
content that was served from within the Kubernetes cluster I need to use:
- http://172.19.167.170:31145/

**Finally, success!**

One of the important bits here is deciding **how** you would like to _expose_ the node, this is done with **type**
on the `kubectl expose` command, this can be **LoadBalancer, ClusterIP** or **NodePort**.

To remove the exposed service use:
```
kubectl delete services nginx-service
```

This will leave the pod running, but just remove the exposure.

Lots more [details here](https://kubernetes.io/docs/concepts/services-networking/connect-applications-service/) about
all this.

## Namespaces
So far I've only deployed images/containers to pods in the **default** namespace, you can check what namespaces
have been defined with `kubectl get namespaces --show-labels`. There is more than just **default**, but those are used
by microk8s and some of the add-ons that have been enabled.

Lets create a **test** namespace.

But what is a 'context' - it is just a mechanism and sort of working set of namespaces your `kubectl` will
interact with.

```
# Create the namespace
kubectl create namespace test
 
# Get the current context
kubectl config view

# This will report microk8s
kubectl config current-context

# Now make a new context 
kubectl config set-context test --namespace=test --cluster=microk8s-cluster --user=admin

# Now if you look again you will see an additional context has been created.
kubectl config view

# Use that context
kubectl config use-context test

# This means we're now using the test namespace in the text context
# This will show now pods running (there won't be any yet) - as we are in the test context
kubectl get pods

# If you flip back to use microk8s
kubectl config use-context microk8s
# You will see that nginx pod running that we deployed earlier
kubectl get pods

# Flip back again
kubectl config use-context test

# So hopefully you can now see the point of a 'context'

# Now use same command to deploy and expose the nginx container as we did in the 'default' namespace
kubectl create deployment nginx-try --image=localhost:32000/nginx:1.22
kubectl expose deployment nginx-try --port 9095 --target-port 80 --selector app=nginx-try --type NodePort --name nginx-service

# Lets list the service in out test namespace
kubectl get services

# NAME            TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
# nginx-service   NodePort   10.152.183.89   <none>        9095:31169/TCP   25s

# Again we can check that service is exposed on http://172.19.167.170:31169/
 
```

So, it is quite easy to create new namespaces, then set your `kubectl` to work with a specific context.
This is quite nice, as it means you can set yourself up and just work with a specific namespace with ease.

You're probably thinking, but how can I set up communication between namespaces and what about DNS.

#### Kube DNS
As soon as you expose your deployment of pods as a 'service' in our case above _nginx-service_, Kubernetes
will create a DNS entry for that service. Kubernetes then deals with traffic coming into that service and
distributes it to one the pods that runs on one of your nodes.

So from within the Kubernetes cluster you have a built-in DNS mechanism (within your namespace), just
use the service name you gave your service.

Now to address services in other namespaces (decide if that's a good or a bad idea yourself), you can
just append the namespace to the service name.
For example:
```
# Assuming you have 'test' as your default context lets get a session on to a pod
kubectl get pods

# nginx-try-5697dbcbf5-n7g55   1/1     Running   0          18m

# get bash session on the pod
kubectl exec --stdin --tty nginx-try-5697dbcbf5-n7g55 -- /bin/bash

# Now lets make that curl call to nginx running in the default namespace (not this test namespace)
curl http://nginx-service.default:9095

# You should get the normal nginx welcome HTML response.
```

Now I'll just tidy up and remove that service and deployment from the test namespace, but I'll leave the namespace.

```
kubectl delete service nginx-service
kubectl delete deployment nginx-try

# Just check they have gone
kubectl get services
kubectl get pods
```

## Summary

Kubernetes is a big topic, I've tried to give a sort of overview here, but with some simple practical examples.
Clearly tinkering around with microk8s and namespaces, services, deployments and pods is a start,
but it's quite along way from production. We'd need to think about security, isolation, observations (linkerd/istio),
failing nodes/pods, etc.

I think the biggest thing to wrap your head around is the 'network nature' of Kubernetes, the separate concepts of:
- Nodes that actually run things
- Pods that are groupings of container/images that execute on the Nodes
- Services that act almost like a point of entry/load balancer across the set of pods
- Namespaces that provide some degree of logical isolation and consistent naming/addressing via DNS (from the Services)

The other important part is that namespaces are not fixed rigid bounds.

I've not covered secrets, mounted volumes/environment variables/config maps yet.

There is [more detailed worked example](BuildAndDeployAContainer.md) available - with is more on the developer side.

I won't do that much on the practical operations side of Kubernetes, like draining, scaling, securing. But I might
do some more bits on monitoring.