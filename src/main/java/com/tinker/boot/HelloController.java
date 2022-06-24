package com.tinker.boot;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class HelloController {

	private final static String SECRET_USERNAME_ENV_VAR = "SECRET_USERNAME";
	private final static String SECRET_PASSWORD_ENV_VAR = "SECRET_PASSWORD";
	private final static String TEST_ENV_VAR = "TEST_ENV_VAR";

	@Autowired
	private CheckPropertiesExample checkPropertiesExample;

	/**
	 * Now map for a REST GET and also time and count the calls.
	 */
	@GetMapping("/")
	@Counted(value = "greeting.count", description = "Number of times GET request is made")
	@Timed(value = "greeting.time", description = "Time taken to return greeting")
	public String index() throws InterruptedException
	{
		var currentDir = System.getProperty("user.dir");

		var envVarValues = List.of(TEST_ENV_VAR, SECRET_USERNAME_ENV_VAR, SECRET_PASSWORD_ENV_VAR)
						.stream()
						.map(System::getenv)
						.collect(Collectors.toList());

		Thread.sleep(new Random().nextInt(1000));
		return currentDir +": Greetings from Spring Boot! Env Vars are " + envVarValues + " from properties " + checkPropertiesExample.getValues();
	}
}
