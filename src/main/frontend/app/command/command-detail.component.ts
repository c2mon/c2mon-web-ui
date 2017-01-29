import {Command} from './command';
import {Process} from '../process/process';
import {ProcessService} from '../process/process.service';
import {Equipment} from '../equipment/equipment';
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class CommandDetailComponent implements IComponentOptions {
  public templateUrl: string = '/command/command-detail.component.html';
  public controller: Function = CommandDetailController;
  public bindings: any = {
    command: '='
  };
}

class CommandDetailController {
  public static $inject: string[] = ['$state', 'ProcessService'];

  public process: Process;
  public equipment: Equipment;
  public command: Command;

  public constructor(private $state: IStateService, processService: ProcessService) {
    processService.getProcess(this.command.processName).then((process: Process) => {
      this.process = process;

      let equipments: any = this.process.equipmentConfigurations;

      for (let equipmentId: number in equipments) {
        let equipment: Equipment = equipments[equipmentId];

        if (equipment.name === this.command.equipmentName) {
          this.equipment = equipment;
        }
      }
    });
  }
}