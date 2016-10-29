import {Process} from './process';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class ProcessService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getProcesses(): IPromise<Process[]> {
    let q: IDeferred<Process[]> = this.$q.defer();

    this.$http.get('/api/processes').then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getProcess(name: string): IPromise<Process> {
    let q: IDeferred<Process> = this.$q.defer();

    this.$http.get('/api/processes/' + name).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}