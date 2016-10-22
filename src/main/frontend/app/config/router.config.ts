/**
 * Define the routes (pages) that make up the application.
 */
export class RouterConfig {

  public static configure($stateProvider: any, $urlRouterProvider: any, $locationProvider: any): void {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('home', { url: '/', component: 'home'})
  }
}
