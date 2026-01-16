All tests were run on the DigitalOcean infrastructure. Specifically:
* each db ran on the `c-8-intel` machine - 8 CPUs and 16 GB of memory
* same for tests - each test was run on its own `c-8-intel` machine
* OS - Ubuntu 24.04.3 LTS

DigitalOcean describes these machines here (we have premium CPU-optimized):
https://docs.digitalocean.com/products/droplets/concepts/choosing-a-plan/#shared-vs-dedicated
 
I chose premium Intel; quoting description:
> If your workloads require guaranteed and sustained CPU performance but are not as memory-intensive, CPU-Optimized Droplets let you to minimize cost per dedicated vCPU. Backed by Intel’s Ice Lake and older processors with base clock speeds in excess of 2.6 GHz, CPU-Optimized Droplets are built for CPU-bound workloads.

And:
> CPU-Optimized Droplets with Premium CPUs have higher network throughput capabilities of up to 10 Gbps of traffic from your Droplets. This improved throughput speed lets you export your data five times faster than CPU-Optimized Droplets with Regular CPUs, which is beneficial for use cases like live streaming and analytics workloads.
>
> CPU-Optimized Droplets with Premium CPUs are also guaranteed to use NVMe SSDs and one of the latest two generations of CPUs we have. NVMe SSDs use parallelism to deliver faster disk performance than regular SSDs. Workloads that require a large number of transactions have much lower latency with NVMe SSDs.
>
> CPU-Optimized Droplets with Premium CPUs currently have third generation Intel Xeon Scalable processors. CPU-Optimized Droplets with Regular CPUs have a mix of second generation or older Intel Xeon Scalable processors.


And to be exact, I managed to get this from the machine:
```
cat /proc/cpuinfo

processor	: 0
vendor_id	: GenuineIntel
cpu family	: 6
model		: 106
model name	: Intel(R) Xeon(R) Platinum 8358 CPU @ 2.60GHz
stepping	: 6
microcode	: 0x1
cpu MHz		: 2600.000
cache size	: 4096 KB
physical id	: 0
siblings	: 8
core id		: 0
cpu cores	: 8
apicid		: 0
initial apicid	: 0
fpu		: yes
fpu_exception	: yes
cpuid level	: 13
wp		: yes
flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ht syscall nx pdpe1gb rdtscp lm constant_tsc arch_perfmon rep_good nopl xtopology cpuid tsc_known_freq pni pclmulqdq vmx ssse3 fma cx16 pcid sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer aes xsave avx f16c rdrand hypervisor lahf_lm abm 3dnowprefetch cpuid_fault pti ssbd ibrs ibpb tpr_shadow flexpriority ept vpid ept_ad fsgsbase bmi1 avx2 smep bmi2 erms invpcid avx512f avx512dq rdseed adx smap clflushopt clwb avx512cd avx512bw avx512vl xsaveopt xsavec xgetbv1 wbnoinvd arat vnmi avx512vbmi umip pku ospke avx512_vbmi2 gfni vaes vpclmulqdq avx512_vnni avx512_bitalg avx512_vpopcntdq
vmx flags	: vnmi preemption_timer posted_intr invvpid ept_x_only ept_ad ept_1gb flexpriority apicv tsc_offset vtpr mtf vapic ept vpid unrestricted_guest vapic_reg vid shadow_vmcs
bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf mds swapgs itlb_multihit mmio_stale_data gds bhi
bogomips	: 5200.00
clflush size	: 64
cache_alignment	: 64
address sizes	: 40 bits physical, 48 bits virtual
power management:
```

Disks are of course virtualized; getting their details on db machines:
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
       resources: irq:0 ioport:1f0(size=8) ioport:3f6 ioport:170(size=8) ioport:376 ioport:c220(size=16)
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
       resources: irq:10 ioport:c180(size=64) memory:febf3000-febf3fff memory:fe80c000-fe80ffff
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
          size: 100GiB (107GB)
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

For db configs, check out `build_and_package.bash` scripts, located in each db dir.