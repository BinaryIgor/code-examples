All tests were run on the DigitalOcean infrastructure. Specifically:
* each db ran on `c-8-intel` machine - 8 CPUs and 16 GB of memory
* same for tests - each test was run on its own `c-8-intel` machine
* OS - Ubuntu 24.04.3 LTS

DigitalOcean describes those machines here (we have premium CPU-optimized:
https://docs.digitalocean.com/products/droplets/concepts/choosing-a-plan/#shared-vs-dedicated
 
I chose premium Intel here; quoting description:
> If your workloads require guaranteed and sustained CPU performance but are not as memory-intensive, CPU-Optimized Droplets let you to minimize cost per dedicated vCPU. Backed by Intel’s Ice Lake and older processors with base clock speeds in excess of 2.6 GHz, CPU-Optimized Droplets are built for CPU-bound workloads.

And:
> CPU-Optimized Droplets with Premium CPUs have higher network throughput capabilities of up to 10 Gbps of traffic from your Droplets. This improved throughput speed lets you export your data five times faster than CPU-Optimized Droplets with Regular CPUs, which is beneficial for use cases like live streaming and analytics workloads.
>
> CPU-Optimized Droplets with Premium CPUs are also guaranteed to use NVMe SSDs and one of the latest two generations of CPUs we have. NVMe SSDs use parallelism to deliver faster disk performance than regular SSDs. Workloads that require a large number of transactions have much lower latency with NVMe SSDs.
>
> CPU-Optimized Droplets with Premium CPUs currently have third generation Intel Xeon Scalable processors. CPU-Optimized Droplets with Regular CPUs have a mix of second generation or older Intel Xeon Scalable processors.


Disk are of course virtualized; getting their details on db machines:
```
sudo lshw -class disk -class storage

  *-ide                     
       description: IDE interface
       product: 82371SB PIIX3 IDE [Natoma/Triton II]
       vendor: Intel Corporation
       physical id: 1.1
       bus info: pci@0000:00:01.1
       version: 00
       width: 32 bits
       clock: 33MHz
       capabilities: ide isa_compat_mode bus_master
       configuration: driver=ata_piix latency=0
       resources: irq:0 ioport:1f0(size=8) ioport:3f6 ioport:170(size=8) ioport:376 ioport:c1e0(size=16)
  *-scsi:0
       description: SCSI storage controller
       product: Virtio SCSI
       vendor: Red Hat, Inc.
       physical id: 5
       bus info: pci@0000:00:05.0
       version: 00
       width: 64 bits
       clock: 33MHz
       capabilities: scsi msix bus_master cap_list
       configuration: driver=virtio-pci latency=0
       resources: irq:10 ioport:c100(size=64) memory:febf3000-febf3fff memory:fe80c000-fe80ffff
  *-scsi:1
       description: SCSI storage controller
       product: Virtio block device
       vendor: Red Hat, Inc.
       physical id: 6
       bus info: pci@0000:00:06.0
       version: 00
       width: 64 bits
       clock: 33MHz
       capabilities: scsi msix bus_master cap_list
       configuration: driver=virtio-pci latency=0
       resources: irq:10 ioport:c000(size=128) memory:febf4000-febf4fff memory:fe810000-fe813fff
     *-virtio4
          description: Virtual I/O device
          physical id: 0
          bus info: virtio@4
          logical name: /dev/vda
          size: 320GiB (343GB)
          capabilities: gpt-1.00 partitioned partitioned:gpt
          configuration: driver=virtio_blk guid=dcdd7cfe-dca3-4181-877e-b1dfcf70e9aa logicalsectorsize=512 sectorsize=512
  *-scsi:2
       description: SCSI storage controller
       product: Virtio block device
       vendor: Red Hat, Inc.
       physical id: 7
       bus info: pci@0000:00:07.0
       version: 00
       width: 64 bits
       clock: 33MHz
       capabilities: scsi msix bus_master cap_list
       configuration: driver=virtio-pci latency=0
       resources: irq:11 ioport:c080(size=128) memory:febf5000-febf5fff memory:fe814000-fe817fff
     *-virtio5
          description: Virtual I/O device
          physical id: 0
          bus info: virtio@5
          logical name: /dev/vdb
          size: 504KiB (516KB)
          configuration: driver=virtio_blk logicalsectorsize=512 sectorsize=512
```