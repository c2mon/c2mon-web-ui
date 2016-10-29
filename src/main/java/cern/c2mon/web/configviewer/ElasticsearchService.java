package cern.c2mon.web.configviewer;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class ElasticsearchService {

  private TransportClient client;

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

  public List<Map<String, Object>> getHistory(Long id) {
    SearchHit[] hits = client.prepareSearch("_all")
        .setQuery(termQuery("id", id))
        .setSize(100)
        .execute().actionGet().getHits().getHits();
    List<Map<String, Object>> results = new ArrayList<>();

    for (SearchHit hit : hits) {
      results.add(hit.getSource());
    }

    return results;
  }

  public List<String> getTopTagNames(Integer size) {
    List<String> topTagNames = new ArrayList<>(size);

    AggregationBuilder aggregation = AggregationBuilders.terms("top-tags").field("name")
        .size(size)
        .subAggregation(
            AggregationBuilders.topHits("top")
                .setSize(1)
                .setFetchSource(new String[]{"id"}, new String[]{})
        );

    StringTerms agg = client.prepareSearch("c2mon-tag*")
        .addAggregation(aggregation).execute().actionGet().getAggregations().get("top-tags");

    for (Terms.Bucket bucket : agg.getBuckets()) {
      String tagName = bucket.getKeyAsString();
      topTagNames.add(tagName);
    }

    return topTagNames;
  }
}
