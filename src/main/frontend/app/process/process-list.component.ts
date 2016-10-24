import {Process} from '../process/process';
import {Equipment} from '../process/equipment';
import {ProcessService} from '../process/process.service';
import {IComponentOptions} from 'angular';

export class ProcessListComponent implements IComponentOptions {
  public templateUrl: string = '/process/process-list.component.html';
  public controller: Function = ProcessListController;
}

class ProcessListController {
  public static $inject: string[] = ['ProcessService'];

  public processNames: string[];
  public selectedProcess: Process;
  public selectedEquipment: Equipment[] = [];

  public constructor(private processService: ProcessService) {
    this.processService.getProcessNames().then((processNames: string[]) => {
      this.processNames = processNames;

      // Load the first process by default
      this.onProcessSelected(processNames[0]);
    });
  }

  public onProcessSelected(processName: string): void {
    this.processService.getProcess(processName).then((process: Process) => {
      this.selectedProcess = process;
      this.selectedEquipment = [];
    });
  }

  public onEquipmentSelected(equipment: Equipment) {
    equipment.active = true;
    this.selectedEquipment.push(equipment);
  }
}
