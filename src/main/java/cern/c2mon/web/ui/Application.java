package cern.c2mon.web.ui;

import java.util.List;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.service.TagService;

/**
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    ApplicationContext context = new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF).sources(Application.class).run(args);

    ElasticsearchService elasticsearchService = context.getBean(ElasticsearchService.class);
    TagService tagService = context.getBean(TagService.class);

    List<Tag> tags = (List<Tag>) tagService.findByMetadata("location", "513");
    System.out.println(tags);
  }
}
