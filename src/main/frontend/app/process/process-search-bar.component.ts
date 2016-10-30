import {Process} from './process';
import {ProcessService} from './process.service';
import {IComponentOptions} from 'angular';
import {IStateService} from "angular-ui-router";

export class ProcessSearchBarComponent implements IComponentOptions {
  public templateUrl: string = '/process/process-search-bar.component.html';
  public controller: Function = ProcessSearchBarController;
}

class ProcessSearchBarController {
  public static $inject: string[] = ['$state', 'ProcessService'];

  public constructor(private $state: IStateService, private processService: ProcessService) {}

  public onProcessSelected(process: Process) {
    this.$state.go('process', { name: process.processName });
  }

  public getProcesses() {
    return this.processService.getProcesses();
  }
}
