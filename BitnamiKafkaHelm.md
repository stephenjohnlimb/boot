## Deploying Kafka via Helm from a Chart

As I plan to play around with Kafka a bit I'll need to deploy some
sort of development cluster to run some tests on.

But on Windows there's a 'fly in the ointment', the dynamic IP range that is always
applied to my `Default Switch` in the Hyper-v.

So I've had a tinker around; and I think the best way to get around this is to actually
create a separate additional network and add my VM's to both switches.

See [Windows Networking stuff with multipass](WindowsNetworkWithMultipass.md).

### What is Kafka?
It's basically a bit of tech for passing messages via topics and queues (a bit like
RabbitMQ, IBM MQ, etc.). But it does have a few extra feature like configurable persistence
of messages for a number of consumers. So unlike most message queueing systems it is
possible for messages to stay in a queue just waiting for a named consumer to consume them.

Under the hood it uses a bit of tech called 'Zookeeper' but that may get replaced in future releases.

It's a bit of a beast to set up and configure, but as I only plan to use it for development I'll deploy
it on a single node Kubernetes cluster using a standard helm chart.

But I will make a few changes to the `values.yaml` and see how that goes.

### How to deploy
Using the existing `microk8s` and `primary` [multipass virtual machines](K8s.md) I set up earlier, I'll
deploy Kafka, but I want to decide where the persistent data get stored.
So I'll use the [persistence claim](src/main/microk8s/persistence/README.md) I defined.

First I need to add the `bitnami repro` to helm:
```
helm repo add bitnami https://charts.bitnami.com/bitnami

# You can check what repos you have installed
helm repo list
# I have two here
# NAME                    URL
# kubernetes-dashboard    https://kubernetes.github.io/dashboard/
# bitnami                 https://charts.bitnami.com/bitnami
```

Then I can just deploy that chard - but override a few settings:
```
helm install --set global.storageClass=manual --set zookeeper.persistence.storageClass=manual --set zookeeper.persistence.existingClaim=task-pv-claim --set persistence.storageClass=manual --set persistence.existingClaim=task-pv-claim --set logPersistence.storageClass=manual --set logPersistence.existingClaim=task-pv-claim my-release bitnami/kafka
```