import 'angular';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'jquery'
import 'bootstrap-sass';

import {MainComponent} from './main.component';
import {TagListComponent} from './tag/tag-list.component';
import {ProcessListComponent} from './process/process-list.component';
import {ConfigComponent} from './config/config.component';
import {TagService} from './tag/tag.service';
import {ProcessService} from './process/process.service';
import {ConfigService} from './config/config.service';
import {RouteConfig} from './routes/routes.config';
import {HttpConfig} from './http/http.config';

let app: any = angular.module('c2mon-web-ui', [
  'ng',
  'ui.bootstrap',
  'ui.router'
]);

app.component('main', new MainComponent());
app.component('tagList', new TagListComponent());
app.component('processList', new ProcessListComponent());
app.component('config', new ConfigComponent());
app.service('TagService', TagService);
app.service('ProcessService', ProcessService);
app.service('ConfigService', ConfigService);

// Configure HTTP
app.config(['$httpProvider', ($httpProvider: any) => HttpConfig.configure($httpProvider)]);

// Configure routes
app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
  ($stateProvider: any, $urlRouterProvider: any, $locationProvider: any) => {
    RouteConfig.configure($stateProvider, $urlRouterProvider, $locationProvider);
  }]);

// Bootstrap the angular app
angular.bootstrap(document, ['c2mon-web-ui'], {
  strictDi: true
});
