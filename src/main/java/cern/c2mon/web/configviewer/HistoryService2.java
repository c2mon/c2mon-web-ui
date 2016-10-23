package cern.c2mon.web.configviewer;

import cern.c2mon.client.common.tag.Tag;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

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
public class HistoryService2 {

  public List<Map<String, Object>> get(Long id) {
    TransportClient client;
    Settings settings = Settings.settingsBuilder().put("cluster.name", "c2mon").build();

    try {
      client = TransportClient.builder().settings(settings).build()
          .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
    } catch (UnknownHostException e) {
      throw new RuntimeException("Could not connect to Elasticsearch");
    }

    SearchHit[] hits = client.prepareSearch("_all").setQuery(termQuery("id", id)).setSize(100).execute().actionGet().getHits().getHits();
    List<Map<String, Object>> results = new ArrayList<>();

    for (SearchHit hit : hits) {
      results.add(hit.getSource());
    }

    return results;
  }
}
