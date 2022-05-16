package com.tinker.boot;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BootApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(BootApplication.class, args);


		System.out.println("BootApplication Finished");
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
