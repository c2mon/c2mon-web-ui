import 'angular';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'jquery';
import 'bootstrap-sass';

import {MainComponent} from './main.component';
import {TagListComponent} from './tag/tag-list.component';
import {TagDetailComponent} from './tag/tag-detail.component';
import {TagSearchBarComponent} from './tag/tag-search-bar.component';
import {AlarmListComponent} from './alarm/alarm-list.component';
import {AlarmDetailComponent} from './alarm/alarm-detail.component';
import {AlarmSearchBarComponent} from './alarm/alarm-search-bar.component';
import {ProcessListComponent} from './process/process-list.component';
import {ProcessDetailComponent} from './process/process-detail.component';
import {ProcessSearchBarComponent} from './process/process-search-bar.component';
import {EquipmentDetailComponent} from './equipment/equipment-detail.component';
import {CommandDetailComponent} from './command/command-detail.component'
import {ConfigListComponent} from './config/config-list.component';
import {ConfigDetailComponent} from './config/config-detail.component';
import {ConfigRunBarComponent} from './config/config-run-bar.component';
import {TagService} from './tag/tag.service';
import {AlarmService} from './alarm/alarm.service';
import {ProcessService} from './process/process.service';
import {CommandService} from './command/command.service';
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
app.component('tagDetail', new TagDetailComponent());
app.component('tagSearchBar', new TagSearchBarComponent());
app.component('alarmSearchBar', new AlarmSearchBarComponent());
app.component('alarmList', new AlarmListComponent());
app.component('alarmDetail', new AlarmDetailComponent());
app.component('processList', new ProcessListComponent());
app.component('processDetail', new ProcessDetailComponent());
app.component('processSearchBar', new ProcessSearchBarComponent());
app.component('equipmentDetail', new EquipmentDetailComponent());
app.component('commandDetail', new CommandDetailComponent());
app.component('configList', new ConfigListComponent());
app.component('configDetail', new ConfigDetailComponent());
app.component('configRunBar', new ConfigRunBarComponent());
app.service('TagService', TagService);
app.service('AlarmService', AlarmService);
app.service('ProcessService', ProcessService);
app.service('CommandService', CommandService);
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
