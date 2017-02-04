import {Alarm} from './alarm';
import {AlarmService} from './alarm.service';
import {IComponentOptions} from 'angular';
import 'moment';
import Moment = moment.Moment;

export class AlarmDetailComponent implements IComponentOptions {
  public templateUrl: string = '/alarm/alarm-detail.component.html';
  public controller: Function = AlarmDetailController;
  public bindings: any = {
    alarm: '='
  };
}

class AlarmDetailController {
  public static $inject: string[] = ['AlarmService'];

  public alarm: Alarm;
  public history: Alarm[];

  public constructor(private alarmService: AlarmService) {
    // Ask for one hour by default
    let max: Moment = moment();
    let min: Moment = moment(max).subtract(1, 'hour');

    this.alarmService.getHistory(this.alarm, min.valueOf(), max.valueOf()).then((history: Alarm[]) => {
      this.history = history;
    });
  }

  public formatTimestamp(timestamp: number): string {
    return moment(timestamp).format();
  }
}
