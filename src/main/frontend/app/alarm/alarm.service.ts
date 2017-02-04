import {Alarm} from './alarm';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class AlarmService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getAlarm(id: number): IPromise<Alarm> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    this.$http.get('/api/alarms/' + id).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getActiveAlarms(): IPromise<Alarm[]> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    this.$http.get('/api/alarms/active').then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getHistory(alarm: Alarm, min: number, max: number): IPromise<Alarm[]> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    this.$http.get('/api/alarms/' + alarm.id + '/history', {params: {min: min, max: max}}).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}
