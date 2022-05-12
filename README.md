## Spring Boot Example
### Purpose
Just a quick check of the processes in getting a Spring Boot app up running and pushed to gitHub via IntelliJ.
Now adding in AVRO and coackroachdb. Later I'll add in some Kafka and maybe also wrap the Spring Boot app up in
a docker image.

### Spring
Used [Spring initializr](https://start.spring.io/) to create a simple app, could have used a maven archetype, or just done it all by hand.

### Add in some REST
The next plan is to add in some simple REST and then maybe employ CockroachDb to try that out.

### Planning to use CockroachDB
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

### Java ORM access to CockroachDB
