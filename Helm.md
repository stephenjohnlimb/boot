## Helm what is it and why use it.


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
TODO

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