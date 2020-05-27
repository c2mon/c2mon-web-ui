package cern.c2mon.web.ui.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
public class ForceTomcatConfig {
	@Bean
	ServletWebServerFactoryAutoConfiguration tomcat() {
         return new ServletWebServerFactoryAutoConfiguration();
    }
}
