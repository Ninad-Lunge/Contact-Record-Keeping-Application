# Multipass Kubernetes Setup Guide

This guide covers a practical Kubernetes setup on Multipass for this repository.

It assumes:

- Windows host
- Multipass VMs
- `kubeadm` cluster
- one `master` VM
- one or more `worker` VMs

## Important Networking Note

If you want to access the application directly from Windows in a browser, bridged networking is the correct model.

Default Multipass NAT networking often makes:

- node IPs unreachable from Windows
- NodePorts unreachable from Windows
- MetalLB IPs unreachable from Windows

If you already built the cluster on the default NAT network and Windows cannot reach node IPs, rebuild the VMs with bridged networking.

## Recommended VM Sizing

For a small lab cluster:

- `master`: 2 CPU, 4 GB RAM, 20 GB disk
- `worker`: 2 CPU, 4 GB RAM, 20 GB disk

If you plan to run more than one backend replica, give the worker more memory.

## Step 1: Create A Bridged Network

On Windows:

1. open `Hyper-V Manager`
2. open `Virtual Switch Manager`
3. create an `External` switch
4. bind it to your active network adapter
5. give it a stable name such as `ExternalSwitch`

Check available Multipass networks:

```powershell
multipass networks
```

Set the preferred bridged network:

```powershell
multipass set local.bridged-network=ExternalSwitch
```

## Step 2: Launch Multipass Instances

Examples:

```powershell
multipass launch 24.04 --name master --cpus 2 --memory 4G --disk 20G --bridged
multipass launch 24.04 --name worker --cpus 2 --memory 4G --disk 20G --bridged
```

Or explicitly:

```powershell
multipass launch 24.04 --name master --cpus 2 --memory 4G --disk 20G --network ExternalSwitch
multipass launch 24.04 --name worker --cpus 2 --memory 4G --disk 20G --network ExternalSwitch
```

Verify:

```powershell
multipass list
```

The instances should get IPs that Windows can reach.

## Step 3: Verify Reachability From Windows

```powershell
ping <master-ip>
ping <worker-ip>
```

Do not continue until Windows can reach the VM IPs.

## Step 4: Install Kubernetes Prerequisites

On both `master` and `worker`:

```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg
sudo swapoff -a
```

Disable swap permanently if needed by editing `/etc/fstab`.

Load required kernel modules:

```bash
cat <<'EOF' | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter
```

Set sysctl values:

```bash
cat <<'EOF' | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

## Step 5: Install containerd

On both nodes:

```bash
sudo apt-get update
sudo apt-get install -y containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml >/dev/null
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml
sudo systemctl restart containerd
sudo systemctl enable containerd
```

## Step 6: Install kubeadm, kubelet, kubectl

On both nodes:

```bash
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | \
  sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' | \
  sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

## Step 7: Initialize The Control Plane

On `master`:

```bash
sudo kubeadm init --pod-network-cidr=10.244.0.0/16
```

Configure kubectl for the Ubuntu user:

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

Check:

```bash
kubectl get nodes
```

## Step 8: Install A CNI

Example with Flannel:

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

Wait until nodes are `Ready`.

## Step 9: Join The Worker

On `master`, get the join command from `kubeadm init` output or regenerate:

```bash
kubeadm token create --print-join-command
```

Run that command on the `worker`.

Then verify on the master:

```bash
kubectl get nodes -o wide
```

## Step 10: Install Storage

Install a simple storage provisioner:

```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.34/deploy/local-path-storage.yaml
kubectl get storageclass
```

If `local-path` is not the default:

```bash
kubectl patch storageclass local-path \
  -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

## Step 11: Install ingress-nginx

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace
```

Check:

```bash
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
kubectl get ingressclass
```

## Step 12: Install MetalLB

Use the dedicated guide:

- [k8s/external-services/metal-lb/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/external-services/metal-lb/README.md)

## Step 13: Deploy The Application

Use the Kubernetes deployment guide:

- [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md)

## Step 14: Set Up GitHub Runner

Use the GitHub workflow guide:

- [.github/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/README.md)

## Access Pattern After MetalLB

Once MetalLB assigns an ingress IP:

1. map that IP in Windows hosts file
2. point `contact-record.localtest.me` to it
3. open:

```text
http://contact-record.localtest.me
```

## Troubleshooting

### Windows cannot reach node IPs

This usually means the VMs are still on the default NAT network instead of a bridged network.

### Worker cannot schedule backend pods

Reduce backend replicas or memory requests in [backend.yaml](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/backend.yaml), or increase VM memory.

### MetalLB assigns an IP but Windows cannot reach it

If Windows also cannot reach node IPs, this is still a VM network issue, not a MetalLB issue.

### Self-hosted runner works manually but not as a service

The service user likely does not have the right kubeconfig path or permissions.
