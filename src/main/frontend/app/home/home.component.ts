import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions} from 'angular';

var Highcharts = require('highcharts/highstock');

export class HomeComponent implements IComponentOptions {
  public templateUrl: string = '/home/home.component.html';
  public controller: Function = HomeController;
}

class HomeController {
  public static $inject: string[] = ['TagService'];

  public tag: Tag;
  public tagHistory: Tag[];

  public constructor(private tagService: TagService) {
    this.tag = {
      id: 1234,
      name: "cpu.loadavg",
      description: "",
      value: 25.23
    };
  }

  public onTagSelected(tag: Tag) {
    this.tagService.getHistory(tag).then((history: Tag[]) => {
      this.tagHistory = history;

      let data: any[] = []

      history.forEach(function (record: any) {
        data.push([record.timestamp, record.value]);

        //x.push(hit._source.serverTimestamp);
        //if (self.tag && (self.tag.dataType === 'Integer' || self.tag.dataType === 'Float' || self.tag.dataType === 'Double')) {
        //  y.push(hit._source.valueNumeric);
        //}
        //else if (self.tag && self.tag.dataType === 'Boolean') {
        //  y.push(hit._source.valueBoolean);
        //}

        //self.tagHistory.push(hit._source);
      });

      // create the chart
      Highcharts.chart('chart', {
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
    })
  }

  public afterSetExtremes(e) {
    //var chart = $('#container').highcharts();
    //chart.showLoading('Loading data from server...');
    //loadData(Math.round(e.min), Math.round(e.max)).then(function (data) {
    //  chart.series[0].setData(data);
    //  chart.hideLoading();
    //});
  }

  public findTags(query: string) {
    return this.tagService.findTags(query);
  }
}
