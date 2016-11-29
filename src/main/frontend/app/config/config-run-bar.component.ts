import {ConfigReport} from './config-report';
import {ProgressUpdate} from './progress-update';
import {ConfigService} from './config.service';
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class ConfigRunBarComponent implements IComponentOptions {
  public templateUrl: string = '/config/config-run-bar.component.html';
  public controller: Function = ConfigRunBarController;
}

class ConfigRunBarController {
  public static $inject: string[] = ['$state', 'ConfigService'];

  public configId: number = null;
  public configStatus: string = null;
  public configProgress: ProgressUpdate = undefined;

  public constructor(private $state: IStateService, private configService: ConfigService) {}

  public runConfig() {
    this.configStatus = 'started';

    this.configService.runConfig(this.configId).then((config: ConfigReport) => {
      console.log('running config ' + this.configId);

      // Start a timer loop to periodically poll for progress updates
      this.getProgress();
    }, (error: any) => {
      this.configStatus = 'error';
    });
  }

  public getProgress() {
    console.log('checking progress');

    this.configService.getConfigProgress(this.configId).then((progressUpdate: ProgressUpdate) => {
      this.configProgress = progressUpdate;

      if (!this.configProgress) {
        this.configStatus = 'success';
        this.$state.go('config', {id: this.configId});
      }

      if (this.configStatus !== 'success' && this.configStatus !== 'error') {
        setTimeout(this.getProgress, 100);
      }
    });
  }
}
