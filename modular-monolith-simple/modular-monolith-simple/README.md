# Modular Monolith - Simple

Simple version of [Modular Monolith with Independent Deployments](https://github.com/BinaryIgor/code-examples/tree/master/modular-monolith-with-independent-deployments).

It has only one database, each module has its own schema and each module is just a separate Java package. 
They communicate only through interfaces defined in *_contracts* package.

It is much simpler modular monolith structure than the previous one, but still have nice separation of modules and allows to move to more elaborate structure,
if we follow a few simple rules:
* modules should communicate only through *_contracts* package, they can't import each other code
* database schemas should be independent of each other, there shouldn't be foreign keys between schemas, for example
* we should try to limit, or avoid entirely, transactions between modules, because it makes them more dependent on each other (db dependence)