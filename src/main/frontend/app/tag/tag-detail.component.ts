import {Tag} from './tag';
import {TagService} from './tag.service';
import {IComponentOptions} from 'angular';
import 'moment';
import Moment = moment.Moment;

var Highcharts = require('highcharts/highstock');

export class TagDetailComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-detail.component.html';
  public controller: Function = TagDetailController;
  public bindings: any = {
    tag: '=',
  };
}

class TagDetailController {
  public static $inject: string[] = ['TagService'];

  public tag: Tag;
  public history: Tag[];
  public chart: Highcharts;

  public constructor(private tagService: TagService) {
    // Ask for one month by default
    let max: Moment = moment();
    let min: Moment = moment(max).subtract(1, 'month');

    this.tagService.getHistory(this.tag, min.unix(), max.unix()).then((history: Tag[]) => {
      this.history = history;
      this.createTagHistoryChart(this.history);
    });
  }

  public createTagHistoryChart(data) {
    this.chart = Highcharts.stockChart('chart', {
      chart: {zoomType: 'x'},
      navigator: {
        adaptToUpdatedData: false,
        series: {
          data: data
        }
      },
      scrollbar: {liveRedraw: false},
      rangeSelector: {
        buttons: [
          {type: 'minute', count: 1, text: '1m'}, {type: 'hour', count: 1, text: '1h'},
          {type: 'day', count: 1, text: '1d'}, {type: 'month', count: 1, text: '1m'},
          {type: 'year', count: 1, text: '1y'}, {type: 'all', text: 'All'}],
        inputEnabled: false,
        selected: 5
      },
      xAxis: {
        events: {
          afterSetExtremes: this.afterSetExtremes
        },
        minRange: 1000 // one second
      },
      yAxis: {floor: 0},
      series: [{
        data: data,
        dataGrouping: {enabled: false},
        marker: {enabled: true, radius: 2},
        connectNulls: true
      }]
    });
  }

  public afterSetExtremes = (event: any) => {
    this.chart.showLoading('Loading data from server...');

    this.tagService.getHistory(this.tag, Math.round(event.min), Math.round(event.max)).then((history: Tag[]) => {
      this.history = history;
      this.chart.series[0].setData(history);
      this.chart.hideLoading();
    });
  };

  public formatTimestamp(timestamp: number): string {
    return moment(timestamp).format();
  }
}