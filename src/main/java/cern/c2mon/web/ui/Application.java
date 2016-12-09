package cern.c2mon.web.ui;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF).sources(Application.class).run(args);
  }
}
