# Modular Monolith with Indepedent Deployments

Let's face it, modular monolith is great: we can have almost all of the microservices benefits without their complexity. One major thing that is lacking and is very useful and comes for free with miroservices are independent deployments.

Can we add this to a modular monolith and have independent deployments of modular monolith modules?

Modules:
* campaign
* inventory
* budget


Source of truth for modular monolith modules
* separate repo:
    * where to deploy?
* locally:
    * git pull
    * git checkout branch a, install module 1
    * git checkout branch b, install module 2
    * git checkout branch c, install module 3


## Some resources
* https://microservices.io/post/architecture/2023/08/20/how-modular-can-your-monolith-go-part-2.html
* https://medium.com/design-microservices-architecture-with-patterns/microservices-killer-modular-monolithic-architecture-ac83814f6862
* https://renegadeotter.com/2023/09/10/death-by-a-thousand-microservices.html
* https://www.youtube.com/watch?v=Ip9GCQ24-sQ