import {ConfigReport} from './config-report';
import {ProgressUpdate} from './progress-update';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class ConfigService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getConfigHistory(): IPromise<ConfigReport[]> {
    let q: IDeferred<ConfigReport[]> = this.$q.defer();

    this.$http.get('/api/config/history').then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getConfig(id: number): IPromise<ConfigReport> {
    let q: IDeferred<ConfigReport> = this.$q.defer();

    this.$http.get('/api/config/' + id).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public runConfig(id: number): IPromise<ConfigReport> {
    let q: IDeferred<ConfigReport> = this.$q.defer();

    this.$http.post('/api/config/' + id + '/run', {}).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getConfigProgress(id: number): IPromise<ProgressUpdate> {
    let q: IDeferred<ProgressUpdate> = this.$q.defer();

    this.$http.get('/api/config/' + id + '/progress').then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}