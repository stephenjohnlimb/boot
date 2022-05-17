## Spring Boot Example
### Purpose
Just a quick check of the processes in getting a Spring Boot app up running and pushed to gitHub via IntelliJ.
Now adding in AVRO and coackroachdb.

But I'm also interested in monitoring and metrics so will employ Prometheus (as a store of metrics) and Grafana
for visualisation and alerts.

Later I'll add in some Kafka and maybe also wrap the Spring Boot app up in a docker image.

### Spring
Used [Spring initializr](https://start.spring.io/) to create a simple app, could have used a maven archetype, or just done it all by hand.

### Add in some REST
I've added a single **Controller** that just returns the standard "Hello, world". The point of this project is **not** to
create lots of application functionality; but to touch lots of different technologies. Then actually hook them all together,
try them all out. So it is 'full stack' in terms of technology - I may even include some _Vue.js_ later.

### Exposing metrics
I've detailed the [steps](Metrics.md) to expose metrics out to Prometheus. This includes
additional dependencies and what you need to do in your java code to expose metrics.

### Setup of a Prometheus Server

### Using CockroachDB
Follow this link for [details on installation](CockroachDB.md) of cockroachDB.

### Java JDBC access to CockroachDB
By adding in postgres dependency (because CockroachDb is wire compatible) we can
access the database via JDBC. See com.tinker.jdbc.CockroachDirect.java as a quick example of this use.
```
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
	<version>42.3.5</version>
</dependency>
```

See [Direct JDBC access](src/main/java/com/tinker/jdbc/CockroachDirect.java)
for more details on how you can use direct JDBC access.

### Java ORM access to CockroachDB
Just as a simple separate example, I've also started using a simple Hibernate ORM with a single persistent class.
See [the Account class](src/main/java/com/tinker/orm/Account.java) and
[the ORM setup class](src/main/java/com/tinker/orm/CockroachORM.java).
Importantly this also needs [a hibernate configuration](src/main/resources/hibernate.cfg.xml).
The details of the JDB URL to connect to is pulled in from an environment variable **JDBC_DATABASE_URL**.

