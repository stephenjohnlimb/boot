## Helm what is it, and why use it?

Helm is a sort of package manager/templating mechanism for Kubernetes.

The [spring-boot manifest](src/main/microk8s/spring-boot/spring-boot-for-k8s.yml) and
[example config map](src/main/microk8s/spring-boot/spring-boot-example1.yml) are basically
manifest files for Kubernetes.

Later on in this document I'll go through the redeployment of the `spring-boot` application I have already
developed the kubernetes manifest for; but via helm charts.

### Templating
Now while these are great for defining infrastructure as code, you will find over time you need to 
update and modify these with new versions of images, or maybe add additional config map entries.
But more importantly you may want to take most of those manifest and use them in different 'environments';
for example `TEST`, `UAT` and `PRE` or migration environments.
So you want almost the same configuration, but for example you would not want real 'payments to be taken' in
a test environment, you may also need to 'stub out' some services that cost real money to interact with.

That is where the templating idea comes in. The helm processing requires a defined file structure and file naming.
- Chart.yaml - focuses on the type of things being deployed, the version of the chart and the version of the application.
- values.yaml - is basically just a structured set of values you want to use in helm templates.
- templates - directory holds the templated form of the kubernetes manifests.

### Package Management
The next aspect of helm that is useful; it that of package management. It is important to understand that there are two
aspects of what is being deployed that can vary.
- The version of the actual application itself will change over time - i.e. version 2.1.2 -> 2.1.3 -> 2.2.9
- The version of the chart that defines 'how' that application will be deployed, this includes scaling/replicas etc

Once a chart has been used deploy an application into a cluster; it will be important to record what was deployed and when.
This becomes more important as the number of applications and environments proliferate.

Typical commands that support this are:
- `helm install`
- `helm upgrade`
- `helm status`
- `helm history`

### Install on MacOS

Just use brew to install: `brew install kubernetes-helm`.

Also, just ensure you have your kube config set up correctly.
On a MacOS shell session.
```
microk8s config > ~/.kube/config
chmod go-rwx ~/.kube/config
```
### Install on Linux

```
wget https://get.helm.sh/helm-v3.9.0-linux-amd64.tar.gz
tar xvf helm-v3.9.0-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/local/bin
helm version
rm -rf linux-amd64 helm-v3.9.0-linux-amd64.tar.gz
```

If using the **primary** vm we setup elsewhere then you also nee to ensure
that kube config is setup OK.
```
microk8s config | multipass transfer - primary:.kube/config
microk8s shell primary
chmod go-rwx ~/.kube/config
```

### Install on Windows
Download [helm](https://github.com/helm/helm/releases); then unpack the zip.
Just copy the `helm` application to a directory on your 'PATH'.

## Enable helm in microk8s

From a host shell, issue the following command to enable helm version 3.
```
microk8s enable helm3
```

### Initial use of helm

You can now try (from a shell on either the host or **primary**):
```
helm list
```

It is now necessary to create the chart for your application. helm comes build with a 
mechanism to give you an initial project with the right structure.

#### Some initial thoughts
**But** my initial experience with helm is that it is a 'bit crufty'. I don't mean this in a
negative manner, but it is a bit 'fickle'. For example; it will let you create a chart called
'bootChart' and then only later (when you come to apply it) tell you that names do not conform to what is required.

Also, the `--dry-run` option does not give errors, only when you actually apply fully do you see failures.

There are a few other 'gotchas' as well, for example you'd have thought that if you alter a `configMap`,
bump the `version` in `Chart.yaml` and issue a `helm upgrade ...` that between helm and kubernetes it would be
obvious that the pods that depend on that `configMap` would get redeployed. **But No**.
The only way I could make this work was by actually altering the `values.yaml` specifically the
`podAnnotations` part. Then redeployment of the pod takes place and the updated `configMap` is employed.

So while you go to great lengths in your `deployment.yaml` to state that your pod deployment depends on a
specific `configMap` there is no dependency management within helm/kubernetes that automatically manages this.
Which is really what I would have expected. For saying that both helm and kubernetes were initially release in
2015/2016 - I would have thought they would have this functionality by now.
Hey it's open source I could get involved and add it! It just surprised me; that's all.

## So how do you use it?
Once you have it installed you can create a fresh chart with:
```
# I'm using boot-chart here as the name of my chart
helm create boot-chart
```

This will create a new directory called `boot-chart` and populate it with a range of directories and
`yaml` files.

The first change I will make is in [`Chart.yaml`](src/main/helm/boot-chart/Chart.yaml), I'll alter:
- version: 1.1.2
- appVersion: "2.1.2"

This is to demonstrate the difference between a chart version and the actual docker image version being deployed.

The next change is in [`values.yaml`](src/main/helm/boot-chart/values.yaml), I want to use the spring-boot
application I have already deployed with Kubernetes [See this example](src/main/microk8s/spring-boot/README.md).
You may be wondering how does it know which version of that docker image to deploy (that the appVersion above - so 2.1.2),
but I will need to tag and push that to the microk8s-vm repository.
```
image:
  repository: localhost:32000/spring-boot
  pullPolicy: IfNotPresent

# Also
podAnnotations:
  # Just adding in a check annotation to see what happens.
  envAnnotation: "1.1.2"

# And
service:
  type: LoadBalancer
  port: 80
  targetPort: 8080
```

Note this is how I tagged and pushed the docker image again (obviously you can pull images for any repo):
```
docker tag spring-boot:latest 172.24.172.138:32000/spring-boot:2.1.2
docker push 172.24.172.138:32000/spring-boot:2.1.2
```

#### Deployment
The main focus of the helm deployment is [`deployment.yaml`](src/main/helm/boot-chart/templates/deployment.yaml)
in the [`templates`](src/main/helm/boot-chart/templates) directory. Most of this is the same as the 
[spring-boot kubernetes example](src/main/microk8s/spring-boot/spring-boot-for-k8s.yml).
but it has been templates with all those `{{ ... }}` these are `go template directives`.

You should be able to see where I've pulled the `configMap` in. I'll come on to how to define that next.

#### Env
I've added a file [`env.yaml`](src/main/helm/boot-chart/templates/env.yaml) this is almost identical to
[`spring-boot-example1.yml`](src/main/microk8s/spring-boot/spring-boot-example1.yml) the main difference is the use
of the templating to create the name of the configMap.

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Release.Name }}-configmap
...
```

#### Service
The [`service.yaml`](src/main/helm/boot-chart/templates/service.yaml) has been altered:
```
apiVersion: v1
kind: Service
metadata:
  name: {{ include "boot-chart.fullname" . }}
  labels:
    {{- include "boot-chart.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}
      protocol: TCP
      name: http
  selector:
    {{- include "boot-chart.selectorLabels" . | nindent 4 }}
```

Note how in values I added:
```
service:
  type: LoadBalancer
  port: 80
  targetPort: 8080
```

Now in the service I pick the value of `targetPort` up and use it, `service.type` was already
exposed as a configurable value.

### Templating Summary
Really this is the main point of helm, to provide a way of templating into kubernetes manifest by using
a structured configuration file called `values.yaml`.

## Deploying the chart
The chart is defined now, the docker image has been tagged and pushed to the right repository.

So let's deploy it.

### Check what is running
You can check what is deployed and running with:
```
helm list
# We will get this result once we've deployed, but for now it will be empty
# NAME    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                   APP VERSION
# testrun default         7               2022-06-21 10:32:10.963989954 +0100 BST deployed        boot-chart-1.1.2        2.1.2
```

### Install for the first time
But let's check the files with `lint` and have a dry run first:
```
# From dir :~/src/boot/src/main/helm

helm lint ./boot-chart
 
helm install testrun --dry-run --debug ./boot-chart --set service.type=NodePort
```

This basically just expands all the template directives and results in a set of kubernetes manifests.
But in this example I'm overriding the service type on the command like from LoadBalancer to NodePort,
just to show that you can override the `values.yaml`.
Now run it for real:
```
helm install testrun ./boot-chart --set service.type=NodePort
# Results in:
# NAMESPACE: default
# STATUS: deployed
# REVISION: 1

helm list
# NAME    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                   APP VERSION
# testrun default         1               2022-06-21 15:07:53.239685378 +0100 BST deployed        boot-chart-1.1.2        2.1.2

kubectl get pods
# testrun-boot-chart-5c5c594558-k5klh    1/1     Running   0          3m58s
kubectl get services
# testrun-boot-chart    NodePort    10.152.183.120   <none>        80:32050/TCP     4m33s
```

Now you can go to your browser and access the deployed application via the port (32050 in this case).

You will see something like:
```
/: Greetings from Spring Boot! Checking an env var is [Helm 3 Injected Value] from properties [light-green, bright-red]
```

I have altered the `configMap` with different values, lets now alter `configMap` again and update the deployment.

Edit [`env.yaml`](src/main/helm/boot-chart/templates/env.yaml)
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Release.Name }}-configmap
data:
  # property-like keys; each key maps to a simple value
  check_value: "Helm 3+ Value"

  # file like spring-boot application configuration.
  application.properties: |
    server.shutdown=graceful
    management.endpoints.web.exposure.include=*

  # file-like keys
  check.properties: |
    color.good=dull-green
    color.bad=sun-red
```

Also edit [`Chart.yaml`](src/main/helm/boot-chart/Chart.yaml) an bump the version of the chart to 1.1.3.

It is also necessary to trigger the pod redeployment, this can be done by editing [`values.yaml](src/main/helm/boot-chart/values.yaml):
```
podAnnotations:
  # Just adding in a check annotation to see what happens.
  envAnnotation: "1.1.3"
```

Now you can upgrade:
```
helm upgrade testrun ./boot-chart --set service.type=NodePort
# NAME: testrun
# LAST DEPLOYED: Tue Jun 21 15:19:14 2022
# NAMESPACE: default
# STATUS: deployed
# REVISION: 2

helm status testrun

helm history testrun
# REVISION        UPDATED                         STATUS          CHART                   APP VERSION     DESCRIPTION
# 1               Tue Jun 21 15:07:53 2022        superseded      boot-chart-1.1.2        2.1.2           Install complete
# 2               Tue Jun 21 15:19:14 2022        deployed        boot-chart-1.1.3        2.1.2           Upgrade complete
```

This is where the package management value comes in. We can now see what has happened and when it happened.

If you go back to your browser you will now see:
```
/: Greetings from Spring Boot! Checking an env var is [Helm 3+ Value] from properties [dull-green, sun-red]
```

So while the application `spring-boot` and its version `2.1.2` has not changed, only the configuration has changed.
This is a key point, it is necessary to make your applications **reasonably** configurable in of themselves, but also
to make sure that your helm charts are also **reasonably** configurable.

### Tidy up
To remove that application completely you can use:
```
helm uninstall testrun
```
