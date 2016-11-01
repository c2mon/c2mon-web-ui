package cern.c2mon.web.ui;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.TagService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.AvgAggregation;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ElasticsearchService {

  private JestClient client;

  @Autowired
  private TagService tagService;

  @PostConstruct
  public void init() {
    JestClientFactory factory = new JestClientFactory();
    factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
        .multiThreaded(true)
        .build());
    client = factory.getObject();
  }

  /**
   * Retrieve aggregated history for the given tag for the specified time period.
   * <p>
   * A suitable average aggregation interval is automatically calculated.
   *
   * @param id  the id of the tag
   * @param min the beginning of the requested date range
   * @param max the end of the requested date range
   * @return list of [timestamp, value] pairs
   */
  public List<Object[]> getHistory(Long id, Long min, Long max) {
    List<Object[]> results = new ArrayList<>();

    // Figure out the right interval
    String interval = getInterval(min, max);
    log.info("using interval: " + interval);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(termQuery("id", id))
        .size(1)
        .aggregation(AggregationBuilders.dateHistogram("events-per-interval")
            .field("timestamp")
            .interval(new DateHistogramInterval(interval))
            .subAggregation(
                AggregationBuilders.avg("avg-value").field("value")
            ));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag #" + id, e);
    }

    for (DateHistogramAggregation.DateHistogram bucket : result.getAggregations().getDateHistogramAggregation("events-per-interval").getBuckets()) {
      AvgAggregation avg = bucket.getAvgAggregation("avg-value");
      results.add(new Object[]{Long.parseLong(bucket.getTimeAsString()), avg.getAvg()});
    }

    return results;
  }

  private String getInterval(Long min, Long max) {
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

    return interval;
  }

  /**
   * Get the top {@literal size} most active tags.
   *
   * @param size the number of top tags to retrieve
   * @return a list of {@link Tag} instances
   */
  public List<Tag> getTopTags(Integer size) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.aggregation(AggregationBuilders.terms("group-by-id")
        .field("id")
        .size(size));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying top most active tags", e);
    }

    tagIds.addAll(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
        .stream()
        .map(bucket -> Long.valueOf(bucket.getKey())).collect(Collectors.toList()));

    return (List<Tag>) tagService.get(tagIds);
  }

  /**
   * Find all tags by name with a given prefix.
   *
   * @param query the tag name prefix
   * @return a list of tags whose names match the given prefix
   */
  public Collection<Tag> findByName(String query) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder
        .query(prefixQuery("name", query))
        .size(0)
        .aggregation(AggregationBuilders.terms("group-by-name")
            .field("name")
            .subAggregation(
                AggregationBuilders.topHits("top")
                    .setSize(1)
                    .addSort("timestamp", SortOrder.DESC)
                    .setFetchSource("id", null)
            ));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying top most active tags", e);
    }

    for (TermsAggregation.Entry bucket : result.getAggregations().getTermsAggregation("group-by-name").getBuckets()) {
      double id = (double) bucket.getTopHitsAggregation("top").getFirstHit(Map.class).source.get("id");
      tagIds.add((long) id);
    }

    return tagService.get(tagIds);
  }

  /**
   * Find all tags containing the exact metadata key/value pair.
   *
   * @param key   the metadata key
   * @param value the metadata value
   * @return a list of tags containing the exact metadata requested
   */
  public Collection<Tag> findByMetadata(String key, String value) {
    List<Long> tagIds = new ArrayList<>();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(
        nestedQuery("metadata",
            boolQuery().must(matchQuery("metadata." + key, value))))
        .aggregation(AggregationBuilders.terms("group-by-id")
            .field("id")
            .size(0)
        );

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying top most active tags", e);
    }

    tagIds.addAll(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
        .stream()
        .map(bucket -> Long.valueOf(bucket.getKey())).collect(Collectors.toList()));

    return tagService.get(tagIds);
  }
}
