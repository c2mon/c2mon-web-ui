package cern.c2mon.web.ui;

import cern.c2mon.client.common.tag.Tag;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    ApplicationContext context = new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF).sources(Application.class).run(args);

    ElasticsearchService elasticsearchService = context.getBean(ElasticsearchService.class);

    List<Tag> tags = (List<Tag>) elasticsearchService.findByMetadata("location", "513");
    System.out.println(tags);
  }
}
