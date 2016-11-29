import {Process} from './process';
import {Equipment} from './equipment';
import {IComponentOptions} from 'angular';

export class ProcessDetailComponent implements IComponentOptions {
  public templateUrl: string = '/process/process-detail.component.html';
  public controller: Function = ProcessDetailController;
  public bindings: any = {
    process: '=',
  };
}

class ProcessDetailController {
  public static $inject: string[] = [];

  public process: Process;
  public selectedEquipment: Equipment[] = [];
  public active: number;

  public constructor() {}

  public onEquipmentSelected(equipment: Equipment) {
    this.active = this.selectedEquipment.indexOf(equipment);
    this.selectedEquipment.push(equipment);
  }
}