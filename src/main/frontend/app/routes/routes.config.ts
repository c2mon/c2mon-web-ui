/**
 * Define the routes (pages) that make up the application.
 */
export class RouteConfig {

  public static configure($stateProvider: any, $urlRouterProvider: any, $locationProvider: any): void {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('tags',      { url: '/',          component: 'tagList'})
        .state('processes', { url: '/processes', component: 'processList'})
        .state('config',    { url: '/config',    component: 'config'})
  }
}
