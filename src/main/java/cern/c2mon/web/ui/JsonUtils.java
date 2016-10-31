package cern.c2mon.web.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * @author Justin Lewis Salmon
 */
public class JsonUtils {

  public static JsonNode merge(JsonNode a, JsonNode b) {
    Iterator<String> fieldNames = b.fieldNames();

    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode jsonNode = a.get(fieldName);

      if (jsonNode != null && jsonNode.isObject()) {
        merge(jsonNode, b.get(fieldName));
      } else {
        if (a instanceof ObjectNode) {
          JsonNode value = b.get(fieldName);
          ((ObjectNode) a).put(fieldName, value);
        }
      }
    }

    return a;
  }
}
