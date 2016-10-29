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

  public processes: string[];
  public selectedProcess: Process;
  public selectedEquipment: Equipment[] = [];

  public constructor(private processService: ProcessService) {
    this.processService.getProcesses().then((processes: string[]) => {
      this.processes = processes;
    });
  }

  public onProcessSelected(process: Process): void {
    this.selectedProcess = process;
    this.selectedEquipment = [];
  }

  public onEquipmentSelected(equipment: Equipment) {
    equipment.active = true;
    this.selectedEquipment.push(equipment);
  }
}
