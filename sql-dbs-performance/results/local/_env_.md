All tests were run on my local machine:
* CPU - AMD Ryzen 7 PRO 7840U; 8 cores and 16 threads, base clock speed of 3.3 GHz and a maximum boost of 5.1 GHz
* Memory - 32 GiB
* OS - Ubuntu 24.04.3 LTS

Disk (1 TB) details:
```
sudo lshw -class disk -class storage

description: NVMe device
product: SAMSUNG MZVL21T0HDLU-00BLL
vendor: Samsung Electronics Co Ltd
bus info: pci@0000:03:00.0
version: 6L2QGXD7
width: 64 bits
clock: 33MHz
capabilities: nvme pm msi pciexpress msix nvm_express bus_master cap_list
configuration: driver=nvme latency=0 nqn=nqn.1994-11.com.samsung:nvme:PM9A1a:M.2:S75YNF0XC05149 state=live
resources: irq:68 memory:78c00000-78c03fff
```

Databases ran in Docker with memory capped at 16G and CPUs at 8. The test runner did not have any limits imposed. 