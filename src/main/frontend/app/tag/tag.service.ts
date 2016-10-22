import {Tag} from './tag';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class TagService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public findTags(query: string|number): IPromise<Tag[]> {
    let q: IDeferred<Tag[]> = this.$q.defer();

    if (isNaN(query)) {
      // If we have a non-numeric string, search by name
      this.$http.get('/api/tags/search?query=' + query).then(function (response) {
        console.log(response.data);
        q.resolve(response.data);
      })

    } else {
      // Otherwise, look for an exact tag by id
      this.$http.get('/api/tags/' + query).then(function (response) {
        console.log(response.data);
        q.resolve(response.data);
      })
    }

    return q.promise;
  }

  public getHistory(tag: Tag): IPromise<Tag[]> {
    let q: IDeferred<Tag[]> = this.$q.defer();

    this.$http.get('/api/tags/' + tag.id + '/history?records=10').then(function (response) {
      console.log(response.data);
      q.resolve(response.data);
    });

    return q.promise;
  }
}