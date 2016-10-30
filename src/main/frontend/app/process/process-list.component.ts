import {Process} from '../process/process';
import {Equipment} from '../process/equipment';
import {ProcessService} from '../process/process.service';
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class ProcessListComponent implements IComponentOptions {
  public templateUrl: string = '/process/process-list.component.html';
  public controller: Function = ProcessListController;
}

class ProcessListController {
  public static $inject: string[] = ['$state', 'ProcessService'];

  public processes: string[];
  public selectedProcess: Process;


  public constructor(private $state: IStateService, private processService: ProcessService) {
    this.processService.getProcesses().then((processes: string[]) => {
      this.processes = processes;
    });
  }

  public onProcessSelected(process: Process): void {
    this.$state.go('process', { name: process.processName });
  }
}
