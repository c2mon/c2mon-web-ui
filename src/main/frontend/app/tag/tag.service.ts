import {Tag} from './tag';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class TagService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getTag(id: string): IPromise<Tag> {
    let q: IDeferred<Tag> = this.$q.defer();

    this.$http.get('/api/tags/' + id).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public findTags(query: string): IPromise<Tag[]> {
    let q: IDeferred<Tag[]> = this.$q.defer();

    if (isNaN(Number(query))) {
      // If we have a non-numeric string, search by name
      this.$http.get('/api/tags/search?query=' + '.*' + query + '.*').then((response: any) => {
        q.resolve(response.data);
      });

    } else {
      // Otherwise, look for an exact tag by id
      this.$http.get('/api/tags/' + query).then((response: any) => {
        q.resolve(response.data ? [response.data] : []);
      });
    }

    return q.promise;
  }

  public getTopTags(size: number): IPromise<Tag[]> {
    let q: IDeferred<Tag[]> = this.$q.defer();

    this.$http.get('/api/tags/top?size=' + size).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getHistory(tag: Tag, min: number, max: number, aggregate: string): IPromise<Tag[]> {
    let q: IDeferred<Tag[]> = this.$q.defer();

    this.$http.get('/api/tags/' + tag.id + '/history', {params: {min: min, max: max, aggregate: aggregate}}).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}
