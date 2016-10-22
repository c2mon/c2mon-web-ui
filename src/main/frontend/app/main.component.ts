import {IComponentOptions, IScope} from 'angular';

export class MainComponent implements IComponentOptions {
  public templateUrl: string = '/main.component.html';
  public controller: Function = MainController;
}

class MainController {
  public static $inject: string[] = ['$scope'];

  constructor(private $scope: IScope) {}
}
