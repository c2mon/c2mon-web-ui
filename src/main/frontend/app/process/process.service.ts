import {Process} from './process';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class ProcessService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getProcessNames(): IPromise<string[]> {
    let q: IDeferred<Process[]> = this.$q.defer();

    this.$http.get('/api/processes').then(function (response) {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getProcess(name: string): IPromise<Process> {
    let q: IDeferred<Process> = this.$q.defer();

    this.$http.get('/api/processes/' + name).then(function (response) {
      q.resolve(response.data);
    });

    return q.promise;
  }
}