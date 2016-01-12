/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.configviewer.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.jfree.data.category.CategoryDataset;

import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreeBarChart;

public class BarChartSerializer extends SerializerBase<JFreeBarChart> {

  /**
   * Constructor.
   */
  public BarChartSerializer() {
    super(JFreeBarChart.class);
  }

  /**
   * Example JSON output:
   *
   * <p>
   * {
   *   "title" : "Graph Title",
   *   "description" : "Graph Description"
   *   "xAxis": {
   *     "label" : "Category of thing",
   *     "data" : ["1", "2", "3", "4"]
   *   },
   *   "yAxis": {
   *     "label" : "Number of things",
   *     "data" : ["41", "56", "34", "44"]
   *   }
   * }
   * </p>
   *
   * @see org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object
   *      , org.codehaus.jackson.JsonGenerator,
   *      org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(JFreeBarChart value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();

    JFreeBarChart chart = value;
    generator.writeStringField("title", chart.getTitle());
    generator.writeStringField("subtitle", chart.getParagraphTitle());
    generator.writeStringField("description", chart.getGraphDescription());

    generator.writeFieldName("xaxis");
    generator.writeStartObject();
    generator.writeStringField("label", value.getDomainAxis());
    generator.writeFieldName("data");
    generator.writeStartArray();
    for (Object object : chart.getJFreeChart().getCategoryPlot().getDataset().getColumnKeys()) {
      generator.writeString(object.toString());
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeFieldName("yaxis");
    generator.writeStartObject();
    generator.writeStringField("label", value.getRangeAxis());

    generator.writeFieldName("data");
    generator.writeStartArray();
    CategoryDataset dataset = chart.getJFreeChart().getCategoryPlot().getDataset();
    for (int r = 0; r < dataset.getRowCount(); r++) {
      for (int c = 0; c < dataset.getColumnCount(); c++) {
        Number number = dataset.getValue(r, c);
        generator.writeNumber(number.doubleValue());
      }
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeEndObject();
  }
}
