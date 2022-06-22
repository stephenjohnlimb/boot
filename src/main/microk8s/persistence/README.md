## Adding a persistent volume in microk8s
This process really only applies on a single node.
For real production work the administrator would mount EFS of NFS type volume rather than a fixed
amount of single node storage.

### The physical allocation
To create this volume to make available to pods, do the following:
- `microk8s enable storage` - just enables the storage mechanism in microk8s
- Get a shell session on to `microk8s-vm` and `sudo mkdir /mnt/data`
- Create a simple html file in that directory called `index.html`

[volume-creation](volume-creation.yaml) is shown below:

```
apiVersion: v1
kind: PersistentVolume
metadata:
  name: task-pv-volume
  labels:
    type: local
spec:
  storageClassName: manual
  capacity:
    storage: 24Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/mnt/data"
```

Now use the following command from a shell with access to the cluster:
`kubectl apply -f ./volume-creation.yaml`

Now using `kubectl get pv` to see the persistent volumes available:
```
k get pv
# NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                               STORAGECLASS        REASON   AGE
# pvc-25f30ba1-7d84-4b04-8d97-f62ee534cd99   20Gi       RWX            Delete           Bound       container-registry/registry-claim   microk8s-hostpath            16d
# task-pv-volume                             24Gi       RWO            Retain           Available                                       manual                       62s
```

You can now see the new volume added.

### Making a claim on that physical space

See [volume-claim](volume-claim.yaml) below for making a claim on that space.
```
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: task-pv-claim
spec:
  storageClassName: manual
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 3Gi
```

Now you can list the claims:
```
kubectl get pvc
# NAME            STATUS   VOLUME           CAPACITY   ACCESS MODES   STORAGECLASS   AGE
# task-pv-claim   Bound    task-pv-volume   24Gi       RWO            manual         21s
```

### An example of using the volume
The file [nginx-deployment](nginx-deployment.yaml) shows how a stock nginx image can be
used but the directory that it uses to serve content is mounted from the `volume`.

The key part of this deployment is as follows:
```
...
    spec:
      volumes:
        - name: task-pv-storage
          persistentVolumeClaim:
            claimName: task-pv-claim
      containers:
        - image: nginx
          name: nginx-pv
          ports:
            - containerPort: 80
          volumeMounts:
            - mountPath: "/usr/share/nginx/html"
              name: task-pv-storage              
```

The key part is the mapping from `persistentVolumeClaim` referencing `task-pv-claim`, then the next part
is the `volumeMounts` with the `mountPath` referencing the volume `task-pv-storage`.

You can now deploy this:
```
kubectl apply -f ./nginx-deployment.yaml

# Now check services and pods.
kubectl get pods
kubectl get services

# nginx-pv-service      NodePort       10.152.183.219   <none>          9095:30587/TCP   10m
```

Now you can go to your local browser and access that nginx service via it's port.
You will then see the content you created earlier `index.html`.

You can get a shell on the `microk8s-vm` and edit `/mnt/data/index.html` and refresh your browser
and the content will be updated - not need to redeploy pods or services.