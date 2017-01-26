import {TagService} from '../tag/tag.service';
import {ProcessService} from '../process/process.service';
import {ConfigService} from '../config/config.service';
import {ILocationProvider} from 'angular';
import {IStateProvider, IUrlRouterProvider, IStateParamsService} from 'angular-ui-router';

/**
 * Define the routes (pages) that make up the application.
 */
export class RouteConfig {

  public static configure($stateProvider: IStateProvider, $urlRouterProvider: IUrlRouterProvider, $locationProvider: ILocationProvider): void {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
      .state('tags',      { component: 'tagList',         url: '/tags'})
      .state('processes', { component: 'processList',     url: '/processes'})
      .state('process',   { component: 'processDetail',   url: '/processes/:pname',
                            resolve: { process: RouteConfig.resolveProcess() }})
      .state('equipment', { component: 'equipmentDetail', url: '/processes/:pname/equipment/:ename',
                            resolve: { process: RouteConfig.resolveProcess() }})
      .state('tag',       { component: 'tagDetail',       url: '/processes/:pname/equipment/:ename/tags/:tname',
                            resolve: { process: RouteConfig.resolveProcess(), tag: RouteConfig.resolveTag() }})
      .state('configs',   { component: 'configList',      url: '/config'})
      .state('config',    { component: 'configDetail',    url: '/config/:id',
      resolve: {
        configs: ['$stateParams', 'ConfigService', ($stateParams: IStateParamsService, configService: ConfigService) => {
          return configService.getConfigReports($stateParams.id);
        }]
      }})
  }

  private static resolveTag(): any {
    return ['$stateParams', 'TagService', ($stateParams: IStateParamsService, tagService: TagService) => {
      return tagService.getTag($stateParams.tname);
    }]
  }

  private static resolveProcess(): any {
    return ['$stateParams', 'ProcessService', ($stateParams: IStateParamsService, processService: ProcessService) => {
      return processService.getProcess($stateParams.pname);
    }]
  }
}
