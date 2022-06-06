## Kubernetes (K8s and specifically MicroK8s)

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

#### Set up on Windows
TODO
