/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.web.ui;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import lombok.Data;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class ProgressUpdateListener implements ClientRequestReportListener {

  private ProgressUpdate progress = new ProgressUpdate();

  @Override
  public void onProgressReportReceived(ClientRequestProgressReport progressReport) {
    progress.setAction(progressReport.getProgressDescription());

    if (progressReport.getTotalProgressParts() > 0) {
      progress.setProgress((100 * progressReport.getCurrentProgressPart()) / progressReport.getTotalProgressParts());
    } else {
      progress.setProgress(100);
    }
  }

  @Override
  public void onErrorReportReceived(ClientRequestErrorReport errorReport) {
    //errorReport.
  }
}
