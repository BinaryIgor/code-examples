## Small machine - 1 CPU, 1 GB of memory 

Handled 12 000 requests with 400 requests/second rate, in 30.317 s. Stats:
* Min: 0.001 s, Max: 0.12 s, Mean: 0.005 s
* Percentile 50 (Median): 0.003 s, Percentile 90: 0.01 s
* Percentile 99: 0.025 s, Percentile 99.9: 0.057 s

## Medium machine - 2 CPUs, 2 GB of memory

Handled 60 000 requests with 2000 requests/second rate, in 30.323 s. Stats:
* Min: 0.0 s (less than 1 ms), Max: 0.194 s, Mean: 0.014 s 
* Percentile 50 (Median): 0.005 s, Percentile 90: 0.041 s
* Percentile 99: 0.074 s, Percentile 99.9: 0.107 s

## Large machine - 4 CPUs, 8 GB of memory

Handled 90 000 requests with 3000 requests/second rate, in 32.535 s. Stats:
* Min: 0.0 s (less than 1 ms), Max: 2.776 s, Mean: 0.207 s
* Percentile 50 (Median): 0.143 s, Percentile 90: 0.569 s
* Percentile 99: 0.618 s, Percentile 99.9: 0.649 s

*Executed requests were ~ 10% writes, 90% reads. Test table had ~ 1.25 million records; every write modified one record, every read used an index.*