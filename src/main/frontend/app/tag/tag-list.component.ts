import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions} from 'angular';
import 'moment';

var Highcharts = require('highcharts/highstock');

export class TagListComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-list.component.html';
  public controller: Function = TagListController;
}

class TagListController {
  public static $inject: string[] = ['TagService'];

  public tag: Tag;
  public tagHistory: Tag[];
  public tagChart: Highcharts;
  public topTagNames: String[];

  public constructor(private tagService: TagService) {
    this.tagService.getTopTags(10).then((tagNames: String[]) => {
      this.topTagNames = tagNames;
    });
  }

  public onTagSelected(tag: Tag) {
    this.tagService.getHistory(tag).then((history: Tag[]) => {
      this.tagHistory = history;

      let data: any[] = [];

      history.forEach((record: any) => {
        data.push([record.timestamp, record.value]);
      });

      this.createTagHistoryChart(data);
    });
  }

  public createTagHistoryChart(data) {
    this.tagChart = Highcharts.stockChart('chart', {
      chart : { zoomType: 'x' },
      navigator : {
        adaptToUpdatedData: false,
        series : {
          data : data
        }
      },
      scrollbar: { liveRedraw: false },
      rangeSelector : {
        buttons: [{type: 'minute',count: 1, text: '1m'}, {type: 'hour', count: 1, text: '1h'},
          {type: 'day', count: 1, text: '1d'}, {type: 'month', count: 1, text: '1m'},
          {type: 'year', count: 1, text: '1y'}, {type: 'all', text: 'All'}],
        inputEnabled: false, // it supports only days
        selected : 5 // all
      },
      xAxis : {
        events : {
          afterSetExtremes : this.afterSetExtremes
        },
        minRange: 1000 // one second
      },
      yAxis: { floor: 0 },
      series : [{
        data : data,
        dataGrouping: { enabled: false },
        marker : { enabled : true, radius : 2 },
        connectNulls: true
      }]
    });
  }

  public afterSetExtremes = (event: any) => {
    this.tagChart.showLoading('Loading data from server...');
    // TODO: re-query based on event.min and event.max
    this.tagService.getHistory(this.tag).then((history: Tag[]) => {
      this.tagHistory = history;

      let data: any[] = [];

      history.forEach((record: any) => {
        data.push([record.timestamp, record.value]);
      });

      this.tagChart.series[0].setData(data);
      this.tagChart.hideLoading();
    });
  };

  public formatTimestamp(timestamp: number): string {
    return moment(timestamp).format();
  }

  public findTags(query: string) {
    return this.tagService.findTags(query + '*');
  }
}
