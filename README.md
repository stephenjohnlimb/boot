## Spring Boot Example
Well a bit more than just Spring Boot, I've pulled a number of different things I'm interested into this project.

### Purpose
Just a quick check of the processes in getting a Spring Boot app up running and pushed to gitHub via IntelliJ.
Now adding in AVRO and coackroachdb.

But I'm also interested in monitoring and metrics so will employ Prometheus (as a store of metrics) and Grafana
for visualisation and alerts.

I've never really used cucumber that much, so thought I'd bundle that in.
It wasn't too bad; the documentation is detailed but just getting started was not that obvious.
So I've added a little primer [here](Cucumber.md) and included some code examples.

Later I'll add in some Kafka and maybe also wrap the Spring Boot app up in a docker image.

This repo - contains a number of code/configuration examples from a variety of sources. I've just
pulled out the bits I need all into one place.

So here are the credits (if I've missed anyone let me know; and I'll add the attribution).

- [Spring](https://start.spring.io/)
- [AVRO](https://avro.apache.org/docs/current/gettingstartedjava.html)
- [CockroachDB](https://www.cockroachlabs.com/docs/cockroachcloud/quickstart?filters=local)
- [Spring ORM/CockroachDB](https://www.cockroachlabs.com/docs/stable/example-apps.html)
- [Prometheus](https://prometheus.io/docs/instrumenting/clientlibs/)
- [TutorialWorks](https://www.tutorialworks.com/spring-boot-prometheus-micrometer/)
- [Robin Hillier](https://medium.com/thg-tech-blog/removing-cross-cutting-concerns-with-micrometer-and-spring-aop-916a5602770f)
- [Grafana](https://grafana.com/docs)

### AVRO
Basically AVRO is just a mechanism for defining structured data, see
[user.avsc](src/main/avro/user.avsc) for a simple example of this.
It is typically used when you really need to establish and control a formal set of data structures between
two or more services. The wire-format is defined to be compact and technology neutral
(meaning you can exchange structured data between different programming languages).

One of the main advantages of publishing a 'specification' of your data structure is to employ
[PACT](https://pact.io/) this formalises the relationship between the _provider_ and the _consumer_ of the
of data that must conform to that specification.

### Spring
Used [Spring initializr](https://start.spring.io/) to create a simple app, could have used a maven archetype, or just done it all by hand.

### Add in some REST
I've added a single **Controller** that just returns the standard "Hello, world". The point of this project is **not** to
create lots of application functionality; but to touch lots of different technologies. Then actually hook them all together,
try them all out. So it is 'full stack' in terms of technology - I may even include some _Vue.js_ later.

You can now run `BootApplication` and then use a browser to access `http://localhost:8080/` where you
will see the response `Greetings from Spring Boot!`

### Exposing metrics
I've detailed the [steps](Metrics.md) to expose metrics out to Prometheus. This includes
additional dependencies and what you need to do in your java code to expose metrics.

With the `BootApplication` still running you can use a browser with the following URLS:
- http://localhost:8080/actuator/health - {"status":"UP"}
- http://localhost:8080/actuator/info - {}
- http://localhost:8080/actuator/prometheus - { LOADS OF METRICS }

But specifically in the prometheus metrics you should be able to see:
```
greeting_count_total{class="com.tinker.boot.HelloController",exception="none",method="index",result="success",} 1.0
```
If you now refresh the URL 'http://localhost:8080/' a few times you'll see the counter increase.

### Setup of a Prometheus Server

While the metrics have been exposed; and are now available, we actually need a persistent store and
Prometheus server to pull these, store them and make them available to something like Grafana.

[Download Prometheus](https://prometheus.io/download/) and install on your local machine (note AWS has a managed service for Prometheus).

So that we can see and check our exposed metrics you'll need to edit the prometheus.yml configuration file as follows:
```
# my global config
global:
  scrape_interval: 15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["localhost:9090"]

  - job_name: "spring boot"
    metrics_path: "/actuator/prometheus"
    scrape_interval: 5s
    static_configs:
      - targets: ["localhost:8080"]
```

All I've done here is; add in an additional **job** called "spring boot" with the relevant details.
This basically configures prometheus to poll http://localhost:8080/actuator/prometheus every 5 seconds
and pull back all the metrics.

You can check Prometheus on 'http://localhost:9090' from there it is possible to navigate to
'http://localhost:9090/targets' and there you will see the 'spring boot' app - assuming you have it running.

You will notice that if you stop and start the spring boot app; Prometheus will notice and alter the status of that application to **DOWN**.

It is possible to use simple graphs and also do an alert with Prometheus.
See 'http://localhost:9090/graph?g0.expr=greeting_time_seconds_max&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h' for an example
of a graph. But I'll move on to Grafana next and use that for graphs and alerts.

### Setup of a Grafana Server
[Download Grafana](https://grafana.com/docs/grafana/latest/installation/) and follow the basic installation instructions.
(note AWS also has a managed service for Grafana).
Copy `sample.ini` to `custom.ini` in the `grafana\conf` directory and alter the port to use to be `http_port = 9092`.
The default username and password is `admin` and `admin` so you can alter these; once you have logged in.

Now from a powershell (Windows) you can start `Grafana` with the command:
` ./bin/grafana-server`; then use a browser to go to 'http://localhost:9092'.
Now you can configure up what you want.
- Go to configuration and add a Prometheus datasource
- Use http://localhost:9090 for that data source
- Now follow the great documentation on [Grafana](https://grafana.com/tutorials/grafana-fundamentals/?utm_source=grafana_gettingstarted)

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

