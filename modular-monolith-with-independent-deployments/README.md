# Modular Monolith with Independently Deployable Modules

Let's face it, a modular monolith is great - we can have almost all the microservices benefits without their complexity. 
One major, often crucial thing that is lacking and is very useful and comes for free with microservices are independent deployments.

Can we add this to a modular monolith and have independent deployments of modular monolith modules?

It turns out that yes, we can!

## What do we have here

We have a modular monolith written in Java 21, using Spring Boot 3, exposing REST API.

It has three, independently deployable, domain modules:

* budget
* campaign
* inventory

Each of them is a separate maven artifact, versioned independently, although we will just use snapshot versioning to
make it simpler (allowing to override packages with the same version).

Then, we also have:
* commons/spring-parent - parent with a common stuff, like dependencies/their versions and some build-related config
* commons/contracts - various interfaces and models for module-to-module communication
* application - module that depends on all other modules and built our monolith as a whole, ready to be deployed
  application

In ops, we have various scripts to simplify operations:
* nexus folder allows us to run nexus locally (private maven repo among other things), in docker, where we will publish
  maven artifacts
* postgres folder has scripts to run postgres instance with three databases prepared, one for every module
* DockerfileBuild is a docker file used by scripts to build our monolith in docker, to make it more reliable and
  repeatable across examples (we also have a few docker volumes to make consecutive builds faster)
* DockerfileRun uses artifact (modular-monolith-app.jar) produced by DockerfileBuild to run the monolith in Docker

## How to run the example?

Dependencies:
* Java 21
* Maven 3.8+
* Docker
* Bash shell

### Prepare Nexus

First, we need to have a nexus repo to upload maven artifact. Go to ops/nexus and run:
```
bash build_and_run.bash
```
...after build and initialization we can go to `localhost:8081` in the browser and see Nexus UI.

Now, we need to set up new admin password. Run:
```
docker ps
CONTAINER ID   IMAGE     COMMAND                  CREATED          STATUS          PORTS                                       NAMES
d61add68a32b   nexus     "/opt/sonatype/nexus…"   10 minutes ago   Up 10 minutes   0.0.0.0:8081->8081/tcp, :::8081->8081/tcp   nexus
docker exec -it nexus cat /nexus-data/admin.password
a3d1d264-bef0-4ae8-920a-f6fcedecede6
```

...we now have admin password. Click sign-in in the browser UI and change it to just `admin` so that our examples can
work.

After clicking enable/disable anonymous access, doesn't matter for our example - pick any, we have ready to be used
nexus repo.

We will update our artifacts as snapshots to make development and an example faster. We will find them
here: http://localhost:8081/#browse/browse:maven-snapshots

### Upload maven artifacts

DockerfileBuild and related `build_modular_monolith_in_docker.bash` has only sources of application module that
aggregated all other modules.
Rest of the modules are taken by it from our Nexus repo, so we need to upload them first!

We will upload those artifacts from our local machine see we need to add:
```
<servers>
  <server>
    <id>internal.repo</id>
    <username>admin</username>
    <password>admin</password>
  </server>
</servers>
```

...to the `$HOME/.m2/settings.xml`. If you don't have one, just copy maven_settings.xml (as settings.xml) from the repo there.

Go to ops and run:
```
bash upload_all_modules_to_nexus_repo.bash
```

After some time we should have all 5 artifacts available under http://localhost:8081/#browse/browse:maven-snapshots.

### Build and run Modular Monolith

Our glorious monolith also needs a postgres database. In a separate terminal go to ops/postgres and simply run:
```
bash build_and_run.bash
```

We should have postgres with 3 databases (budget, campaign and inventory) running on port 5555.

Now, we can build and run our glorious monolith!

Just go to ops again and run:
```
bash build_and_run_modular_monolith_in_docker.bash
```

This will:
1. Build our monolith in docker, downloading all dependencies into a separate volume. Running it first time can take a few minutes, be patient, consecutive builds will be faster!
2. Run monolith in docker based on the jar created in the first step

After that, we can go to http://localhost:8080/swagger-ui/index.html and see our beautiful api, coming from three
independent modules!

## Let's simulate independent work of two team members

As said, we have three domain modules. Let's see how we can work and deploy changes independently, even though, it's just
a monolith!

### Budget change

Run:
```
git checkout -b team-member-budget-change
```

Go to modules/budget and implement some visible change in the API. We can for example add `/budgets` endpoint that will return all budgets.
It can be mocked for now, if you don't want to bother with the implementation, because we will see the change in Swagger UI.
Go to the BudgetController and add:
```
@GetMapping
List<Budget> getAll() {
  return List.of();
}
```

Now, go to ops and run:

```
bash upload_module_to_nexus_repo.bash modules/budget
```

We have just uploaded new budget module to our Nexus repo!

Commit your changes and switch back to master branch.

Let's build and deploy our monolith again (from ops folder):

```
bash build_and_run_modular_monolith_in_docker.bash
```

...we will se new `/budgets` endpoint in the swagger UI ;)

Remember that we are on the master branch again, where we don't know anything about *team-member-budget-change*!

Our monolith just takes modules from our internal maven repo, which allows us to deploy them independently.

### Inventory change

We are on master branch right now, our modular monolith is running with `team-member-budget-change`, but we don't know
anything about it, as far as source code is concerned.

Let's make an independent change in the inventory module:
```
git checkout -b team-member-inventory-change
```

Go to modules/inventory and implement some visible change in the API. We can for example add `/inventories` endpoint that will return all inventories.
It can be mocked for now, if you don't want to bother with the implementation, because we will see the change in Swagger UI.
Go to the InventoryController and add:
```
@GetMapping
List<Inventory> getAll() {
  return List.of();
}
```

Now, go to ops and run:
```
bash upload_module_to_nexus_repo.bash modules/inventory
```

We have just uploaded new inventory module to our Nexus repo!

Commit the changes and switch back to master branch.

Let's build and deploy our monolith again (from ops folder):
```
bash build_and_run_modular_monolith_in_docker.bash
```

...we will see new `/inventories` endpoint in the swagger UI ;)

But, but! We will also see the previous change of adding `/budgets` endpoint by the `team-member-budget-change`!
Thus, we have deployed changes of our *inventory* module, without affecting the work of other team member that works
on `budget` module!

Here we have it, Modular Monolith with Independently Deployable Modules.

## Caveats and some things to keep in mind

* Our modules will live in the application module together and have all Beans from other modules in memory! That's why we need to name
  repeatable Bean types (like JdbcTemplate for example) properly to avoid name conflicts and configuration bugs. That's
  probably one of the biggest drawbacks of this approach, we can make mistakes here, beware!
* commons/contracts is a set of contracts between our modules. Remember about changing it in a backward compatible way or
  in a few modules together (it has the same/similar issues as REST API versioning really)
* Here, I decided that every module has its own database, so we can't have reliable transactions across modules. For that
  we need to use various patterns from microservices (Outbox pattern mostly), but it is also perfectly fine to share a
  database across modules as long as there is a clear data ownership (tables) separation, based on either having
  separate db schemas or just having a particular table naming convention