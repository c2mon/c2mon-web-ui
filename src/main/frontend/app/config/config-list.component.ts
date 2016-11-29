import {ConfigReport} from './config-report';
import {ConfigService} from './config.service';
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class ConfigListComponent implements IComponentOptions {
  public templateUrl: string = '/config/config-list.component.html';
  public controller: Function = ConfigListController;
}

class ConfigListController {
  public static $inject: string[] = ['$state', 'ConfigService'];

  public history: ConfigReport[] = [];
  public configReports: any = {};

  public constructor(private $state: IStateService, private configService: ConfigService) {
    this.configService.getConfigHistory().then((history: any[]) => {
      this.history = history;
    });
  }

  public viewConfig(config: any): void {
    this.$state.go('config', {id: config.id});
  }
}
