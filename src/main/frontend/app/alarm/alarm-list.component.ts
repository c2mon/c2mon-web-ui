import {Alarm} from './alarm';
import {AlarmService} from './alarm.service';
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class AlarmListComponent implements IComponentOptions {
  public templateUrl: string = '/alarm/alarm-list.component.html';
  public controller: Function = AlarmListController;
}

class AlarmListController {
  public static $inject: string[] = ['$state', 'AlarmService'];

  public alarms: Alarm[];

  public constructor(private $state: IStateService, private alarmService: AlarmService) {
    alarmService.getActiveAlarms().then((alarms: Alarm[]) => {
      this.alarms = alarms;
    });
  }

  public onAlarmSelected(alarm: Alarm): void {
    this.$state.go('alarm', { aid: alarm.id });
  }
}
