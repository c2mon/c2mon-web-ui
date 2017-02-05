import {Process} from '../process/process';
import {Equipment} from './equipment';
import {Tag} from '../tag/tag';
import {Command} from '../command/command';
import {IScope, IComponentOptions} from 'angular';
import {IStateService, IStateParamsService} from 'angular-ui-router';

var Stomp = require('stompjs/lib/stomp.js').Stomp;
var SockJS = require('sockjs-client');

export class EquipmentDetailComponent implements IComponentOptions {
  public templateUrl: string = '/equipment/equipment-detail.component.html';
  public controller: Function = EquipmentDetailController;
  public bindings: any = {
    process: '=',
  };
}

class EquipmentDetailController {
  public static $inject: string[] = ['$scope', '$state', '$stateParams'];

  public process: Process;
  public equipment: Equipment;
  public status: Tag;
  public statusSubscription: any;
  public heartbeat: Tag;
  public heartbeatSubscription: any;
  public commFault: Tag;
  public commFaultSubscription: any;
  private stompClient: any;

  public constructor(private $scope: IScope, private $state: IStateService, private $stateParams: IStateParamsService) {
    let equipments: any = this.process.equipmentConfigurations;

    for (let equipmentId: number in equipments) {
      let equipment: Equipment = equipments[equipmentId];

      if (equipment.name === $stateParams.ename) {
        this.equipment = equipment;
      }
    }

    let socket: any = new SockJS('/websocket');
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = null;
    this.stompClient.connect({}, this.onConnection);
  }

  public onConnection = (frame: any) => {
    console.log('Connected: ' + frame);

    if (this.equipment.statusTagId) {
      this.statusSubscription = this.stompClient.subscribe('/topic/tags/' + this.equipment.statusTagId, this.onStatusUpdate);
      this.stompClient.send('/app/tags/' + this.equipment.statusTagId);
    }

    if (this.equipment.aliveTagId) {
      this.heartbeatSubscription = this.stompClient.subscribe('/topic/tags/' + this.equipment.aliveTagId, this.onHeartbeat);
      this.stompClient.send('/app/tags/' + this.equipment.aliveTagId);
    }

    if (this.equipment.commFaultTagId) {
      this.commFaultSubscription = this.stompClient.subscribe('/topic/tags/' + this.equipment.commFaultTagId, this.onCommFault);
      this.stompClient.send('/app/tags/' + this.equipment.commFaultTagId);
    }
  };

  public onStatusUpdate = (message) => {
    this.status = JSON.parse(message.body);
    this.$scope.$apply();
  };

  public onHeartbeat = (message) => {
    this.heartbeat = JSON.parse(message.body);
    this.$scope.$apply();
  };

  public onCommFault = (message) => {
    this.commFault = JSON.parse(message.body);
    this.$scope.$apply();
  };

  public onTagSelected(tag: Tag) {
    this.$state.go('tag', {
      pname: this.process.processName,
      ename: this.equipment.name,
      tname: tag.name,
      tag: tag
    });
  }

  public onCommandSelected(command: Command) {
    this.$state.go('command', {
      pname: this.process.processName,
      ename: this.equipment.name,
      cid: command.id
    });
  }

  public $onDestroy(): void {
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }

    if (this.heartbeatSubscription) {
      this.heartbeatSubscription.unsubscribe();
    }

    if (this.commFaultSubscription) {
      this.commFaultSubscription.unsubscribe();
    }
  }
}