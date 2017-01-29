import {Command} from './command';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class CommandService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getCommand(id: number): IPromise<Command> {
    let q: IDeferred<Command> = this.$q.defer();

    this.$http.get('/api/commands/' + id).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}