## Kubernetes (K8s and specifically MicroK8s and Multipass)

Why even use Kubernetes? Well if you don't really to; **don't**! But the minute you get a
number of separate (truly separately deployable) services. Have several _teams_ working on
each service in a **devops** manner; by this I mean that teams that consist of a full stack capability. This
includes monitoring and owning the running service. Then you're going to have lots of these services and
will need to have somewhere to deploy them.

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

But to do this you need to remember that Kubernetes is really very linux focussed. First, we need to really
give ourselves the ability and knowledge to work with linux virtual machines.
So quite a bit of the initial focus here is on Multipass.

#### Set up on Windows
Just follow the instructions in [this link](https://ubuntu.com/tutorials/install-microk8s-on-window).

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
You can do that by installing docker or buildAh etc.
```
sudo apt-get install docker
sudo usermod -a -G docker ubuntu
exit

# Now maybe mount a local file system and log in again
multipass mount src primary:/home/ubuntu/src
multipass shell primary

# Now navigate to where you have a docker file and trigger a build
# For example just an alpine image with a bit of s6 overlay
docker build -t just-alpine .
docker images

# You will then see the list of docker images.
```

If you wanted Java 17/maven then follow these instructions.
```
sudo apt install openjdk-17-jdk openjdk-17-jre
sudo apt install maven
```

This means that you can actually do full builds with files from your workspace but actually in an ubuntu virtual machine.
For example:
```
sh ./mvnw spring-boot:build-image
```
Assuming your project has the mvnw wrapper, you can then do a full build of a spring boot app and get a docker image out,
directly.


#### Common kubectl commands and setup

Clearly with microk8s you can use the `microk8s kubectl` command directly from the host, but
if you want to use kubectl from within a fairly current ubuntu, you can just use the following:

```
sudo snap install kubectl --classic
```

But how does that specific virtual machine (host) with kubectl 'know' which kubernetes cluster to look at and work with?

It is necessary to set up a `~/.kube/config` file (normally in your home directory). But what content?
Use the command `microk8s config` from your host machine, then save that content in `~/.kube/config`.
You'll need update this as and when you restart the microk8s virtual machine.

So typically from your host and a vm called `primary` you can use:

```
microk8s config | multipass transfer - primary:.kube/config
```

So this pulls the configuration out of the `microk8s-vm` cluster and then pipes it into a multipass command that
transfers stdin content into the `primary` vm and then stores it in a file called `~/.kube/config.

So now if you have a session on `primary` you can just use:

```
kubectl get pods
#or if you have put  alias k='kubectl' in your .bashrc
k get pods
```

#### Basic kubectl concepts and commands
When a Kubernetes _cluster_ is created it is created with a number of **nodes** these are the 'machines' that
will actually run your applications (microservices).

When you do your application builds and then package your application into an 'image/container' (maybe a docker image), it
has your application but also a thin layer of operating system and additional components you configured.

For `kubectl` to be able to manipulate and configure actions within the kubernetes _cluster_ that _cluster_ needs
what is called a _control plane_.

This _control plane_ communicates with software running on the **node**; that is called a 'Kubelet'.

Finally, a number of 'images/containers' can be bundled and placed into a **pod**, that **pod** is then deployed on
one or more **nodes**. Now your application can actually run. It is also possible to create **namespaces** for the
**pods** to run in.

Those applications can then be exposed via **services** which expose ports in a configurable manner.

Now the basic `kubectl` commands involved in getting information about the above cluster concepts.

```
# View the kubernetes configuration
kubectl config view

# A quick overview of the cluster
kubectl get all

# Show the nodes employed in a cluster
kubectl get nodes

# For lots of detail on loading of a node (in this case microk8s-vm)
kubectl describe nodes microk8s-vm

# Resource use by a specific node
kubectl top node microk8s-vm
 
# Show all the pods that are running in all namespaces
# For microk8s with a number of services this will show
# container-registry, default, kube-system, linkerd, monitoring
kubectl get pods --all-namespaces

# You can get more information about the 
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

# Now services
kubectl get services

```

Some additional `kubectl` commands that will be useful:
```
# Get the logs out of a pod - remember kubectl get pods --selector=app=spring-boot
# Will give you the full pod name.
kubectl logs spring-boot-5dcb4777d7-bq2ks

# To keep 'following' the logs
kubectl logs -f spring-boot-5dcb4777d7-bq2ks

# To execute a command on a pod
kubectl exec spring-boot-5dcb4777d7-bq2ks -- ls /

If you really need a bash session on a pod
kubectl exec --stdin --tty spring-boot-5dcb4777d7-bq2ks -- /bin/bash

# Or if you want to copy a file onto a pod (called junk here)
kubectl cp junk spring-boot-5dcb4777d7-bq2ks:/tmp

# To get loads of infor out about the cluster
kubectl cluster-info dump
```