import {Tag} from '../tag/tag';
import {TagService} from '../tag/tag.service';
import {IComponentOptions} from 'angular';

export class HomeComponent implements IComponentOptions {
  public templateUrl: string = '/home/home.component.html';
  public controller: Function = HomeController;
}

class HomeController {
  public static $inject: string[] = ['TagService'];

  public tag: Tag;
  public tagHistory: Tag[];

  public constructor(private tagService: TagService) {
    this.tag = {
      id: 1234,
      name: "cpu.loadavg",
      description: "",
      value: 25.23
    };
  }

  public onTagSelected(tag: Tag) {
    this.tagService.getHistory(tag).then((history: Tag[]) => {
      this.tagHistory = history;
    })
  }

  public findTags(query: string) {
    return this.tagService.findTags(query);
  }
}
