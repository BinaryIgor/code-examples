## HTMX Multi Page Application

* The slowest `Tasks page = (worst of: GET:js/htmx.2.0.4.min.js, GET:styles_cb92397d6961a772.css) + GET:tasks`
  * Min: 0.047 s, Max: 1.246 s, Mean: 0.074 s
  * Percentile 50 (Median): 0.053 s, Percentile 75: 0.055 s
  * **Percentile 90: 0.059 s, Percentile 99: 0.984 s**
* The fastest `Account page = (worst of: GET:js/htmx.2.0.4.min.js, GET:styles_cb92397d6961a772.css) + GET:account`
  * Min: 0.046 s, Max: 1.188 s, Mean: 0.07 s
  * Percentile 50 (Median): 0.051 s, Percentile 75: 0.054 s
  * **Percentile 90: 0.058 s, Percentile 99: 0.873 s**

## React Single Page Application

* The slowest `Tasks page = GET:/ + (worst of: GET:assets/index-Gy-0gLVz.js, GET:assets/index-BWhT5uIM.css) + GET:api/tasks`
  * Min: 0.075 s, Max: 1.718 s, Mean: 0.124 s
  * Percentile 50 (Median): 0.087 s, Percentile 75: 0.1 s
  * **Percentile 90: 0.124 s, Percentile 99: 1.249 s**
* The fastest `Projects page = GET:/ + (worst of: GET:assets/index-Gy-0gLVz.js, GET:assets/index-BWhT5uIM.css) + GET:api/projects`
  * Min: 0.074 s, Max: 1.734 s, Mean: 0.117 s
  * Percentile 50 (Median): 0.086 s, Percentile 75: 0.098 s
  * **Percentile 90: 0.124 s, Percentile 99: 0.978 s**

\
*For all cases, 2000 requests were made with 100 r/s rate. Stats represent a time needed for each page to be fully visible and functional*