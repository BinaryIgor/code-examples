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
```