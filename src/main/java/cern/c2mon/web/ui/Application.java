package cern.c2mon.web.ui;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
	
  public static void main(String[] args) {
    new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF).sources(Application.class).run(args);
  }
}
