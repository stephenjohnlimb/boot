package com.tinker.boot;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class HelloController {
	/**
	 * Now map for a REST GET and also time and count the calls.
	 */
	@GetMapping("/")
	@Counted(value = "greeting.count", description = "Number of times GET request is made")
	@Timed(value = "greeting.time", description = "Time taken to return greeting")
	public String index() throws InterruptedException
	{
		var injectedByEnvironment = System.getenv("TEST_ENV_VAR");
		Thread.sleep(new Random().nextInt(1000));
		return "Greetings from Spring Boot! Checking an env var is [" + injectedByEnvironment + "]";
	}
}
