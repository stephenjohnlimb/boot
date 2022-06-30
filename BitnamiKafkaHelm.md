## Deploying Kafka via Helm from a Chart

As I plan to play around with Kafka a bit I'll need to deploy some
sort of development cluster to run some tests on.

But on Windows there's a 'fly in the ointment', the dynamic IP range that is always
applied to my `Default Switch` in the Hyper-v.

So I've had a tinker around; and I think the best way to get around this is to actually
create a separate additional network and add my VM's to both switches.

See [Windows Networking stuff with multipass](WindowsNetworkWithMultipass.md), this shows that configuration.

### What is Kafka?
It's basically a bit of tech for passing messages via topics and queues (a bit like
RabbitMQ, IBM MQ, etc.). But it does have a few extra feature like configurable persistence
of messages for a number of consumers. So unlike most message queueing systems it is
possible for messages to stay in a queue just waiting for a consumer to consume them.
it's then up to that consumer to 'move their pointer on' so they don;t process the same message. This is
quite unlike JMS/IBM-MQ - where a message is consumed off the queue (normally when a transaction is committed).

Under the hood it uses a bit of tech called 'Zookeeper' but that may get replaced in future releases.

It's a bit of a beast to set up and configure, but as I only plan to use it for development I'll deploy
it on a single node Kubernetes cluster using a standard helm chart.

But I will make a few changes to the `values` and see how that goes.

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

#### As a stock helm chart
Then I could (and did try out) just deploying that chart - but override a few settings:
```
helm install my-release bitnami/kafka \
--set global.storageClass=manual \
--set zookeeper.persistence.storageClass=manual \
--set zookeeper.persistence.existingClaim=task-pv-claim \
--set persistence.storageClass=manual \
--set persistence.existingClaim=task-pv-claim \
--set externalAccess.enabled=true \
--set externalAccess.service.type=LoadBalancer \
--set externalAccess.service.loadBalancerIPS={'192.168.64.90'} \
--set externalAccess.service.loadBalancerNames={'192.168.64.90'}
```
But this did not quite work out as the metallb Service setting of loadBalancerIPS was not set.
So I think I'll need to pull down the chart and modify it a little.

Now I'm not helm expert (as I'm new to it and writing this), but:
[The external service access template (svc-external-access.yaml)](src/main/helm/kafka/templates/svc-external-access.yaml) is using:
```
{{- if and (not (empty $root.Values.externalAccess.service.loadBalancerIPs)) (eq (len $root.Values.externalAccess.service.loadBalancerIPs) $replicaCount) }}
  loadBalancerIP: {{ index $root.Values.externalAccess.service.loadBalancerIPs $i }}
```

So I put some debug stuff in there and ran:
```
helm install --dry-run --debug my-release ./kafka \
--set global.storageClass=manual \
--set zookeeper.persistence.storageClass=manual \
--set zookeeper.persistence.existingClaim=task-pv-claim \
--set persistence.storageClass=manual \
--set persistence.existingClaim=task-pv-claim \
--set externalAccess.enabled=true \
--set externalAccess.service.type=LoadBalancer \
--set externalAccess.service.loadBalancerIPS={'192.168.64.90'} \
--set externalAccess.service.loadBalancerNames={'192.168.64.90'} \
> /tmp/dryrun.yaml
```

Then had a look in `/tmp/dryrun.yaml` I messed about and altered a few things in `svc-external-access.yaml`
and found that `$root.Values.externalAccess.service.loadBalancerIPs` was empty! What, I passed the values in via a 
`--set` option.

I had imagined that setting the values with `--set externalAccess.service.loadBalancerIPS={'192.168.64.90'}` would
be applied everywhere. **But clearly not**.

So I will now use my own `customValues.yaml` file - well actually modify the one from the chart.

Now maybe this is just my naivety/inexperience - but to a newcomer this is not obvious! Nor is how to debug it.
But having done this sort of stuff for 30 year or more - it's not a surprise, you just have to be patient and
work out how to debug alien tech - until it is no longer alien - just a dysfunctional 'friend'.

#### As a modified helm chart
So lets pull the bitnami/kafka chart down and modify the values.
```
# CD to src\boot\src\main\helm
# Pull and unpack
helm pull bitnami/kafka --untar --untardir=.

# Now we can make a copy of `values.yaml` call it  `customValues.yaml` and put the values in.
cp kafka/values.yaml kafka/customValues.yaml

# So the `customValues.yaml` just has the settings I need. 
# Now you can delete everything except your customValues.yaml

# Finally I can create my kafka cluster and use a persist volume and expose it to my local PC
helm install my-release bitnami/kafka --values ./kafka/customValues.yaml

kubectl get services
# NAME                            TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)                      AGE
# kubernetes                      ClusterIP      10.152.183.1     <none>          443/TCP                      24d
# my-release-kafka                ClusterIP      10.152.183.151   <none>          9092/TCP                     59s
# my-release-kafka-0-external     LoadBalancer   10.152.183.163   192.168.64.90   9094:31617/TCP               59s
# my-release-kafka-headless       ClusterIP      None             <none>          9092/TCP,9093/TCP            59s
# my-release-zookeeper            ClusterIP      10.152.183.247   <none>          2181/TCP,2888/TCP,3888/TCP   59s
# my-release-zookeeper-headless   ClusterIP      None             <none>          2181/TCP,2888/TCP,3888/TCP   59s

kubectl get pods
# NAME                                   READY   STATUS    RESTARTS   AGE
# my-release-kafka-0                     1/1     Running   2          2m17s
# my-release-kafka-client                1/1     Running   0          21h
# my-release-zookeeper-0                 1/1     Running   0          2m17s
```

The point of the last day or so of pain - is to wrangle Helm/Kubernetes/Kafka/Windows to my 'will' and it is only
by having a real task and lots of issues I can learn enough about these technologies to start getting comfortable.

By following:
- [K8S install](K8s.md)
- [Windows Networking and Multipass](WindowsNetworkWithMultipass.md)
- [Helm Setup](Helm.md)
- This page configuring Bitnami/Kafka
- 
I have:
- My local Windows machine with access to a specific local private network via Hyper-V
- My Mac already has access to a known fixed local private network via hyperkit
- I have a vm called `primary` and one running `Microk8s` called `microk8s-vm`
- I have enabled `metallb` in `microk8s` to provide services on 192.168.64-50-192.168.64-100
- Helm has been installed on:
  - The Windows Host
  - The MacOS Host
  - The `primary` Ubuntu vm.
- Now I have installed Kafka with:
  - Use of an external persistent volume - so messages are retained, even if I remove the deployment
  - A known exposed IP address `192.168.64.90` as an externally accessible to by Host PC into the Kakfa cluster in `microk8s`

All I need to do now is try sending some messages into kafka via a `topic` and getting them out again!

So that's really quite a lot of work - just to pass a few messages asynchronously. But this tech stack is one I've
been interested in for a while. The point is that it really uses the same tech as production; and also I'm interested
in learning all about it.

#### How to put stuff into kafka and get it back out

On the `primary` vm use:
- sudo apt-get upgrade
- sudo apt-get install kafkacat

See [Kafkacat](https://docs.confluent.io/4.0.0/app-development/kafkacat-usage.html) for details on how to use it.

This will give us a client to be able to access our kafka cluster from our `primary` vm, this is outside of the
kubernetes cluster.

But I also want to show how we can access it from with the cluster.

Note you need a client for testing (you can use the bitnami one - but I wanted to try a stock one with `kafkacat`):
```
# I've downloaded this simple K8S manifest for deploying a single ubuntu image.
kubectl apply -f src/main/microk8s/ubuntu/ubuntu-deployment.yaml
kubectl get pods
# NAME                                   READY   STATUS    RESTARTS   AGE
# my-release-kafka-0                     1/1     Running   2          40m
# my-release-kafka-client                1/1     Running   0          22h
# my-release-zookeeper-0                 1/1     Running   0          40m
# ubuntu-7b57dfc485-bmlw9                1/1     Running   0          22h

# Now you can see that new ubuntu pod running in the same cluster as kakfa

# To get a session
kubectl exec --stdin --tty ubuntu-7b57dfc485-bmlw9 -- /bin/bash

# Update and install kafkacat
apt-get update
apt-get install kafkacat

# Now lets see if we can contact the kafka broker
# Note that the broker has a DNS address for the service (remember we used the default 9092 for 'client')
# 'default' is our namespace and 'my-release' is the name we gave when deploying via helm 
kafkacat -L -b my-release-kafka.default.svc.cluster.local:9092 -t test-topic

# The response is a bit low level!
# Metadata for test-topic (from broker -1: my-release-kafka.default.svc.cluster.local:9092/bootstrap):
#  1 brokers:
#   broker 0 at my-release-kafka-0.my-release-kafka-headless.default.svc.cluster.local:9092 (controller)
#  1 topics:
#   topic "test-topic" with 1 partitions:
#     partition 0, leader 0, replicas: 0, isrs: 0

```

Now on a session just in `primary` we can do the same sort of thing.
```
# But here - as we are not inside the cluster, we can use that IP address we grafted to get.
kafkacat -L -b 192.168.64.90:9094 -t test-topic
# Metadata for test-topic (from broker 0: 192.168.64.90:9094/0):
#  1 brokers:
#   broker 0 at 192.168.64.90:9094 (controller)
#  1 topics:
#   topic "test-topic" with 1 partitions:
#     partition 0, leader 0, replicas: 0, isrs: 0
```

#### Sending a Receiving
Finally, lets send some message from our test ubuntu pod and pull them out via our `primary` Hyper-V vm.

On the ubuntu pod:
```
# To get on to it (from your host PC)
kubectl exec --stdin --tty ubuntu-7b57dfc485-bmlw9 -- /bin/bash

# Lets produce some messages
kafkacat -P -b my-release-kafka.default.svc.cluster.local:9092 -t test-topic

# Now just type some text and press return
First Message

# Leave it at the prompt for a while and move on to this next bit below
```

On the `primary` vm:
```
# To get on to it (from your host PC)
multipass shell primary

# Lets Consumer some messages
kafkacat -C -b 192.168.64.90:9094 -t test-topic
First Message
% Reached end of topic test-topic [0] at offset 1
```

Now if you keep both these sessions open and write more message from the ubuntu pod, you will see them pop out in the
`primary` vm window:

### Stopping your consumer and starting it again
If you stop your consumer kafkacat (use ctrl-C) in the `primary`vm and then run it again.

What do you expect - surprise - you get all the messages again!

that's the rub with Kafka, the data on the topics is persistent unless you update your counter position as a named
consumer. Though you can set a 'lifetime' for the message retention period.

