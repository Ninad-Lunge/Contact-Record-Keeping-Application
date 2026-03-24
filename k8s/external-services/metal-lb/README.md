# MetalLB Setup Guide

This guide explains how to expose `ingress-nginx` with MetalLB on a Multipass or bare-metal style Kubernetes cluster.

Use this when:

- `ingress-nginx-controller` is a `LoadBalancer` service
- `EXTERNAL-IP` remains `pending`
- you want the app reachable on a stable IP

## Related Guides

- Kubernetes deployment: [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md)
- Multipass setup: [k8s/multipass/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/multipass/README.md)

## Step 1: Install MetalLB

```bash
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.15.3/config/manifests/metallb-native.yaml
kubectl get pods -n metallb-system
```

Wait until the `controller` and `speaker` pods are `Running`.

## Step 2: Choose An IP Range

Pick unused IPs from the same reachable subnet as your bridged Multipass VMs.

Example:

- node IPs: `10.62.102.85`, `10.62.102.113`
- MetalLB pool: `10.62.102.200-10.62.102.210`

Only use IPs that are not already assigned by DHCP or used by other machines.

## Step 3: Configure MetalLB

Example config:

```yaml
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: contact-record-pool
  namespace: metallb-system
spec:
  addresses:
    - 10.62.102.200-10.62.102.210
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: contact-record-l2
  namespace: metallb-system
```

Apply:

```bash
kubectl apply -f k8s/external-services/metal-lb/metallb-config.yaml
```

## Step 4: Check strictARP

If MetalLB does not assign or advertise addresses correctly, check kube-proxy:

```bash
kubectl get configmap kube-proxy -n kube-system -o yaml | grep strictARP
```

If it is `false`, change it to `true` and restart kube-proxy.

## Step 5: Wait For ingress-nginx To Get An IP

```bash
kubectl get svc -n ingress-nginx -w
```

You want:

- `ingress-nginx-controller`

to receive an external IP from the configured pool.

## Step 6: Verify ingress

```bash
kubectl get svc -n ingress-nginx
kubectl get ingress -n contact-record
```

The ingress address should match the MetalLB-assigned IP.

## Step 7: Add The Host Mapping

On Windows:

Edit:

```text
C:\Windows\System32\drivers\etc\hosts
```

Add:

```text
<metallb-ip> contact-record.localtest.me
```

Then flush DNS:

```powershell
ipconfig /flushdns
```

## Step 8: Test

From the cluster side:

```bash
curl -H "Host: contact-record.localtest.me" http://<metallb-ip>
```

From Windows:

```powershell
curl http://contact-record.localtest.me
```

Open:

```text
http://contact-record.localtest.me
```

## Troubleshooting

### EXTERNAL-IP is still pending

Check:

```bash
kubectl get pods -n metallb-system
kubectl get ipaddresspools.metallb.io -n metallb-system
kubectl get l2advertisements.metallb.io -n metallb-system
```

### Windows cannot reach the MetalLB IP

If Windows also cannot reach the node IPs, the problem is the VM network setup, not MetalLB.

Use bridged networking for Multipass.

### curl works inside the cluster but browser does not work on Windows

Check:

- Windows hosts file entry
- DNS cache
- whether the Multipass IP range is reachable from Windows
