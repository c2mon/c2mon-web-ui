package cern.c2mon.web.configviewer;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.TagService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ElasticsearchService {

  private TransportClient client;

  @Autowired
  private TagService tagService;

  @PostConstruct
  public void init() {
    Settings settings = Settings.settingsBuilder().put("cluster.name", "c2mon").build();

    try {
      client = TransportClient.builder().settings(settings).build()
          .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
    } catch (UnknownHostException e) {
      throw new RuntimeException("Could not connect to Elasticsearch");
    }
  }

  public List<Object[]> getHistory(Long id, Long min, Long max) {
    List<Object[]> results = new ArrayList<>();

    // Figure out the right interval
    String interval;
    Long range = max - min;

    // One minute range loads second data
    if (range <= 60 * 1000) {
      interval = "1s";
    }
    // One hour range loads minute data
    else if (range <= 2 * 3600 * 1000) {
      interval = "1m";
    }
    // Two day range loads 10 minute data
    else if (range <= 2 * 24 * 3600 * 1000) {
      interval = "10m";
    }
    // One month range loads hourly data
    else if (range <= 31 * 24 * 3600 * 1000) {
      interval = "1h";
    }
    // One year range loads daily data
    else if (range <= 15 * 31 * 24 * 3600 * 1000) {
      interval = "1d";
    }
    // Greater range loads weekly data
    else {
      interval = "1w";
    }

    log.info("using interval: " + interval);

    AggregationBuilder aggregation = AggregationBuilders.dateHistogram("events-per-interval")
        .field("timestamp")
        .interval(new DateHistogramInterval(interval))
        .subAggregation(
            AggregationBuilders.avg("avg-value").field("value")
        );

    Histogram histogram = client.prepareSearch("_all")
        .setQuery(termQuery("id", id))
        .setSize(1)
        .addAggregation(aggregation)
        .execute().actionGet().getAggregations().get("events-per-interval");

    for (Histogram.Bucket bucket : histogram.getBuckets()) {
      Avg avg = bucket.getAggregations().get("avg-value");
      results.add(new Object[] {Long.parseLong(bucket.getKeyAsString()), avg.getValue()});
    }

    return results;
  }

  public List<Tag> getTopTags(Integer size) {
    Set<String> topTagNames = new HashSet<>(size);

    AggregationBuilder aggregation = AggregationBuilders.terms("top-tags").field("name")
        .size(size)
        .subAggregation(
            AggregationBuilders.topHits("top")
                .setSize(1)
                .addSort("timestamp", SortOrder.DESC)
                .setFetchSource(new String[]{"id"}, new String[]{})
        );

    StringTerms agg = client.prepareSearch("c2mon-tag*")
        .addAggregation(aggregation).execute().actionGet().getAggregations().get("top-tags");

    for (Terms.Bucket bucket : agg.getBuckets()) {
      String tagName = bucket.getKeyAsString();
      topTagNames.add(tagName);
    }

    return (List<Tag>) tagService.findByName(topTagNames);
  }
}
