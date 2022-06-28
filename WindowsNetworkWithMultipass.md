## Windows Networking and Multipass

To be able to use kafka from both within my cluster and also from my Windows dev machine
(I like to be able to run IntelliJ on my local dev machine and stuff things into ports/pipes
that are running in my Kubernetes cluster) - I need some fixed IP addresses to work with.

### The Hyper-V Default-Switch (Default Network)
It's great out of the box for your VM's, and it does NAT and all good stuff. But it gets
a new address range on each time your PC reboots. This is not good for me.

So I'm going to try and see if I can configure a separate network.

#### Command Line to create a new virtual switch

Use a powershell `As Administrator` and do the following (I'm using 192.18.0.1/24 here).
```
New-VMSwitch -SwitchName "NATSwitch" -SwitchType Internal
New-NetIPAddress -IPAddress 192.168.0.1 -PrefixLength 24 -InterfaceAlias "vEthernet (NATSwitch)"
New-NetNAT -Name "NATNetwork" -InternalIPInterfaceAddressPrefix 192.168.0.0/24

# To check these values you can look in the Hyper-V Manager in Windows or
Get-NetAdapter
Get-NetIPAddress
Get-NetNat
```

#### Upgrade multipass
Now to be able to use these via `Multipass` I had to upgrade my Multipass from `1.3` to `1.8` -
now I tried `1.9` but that just asked me for load of authentication and the like. Which I couldn't
get working. So I uninstalled `1.3` and installed `1.8` (but then you do need to reboot after).

But then you have a couple of issues to sort out like mounts now disabled by default.
```
# As an administrator issue this command to enable mounts.
multipass set local.privileged-mounts=true

Then you can do your normal mount as a user
multipass mount src primary:/home/ubuntu/src

# And if you want to unmount
multipass umount primary 
```

### Create a new VM as a test
Now I created a new Virtual Machine as a test, but using the additional network. You always seem to get the
`Default Switch` network - which I suppose makes sense else you could easily cut yourself off.

```
multipass launch --network name=NATSwitch,mode=manual
# This resulted in a new vm called 'refreshing-moray'

# So I got a shell on to that
multipass shell refreshing-moray

# Had a look at the IP config
ip -br address show scope global

# eth0 was up - thats running on the Default Switch
# But eth1 was down - GOOD - note I said mode=manual
# I've now got to do the 'manual bit' 
```

#### Note if you already have a VM
So if you already have a couple of VM's and don;t want to recreate them:
- Just stop the VM
- Go to Hyper-V Manager and select each VM in turn
- Go to Settings and Add Hardware
- Pick Network Adaptor
- Then select the `NATSwitch'

Once you've done that you can follow the same instructions below (giving each VM its own IP).

So far, I've got a new virtual machine with two ethernet adaptors available but only one
(on the Default Switch) working. The other is connected to my new `NATSwitch` but that's not yet
configured in the Linux VM. So I'll do that now.

```
sudo vi /etc/netplan/50-cloud-init.yaml
# Now add in the following:
...
eth1:
    addresses: [192.168.0.2/24]
    gateway4: 192.168.0.1
    nameservers:
        addresses: [8.8.8.8, 1.1.1.1]
...
```

So the final config looks like this:
```
# This file is generated from information provided by the datasource.  Changes
# to it will not persist across an instance reboot.  To disable cloud-init's
# network configuration capabilities, write a file
# /etc/cloud/cloud.cfg.d/99-disable-network-config.cfg with the following:
# network: {config: disabled}
network:
    ethernets:
        eth0:
            dhcp4: true
            match:
                macaddress: 52:54:00:25:99:d5
            set-name: eth0
        eth1:
            addresses: [192.168.0.2/24]
            gateway4: 192.168.0.1
            nameservers:
                addresses: [8.8.8.8, 1.1.1.1]
    version: 2
```

I also created file: `/etc/cloud/cloud.cfg.d/99-disable-network-config.cfg` with
`network: {config: disabled}` as per the instructions.

Now just run `sudo netplan apply` - log out and log back in again.

## Check with Multipass

You can now check with multipass, to see if the IP address is available.
```
multipass list
# Name                    State             IPv4             Image
# primary                 Running           172.28.134.101   Ubuntu 20.04 LTS
#                                           172.17.0.1
# microk8s-vm             Running           172.28.140.238   Ubuntu 18.04 LTS
#                                           10.1.37.0
#                                           10.1.37.1
# refreshing-moray        Running           172.28.129.65    Ubuntu 20.04 LTS
#                                           172.28.128.162
#                                           192.168.0.2
```

## Summary
It looks like I can now - rejig my virtual machines in Multipass and give them two network
interfaces, one on the Default Switch and one on my `NATSwitch` where I can then give them fixed IP addresses.
