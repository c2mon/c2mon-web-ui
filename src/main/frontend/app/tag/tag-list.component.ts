import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions} from 'angular';
import {IStateService} from "angular-ui-router";

export class TagListComponent implements IComponentOptions {
  public templateUrl: string = '/tag/tag-list.component.html';
  public controller: Function = TagListController;
}

class TagListController {
  public static $inject: string[] = ['$state', 'TagService'];

  public topTags: Tag[];

  public constructor(private $state: IStateService, private tagService: TagService) {
    this.tagService.getTopTags(10).then((tags: Tag[]) => {
      this.topTags = tags;
    });
  }

  public onTagSelected(tag: Tag) {
    this.$state.go('tag', { name: tag.name });
  }

  public findTags(query: string) {
    return this.tagService.findTags(query + '*');
  }
}
