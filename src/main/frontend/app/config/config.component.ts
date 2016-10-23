import {ConfigReport} from './config-report';
import {ConfigService} from './config.service';
import {IComponentOptions} from 'angular';

export class ConfigComponent implements IComponentOptions {
  public templateUrl: string = '/config/config.component.html';
  public controller: Function = ConfigController;
}

class ConfigController {
  public static $inject: string[] = ['ConfigService'];

  public history: any[] = [];
  public configReports: any = {};
  public configId: number = null;
  public configStatus: string = null;
  public configProgress: string = undefined;

  public constructor(private configService: ConfigService) {
    this.configService.getConfigHistory().then((history: any[]) => {
      this.history = history;
    });
  }

  public viewConfig(config: any): void {
    this.configService.getConfig(config.id).then((config: ConfigReport) => {
      this.configReports[config.id] = config;
      this.configReports[config.id][0].active = true;
    });
  }

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

    this.configService.getConfigProgress(this.configId).then((config: ConfigReport) => {
      this.configProgress = config;

      if (!this.configProgress) {
        this.configStatus = 'success';

        this.configService.getConfig(this.configId).then((config: ConfigReport) => {
          this.configReports[this.configId] = config;
          this.configReports[this.configId][0].active = true;
        });
      }

      if (this.configStatus !== 'success' && this.configStatus !== 'error') {
        setTimeout(this.getProgress, 100);
      }
    });
  }
}
