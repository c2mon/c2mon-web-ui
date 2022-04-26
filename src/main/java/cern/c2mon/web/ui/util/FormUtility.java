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
package cern.c2mon.web.ui.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to ease preparation of data for the MVC model
 */
public final class FormUtility {

  /**
   * Gets a map of values to include later in the MVC model processed by a jsp
   * @param title tag title of the form displayed on the jsp page (different for datatag, alarm, command)
   * @param instruction description of the user action displayed on the jsp page
   * @param formSubmitUrl url to which the form should be submitted
   * @param formTagValue previous value of a tag (datatag, alarm, command) entered in the form, that should be displayed in the form
   * @return a map of values ready to be used in the MVC model
   */


  public static Map<String, String> getFormModel(final String title, final String instruction, final String formSubmitUrl, final String formTagValue, final String formTagPlaceHolder, final String tagDataUrl) {
    Map<String, String> model = new HashMap<String, String>();
    model.put("title", title);
    model.put("instruction", instruction);
    model.put("formSubmitUrl", formSubmitUrl);
    model.put("formTagValue", formTagValue);
    model.put("formTagPlaceHolder", formTagPlaceHolder);
    model.put("tagDataUrl", tagDataUrl);
    return model;
  }

  public static Map<String, String> getFormModel(final String title, final String formSubmitUrl, final String formTagValue, final String formTagPlaceHolder, final String tagDataUrl) {
    Map<String, String> model = new HashMap<String, String>();
    model.put("title", title);
    model.put("formSubmitUrl", formSubmitUrl);
    model.put("formTagValue", formTagValue);
    model.put("formTagPlaceHolder", formTagPlaceHolder);
    model.put("tagDataUrl", tagDataUrl);
    return model;
  }

  public static String getHeader(String basePath) {

    String header = "<!DOCTYPE html>"
            + "<html lang=\"en\">"
            + "<head>"
            + "<meta charset=\"utf-8\">"
            + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "  <title>Configuration viewer</title>"
            + "  <link rel=\"stylesheet\" type=\"text/css\" href=\"" + basePath + "/css/bootstrap/bootstrap.css\"></link>"
            + "  <link rel=\"stylesheet\" type=\"text/css\" href=\"" + basePath + "/css/common.css\"></link>"
            + "  <script type=\"text/javascript\" src=\"" + basePath + "/js/jquery/jquery.js\"></script>"
            + "  <script type=\"text/javascript\" src=\"" + basePath + "/js/bootstrap/bootstrap.js\"></script>"
            + "</head>"
            + "<body>"
            + "<div class=\"container-fluid\">";

    return header;
  }

  public static String getFooter() {

    String footer = "</div>" + "</body>" + "</html>";

    return footer;
  }


  /**
   * Utility class never needs instantiating.
   */
  private FormUtility() { };

}
