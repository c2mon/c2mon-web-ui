import {ConfigReport} from './config-report';
import {ConfigService} from './config.service';
import {IComponentOptions} from 'angular';

export class ConfigDetailComponent implements IComponentOptions {
  public templateUrl: string = '/config/config-detail.component.html';
  public controller: Function = ConfigDetailController;
  public bindings: any = {
    configs: '=',
  };
}

class ConfigDetailController {
  public static $inject: string[] = ['ConfigService'];

  public configs: ConfigReport[];

  public constructor(private configService: ConfigService) {}
}
