import {TagService} from '../tag/tag.service';
import {ILocationProvider} from 'angular';
import {IStateProvider, IUrlRouterProvider, IStateParamsService} from "angular-ui-router";

/**
 * Define the routes (pages) that make up the application.
 */
export class RouteConfig {

  public static configure($stateProvider: IStateProvider, $urlRouterProvider: IUrlRouterProvider, $locationProvider: ILocationProvider): void {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('tags',      { url: '/',            component: 'tagList'})
        .state('tag',       { url: '/tags/:name',  component: 'tagDetail',
          resolve: {
            tag: ['$stateParams', 'TagService', ($stateParams: IStateParamsService, tagService: TagService) => {
              return tagService.getTag($stateParams.name);
            }]
        }})
        .state('processes', { url: '/processes',   component: 'processList'})
        .state('config',    { url: '/config',      component: 'config'})
  }
}
