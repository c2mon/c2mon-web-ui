import {Tag} from './tag';
import {TagService} from './tag.service';
import {Process} from '../process/process';
import {ProcessService} from '../process/process.service';
import {Equipment} from '../equipment/equipment';
import {IComponentOptions, IHttpService, IScope} from 'angular';
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
  public range: string = 'hour';
  public aggregate: string = 'auto';
  private min: number;
  private max: number;
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
    this.max = moment().valueOf();
    this.min = moment(this.max).subtract(1, this.range).valueOf();

    this.tagService.getHistory(this.tag, this.min, this.max, this.aggregate).then((history: Tag[]) => {
      this.history = history;
      this.createTagHistoryChart(this.history);
    });

    let socket = new SockJS('/websocket');
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = null;
    this.stompClient.connect({}, this.onConnection);
  }

  public onConnection = (frame) => {
    console.log('Connected: ' + frame);

    let tagId: number = this.tag.id;
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
        enabled: false
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
    this.min = Math.round(event.min);
    this.max = Math.round(event.max);
    this.reloadHistory();
  };

  public reloadHistory(): void {
    this.chart.showLoading('Loading...');
    this.tagService.getHistory(this.tag, this.min, this.max, this.aggregate).then((history: Tag[]) => {
      this.history = history;

      this.chart.showLoading('Rendering...');
      this.chart.series[0].setData(history);
      this.chart.hideLoading();
    });
  }

  public useRange(range: string): void {
    this.range = range;
    let extremes: any = this.chart.xAxis[0].getExtremes();
    this.max = moment().valueOf();
    this.min = moment(this.max).subtract(1, range).valueOf();
    this.chart.xAxis[0].setExtremes(this.min, this.max);
  }

  public formatTimestamp(timestamp: number): string {
    return moment(timestamp).format();
  }
}
