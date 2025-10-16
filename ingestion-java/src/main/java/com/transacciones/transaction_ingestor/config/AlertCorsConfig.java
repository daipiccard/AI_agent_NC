package com.transacciones.transaction_ingestor.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AlertCorsConfig implements WebMvcConfigurer {

  private final List<String> allowedOrigins;

  public AlertCorsConfig(@Value("${app.alerts.allowed-origins:*}") String origins) {
    List<String> parsed = Arrays.stream(origins.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
    this.allowedOrigins = parsed.isEmpty() ? Collections.singletonList("*") : parsed;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = allowedOrigins.toArray(String[]::new);
    registry.addMapping("/alerts/**")
        .allowedOrigins(origins)
        .allowedMethods("GET")
        .allowCredentials(false);
  }
}

