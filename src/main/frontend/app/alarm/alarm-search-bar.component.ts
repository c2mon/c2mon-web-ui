import {Alarm} from './alarm';
import {AlarmService} from './alarm.service';
import {IComponentOptions, IPromise} from 'angular';
import {IStateService} from 'angular-ui-router';

export class AlarmSearchBarComponent implements IComponentOptions {
  public templateUrl: string = '/alarm/alarm-search-bar.component.html';
  public controller: Function = AlarmSearchBarController;
}

class AlarmSearchBarController {
  public static $inject: string[] = ['$state', 'AlarmService'];

  public constructor(private $state: IStateService, private alarmService: AlarmService) {}

  public findAlarms(query: string): IPromise<Alarm[]> {
    return this.alarmService.findAlarms(query);
  }

  public onAlarmSelected(alarm: Alarm): void {
    this.$state.go('alarm', { aid: alarm.id });
  }
}
