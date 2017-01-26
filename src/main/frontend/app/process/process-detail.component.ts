import {Process} from './process';
import {Equipment} from '../equipment/equipment';
import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IScope, IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

var Stomp = require('stompjs/lib/stomp.js').Stomp;
var SockJS = require('sockjs-client');

export class ProcessDetailComponent implements IComponentOptions {
  public templateUrl: string = '/process/process-detail.component.html';
  public controller: Function = ProcessDetailController;
  public bindings: any = {
    process: '=',
  };
}

class ProcessDetailController {
  public static $inject: string[] = ['$scope', '$state', 'TagService'];

  public process: Process;
  public heartbeat: Tag;
  public status: Tag;
  private stompClient: any;

  public constructor(private $scope: IScope, private $state: IStateService) {
    var socket = new SockJS('/websocket');
    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({}, this.onConnection);
  }

  public onConnection = (frame) => {
    console.log('Connected: ' + frame);

    this.stompClient.subscribe('/topic/tags/' + this.process.aliveTagID, this.onHeartbeat);
    this.stompClient.send("/app/tags/" + this.process.aliveTagID);

    this.stompClient.subscribe('/topic/tags/' + this.process.statusTagId, this.onStatusUpdate);
    this.stompClient.send("/app/tags/" + this.process.statusTagId);
  };

  public onHeartbeat = (message) => {
    this.heartbeat = JSON.parse(message.body);
    this.$scope.$apply();
  };

  public onStatusUpdate = (message) => {
    this.status = JSON.parse(message.body);
    this.$scope.$apply();
  };

  public onEquipmentSelected(equipment: Equipment) {
    this.$state.go('equipment', { pname: this.process.processName, ename: equipment.name });
  }
}