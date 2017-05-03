package cern.c2mon.web.ui.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(EmbeddedServletContainerAutoConfiguration.class)
public class ForceTomcatConfig {
	@Bean
    TomcatEmbeddedServletContainerFactory tomcat() {
         return new TomcatEmbeddedServletContainerFactory();
    }
}
