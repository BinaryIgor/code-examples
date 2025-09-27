* as much reuse as possible
* components do not make network calls; they support injecting as much data externally as possible
* avoid ids when to necessary:
    * bubbles: true for events
    * this.querySelector('[data-{attr}]') pattern for partially updateable/configureable elements