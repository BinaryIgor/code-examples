<table>
<tr>
<td>

## Writes 100%

20 000 queries were executed with 808 queries/second rate (1000/s attempt), in 24.757 s. Stats:
* Min: 0.015 ms, Max: 1332.919 ms, Mean: 2.398 ms
* Percentile 50 (Median): 0.941 ms, Percentile 90: 1.788 ms
* Percentile 99: 3.899 ms, Percentile 99.9: 532.287 ms

</td>
<td>

## Reads 100%

1 000 000 queries were executed with 49 547 queries/second rate (50 000/s attempt), in 20.183 s. Stats:
* Min: 0.008 ms, Max: 107.297 ms, Mean: 0.017 ms
* Percentile 50 (Median): 0.013 ms, Percentile 90: 0.016 ms
* Percentile 99: 0.035 ms, Percentile 99.9: 0.064 ms

</td>
</tr>
<tr>
<td>

## Writes 50%, Reads 50%

40 000 queries were executed with 1586 queries/second rate (2000/s attempt), in 25.225 s. Stats:
* Min: 0.01 ms, Max: 1434.965 ms, Mean: 1.219 ms
* Percentile 50 (Median): 0.068 ms, Percentile 90: 1.646 ms
* Percentile 99: 2.309 ms, Percentile 99.9: 131.528 ms

</td>
<td>

## Writes 10%, Reads 90%

150 000 queries were executed with 7144 queries/second rate (7500/s attempt), in 20.996 s. Stats:
* Min: 0.008 ms, Max: 1134.174 ms, Mean: 0.262 ms
* Percentile 50 (Median): 0.016 ms, Percentile 90: 0.064 ms
* Percentile 99: 1.753 ms, Percentile 99.9: 19.357 ms

</td>
</tr>
</table>

*All tests were run with resources limited to 1GB of RAM and 2 CPUs in Docker, on a machine with 32 GB of RAM, Intel® Core™ i7-9750HF CPU @ 2.60GHz × 12 and Ubuntu 22 OS.
Test table had ~ 1 million records; every write modified one record, every read used an index.*