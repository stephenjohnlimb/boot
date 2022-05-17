## Exposing Metrics for Prometheus
In general, whether you are doing micro-services - you will find issues in
production; and you'll be asking yourself _what's going on?_

By using a number of carefully placed measurement points you can expose some
internal working characteristics.
For example:
- Number of calls
- Duration of a call
- 
### Dependencies
The following dependencies need to be added in to the pom.xml first.

```
<dependency>
    <groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
	<scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Exposing the end point

Add the following to the `application.properties`; this will enable
Spring to server out `health`, `info` and the full `prometheus` metrics.
Later on I'll detail how to set up a Prometheus server to hold those metrics.  
```
management.endpoints.web.exposure.include=health,info,prometheus
```

### What about the Spring code?
I've added in some _beans_ that enable the aspects for the `micrometer`.
There are alternatives to this approach that use annotation like `@EnablePrometheusMetrics`.

```
@SpringBootApplication
public class BootApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(BootApplication.class, args);		
	}

	/**
	 * We add this in so that timing can be added.
	 */
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	/**
	 * We add this in so that counted items can be used.
	 */
	@Bean
	public CountedAspect countedAspect(MeterRegistry registry) {
		return new CountedAspect(registry);
	}
}
```

Now expose some interesting information from one of the controllers.
```
@RestController
public class HelloController {
	/**
	 * Now map for a REST GET and also time and count the calls.
	 */
	@GetMapping("/")
	@Counted(value = "greeting.count", description = "Number of times GET request is made")
	@Timed(value = "greeting.time", description = "Time taken to return greeting")
	public String index() {
		return "Greetings from Spring Boot!";
	}
}
```

I've used an annotation based approach here, just for capturing the number of hits on the method
and also how long the method took to run.

There are [other metric types](https://prometheus.io/docs/concepts/metric_types/) available, such as gauge for example.
