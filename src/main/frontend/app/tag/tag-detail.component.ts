import {Tag} from './tag';
import {TagService} from './tag.service';
import {Process} from '../process/process';
import {ProcessService} from '../process/process.service';
import {Equipment} from '../equipment/equipment';
import {IComponentOptions, IHttpService, IScope} from 'angular';
import {IStateParamsService} from 'angular-ui-router';
import 'moment';
import Moment = moment.Moment;

var Highcharts = require('highcharts/highstock');
var Stomp = require('stompjs/lib/stomp.js').Stomp;
var SockJS = require('sockjs-client');

export class TagDetailComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-detail.component.html';
  public controller: Function = TagDetailController;
  public bindings: any = {
    tag: '='
  };
}

class TagDetailController {
  public static $inject: string[] = ['TagService', 'ProcessService', '$http', '$scope'];

  public process: Process;
  public equipment: Equipment;
  public tag: Tag;
  public history: Tag[];
  public chart: Highcharts;
  private stompClient: any;

  public constructor(private tagService: TagService, private processService: ProcessService,
                     private $http: IHttpService, private $scope: IScope) {
    processService.getProcess(this.tag.processName).then((process: Process) => {
      this.process = process;

      let equipments: any = this.process.equipmentConfigurations;

      for (let equipmentId: number in equipments) {
        let equipment: Equipment = equipments[equipmentId];

        if (equipment.name === this.tag.equipmentName) {
          this.equipment = equipment;
        }
      }
    });

    // Ask for one hour by default
    let max: Moment = moment();
    let min: Moment = moment(max).subtract(1, 'hour');

    this.tagService.getHistory(this.tag, min.valueOf(), max.valueOf()).then((history: Tag[]) => {
      this.history = history;
      this.createTagHistoryChart(this.history);
    });

    var socket = new SockJS('/websocket');
    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({}, this.onConnection);
  }

  public onConnection = (frame) => {
    console.log('Connected: ' + frame);

    var tagId = this.tag.id;
    this.stompClient.subscribe('/topic/tags/' + tagId, this.onTagUpdate);
    this.stompClient.send('/app/tags/' + tagId);
  };

  public onTagUpdate = (message) => {
    this.tag = JSON.parse(message.body);
    this.$scope.$apply();
  };

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
        selected: 1
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
      }],
      credits: {enabled: false}
    });
  }

  public afterSetExtremes = (event: any) => {
    this.chart.showLoading('Loading...');

    this.tagService.getHistory(this.tag, Math.round(event.min), Math.round(event.max)).then((history: Tag[]) => {
      this.history = history;

      this.chart.showLoading('Rendering...');
      this.chart.series[0].setData(history);
      this.chart.hideLoading();
    });
  };

  public formatTimestamp(timestamp: number): string {
    return moment(timestamp).format();
  }

  // public submit(expression: any): void {
  //   console.log(expression.expression);
  //
  //   this.$http.patch('/api/tags/' + this.tag.id + '/expressions/' + expression.name, expression.expression).then((response: any) => {
  //     console.log(response);
  //
  //     this.tagService.getTag(this.tag.name).then((tag: Tag) => {
  //       this.tag = tag;
  //     })
  //   });
  // }
}
