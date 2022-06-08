## Kubernetes (K8s and specifically MicroK8s and Multipass)

Why even use Kubernetes? Well if you don't really to; **don't**! But the minute you get a
number of separate (truly separately deployable) services. Have several _teams_ working on
each service in a **devops** manner; by this I mean, that teams that consist of a full stack capability (and this
includes monitoring and owning the running service). Then you're going to have lots of these services and
will need to have somewhere to deploy them.

This only really works well **at scale**. So the main benefit is not always that the live deployment of a containerized
scales (which it does); but that the development process can scale.

But testing and proving interfaces are compatible is key. That's why I've looked into PACT, because while an interface
may be compatible, experience shows that you need a degree of fluidity. By this I mean XML XSD schemas are a little too
firm, even JSON 'schemas' are. What PACT focuses on, is what is actually used in terms of the data exchange.
So you can ship more information in a data structure and not break the interface.

### MicroK8S

But as I plan to do some work with kubernetes; I'll have a go with [microk8s on windows](https://ubuntu.com/tutorials/install-microk8s-on-window)
and [microk8s on MacOS](https://ubuntu.com/tutorials/install-microk8s-on-mac-os).

But to do this you need to remember that Kubernetes is really very linux focussed. First, we need to really
give ourselves the ability and knowledge to work with linux virtual machines. So quite a bit of the focus here is on
Multipass.

To see the config for microk8s just use `microk8s config`.

#### Set up on Windows
Just follow the instructions in [this link](https://ubuntu.com/tutorials/install-microk8s-on-window).

Microk8s on Windows and MacOs both use **Multipass** so in short it uses virtualization to set up an Ubuntu
machine with Microk8s on it. It also adds in a few commands on your host machine (either Windows or Mac).
These are:
- multipass
- microk8s

So to get a shell session on your microk8s-vm you can use the following command:
- multipass shell microk8s-vm

To issue kubectl commands from your host you must use:
- microk8s kubctl ...

Now to get around that endless typing you can adopt aliases or functions in powershell (Windows).
Here are the functions I use (very short 'k', 'p' and 's').
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

#### Set up on MacOs
Just follow the instructions in [this link](https://ubuntu.com/tutorials/install-microk8s-on-mac-os), again this
uses virtualization and employs multipass and microk8s.

Again you can just use `aliases` to shorten the commands above.

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

You can list the virtual machines you have and their IP addresses with:
```
multipass list
```

#### Using Docker in 'primary'
Let's say you want to build docker images from within your 'primary' vm. You can do that by installing docker.
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