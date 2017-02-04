import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions, IPromise} from 'angular';
import {IStateService} from 'angular-ui-router';

export class TagSearchBarComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-search-bar.component.html';
  public controller: Function = TagSearchBarController;
}

class TagSearchBarController {
  public static $inject: string[] = ['$state', 'TagService'];

  public constructor(private $state: IStateService, private tagService: TagService) {}

  public findTags(query: string): IPromise<Tag[]>  {
    return this.tagService.findTags(query);
  }

  public onTagSelected(tag: Tag): void {
    this.$state.go('tag', { pname: tag.processName, ename: tag.equipmentName, tname: tag.name });
  }
}
