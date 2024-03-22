## Small machine - 1 CPU, 2 GB of memory 

Handled 1000 requests/second with stats:
* Min: 0.001 s, Max: 0.2 s, Mean: 0.013 s
* Percentile 50 (Median): 0.009 s, Percentile 75: 0.017 s
* Percentile 90: 0.026 s, Percentile 95: 0.034 s, Percentile 99: 0.099 s

Failed at 4000 requests/second: we had 50% + of timeouts with 4.413 s Mean and 5.0 s Median

## Medium machine - 2 CPUs, 4 GB of memory

Handled 1000 requests/second even better with stats:
* Min: 0.001 s, Max: 0.135 s, Mean: 0.004 s 
* Percentile 50 (Median): 0.003 s, Percentile 75: 0.005 s
* Percentile 90: 0.007 s, Percentile 95: 0.01 s, Percentile 99: 0.023 s

Failed at 4000 requests/second, but we had only 10% + of timeouts with 1.97 s Mean and 1.038 s Median

## Large machine - 4 CPUs (dedicated), 8 GB of memory

Handled 4000 requests/second with stats:
* Min: 0.0 s (less than 1 ms), Max: 1.05 s, Mean: 0.058 s
* Percentile 50 (Median): 0.005 s, Percentile 75: 0.053 s
* Percentile 90: 0.124 s, Percentile 95: 0.353 s, Percentile 99: 0.746 s

