package com.tinker.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * The order of these properties is important.
 * So check the class path first for default properties
 * The optionally check an external file location - but don't fail if not there.
 */
@Configuration
@PropertySource("classpath:check.properties")
@PropertySource(value = "file:config/check.properties", ignoreResourceNotFound = true)
public class CheckPropertiesExample {

  @Autowired
  private Environment env;

  public List<String> getValues()
  {
    return List.of(env.getProperty("color.good"), env.getProperty("color.bad"));
  }
}
