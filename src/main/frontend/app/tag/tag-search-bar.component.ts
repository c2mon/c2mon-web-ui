import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions} from 'angular';
import {IStateService} from "angular-ui-router";

export class TagSearchBarComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-search-bar.component.html';
  public controller: Function = TagSearchBarController;
}

class TagSearchBarController {
  public static $inject: string[] = ['$state', 'TagService'];

  public constructor(private $state: IStateService, private tagService: TagService) {}

  public onTagSelected(tag: Tag) {
    this.$state.go('tag', { name: tag.name });
  }

  public findTags(query: string) {
    return this.tagService.findTags(query);
  }
}
