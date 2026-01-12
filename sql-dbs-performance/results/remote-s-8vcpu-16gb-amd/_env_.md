All tests were run on the DigitalOcean infrastructure. Specifically:
* each db ran on `s-8vcpu-16gb-amd` machine - 8 CPUs and 16 GB of memory
* same for tests - each test was run on its own `s-8vcpu-16gb-amd` machine
* OS - Ubuntu 24.04.3 LTS

DigitalOcean describes those machines here:
https://www.digitalocean.com/blog/premium-droplets-intel-cascade-lake-amd-epyc-rome
 
I chose premium AMD here; quoting the article:
> AMD Premium Droplets currently run 2nd Generation AMD EPYC™ processors, based upon the “Zen 2” architecture, which feature what’s commonly referred to as the Rome architecture. These AMD EPYC CPUs operate at a base frequency of 2.0 GHz and a max turbo frequency of 3.35 GHz.

And:
> One important aspect of our new Premium Droplets is their enhanced memory performance, which can play a significant factor in workloads like in-memory databases and server-side caches for web apps. Our Premium AMD Droplets feature memory frequency of 3200 MHz, and Premium Intel 2933 MHz.
>
> As for the disk aspect of your Premium Droplets – NVMe SSDs take advantage of parallelism to deliver disk performance that can be an order of magnitude faster than regular SSDs. If you’re running workloads that require a large number of transactions, you’ll achieve much lower latency with NVMe SSDs.


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