# Metrics and Logs Collector

Simple Python app that collects data from Docker Containers. It then exposes their metrics to Prometheus and saves logs to a local file system, limiting their size and rotating files, if neccessary.

## Useful

Get all Prometheus metrics:
```
{__name__=~".+"}
```