import 'angular';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'jquery'
import 'bootstrap-sass';

import {MainComponent} from './main.component';
import {HomeComponent} from './home/home.component';
import {TagService} from './tag/tag.service';
import {RouterConfig} from './config/router.config';
import {HttpConfig} from './config/http.config';

let app: any = angular.module('c2mon-web-ui', [
  'ng',
  'ui.bootstrap',
  'ui.router'
]);

app.component('main', new MainComponent());
app.component('home', new HomeComponent());
app.service('TagService', TagService);

// Configure HTTP
app.config(['$httpProvider', ($httpProvider: any) => HttpConfig.configure($httpProvider)]);

// Configure routes
app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
  ($stateProvider: any, $urlRouterProvider: any, $locationProvider: any) => {
    RouterConfig.configure($stateProvider, $urlRouterProvider, $locationProvider);
  }]);

// Bootstrap the angular app
angular.bootstrap(document, ['c2mon-web-ui'], {
  strictDi: true
});
