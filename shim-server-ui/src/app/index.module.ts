/// <reference path="../../.tmp/typings/tsd.d.ts" />

import { config } from './index.config';
import { routerConfig } from './index.route';
import { runBlock } from './index.run';
import { MainController } from './main/main.controller';
import { UsersController } from './users/users.controller';
// import { WebDevTecService } from '../app/components/webDevTec/webDevTec.service';
import { userSearchBar } from '../app/components/userSearchBar/userSearchBar.directive';

declare var malarkey: any;
declare var moment: moment.MomentStatic;

module webApp {
  'use strict';

  angular.module('webApp', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngMessages', 'ngAria', 'ngResource', 'ui.router', 'ui.bootstrap', 'ui.select'])
    .constant('moment', moment)
    .config(config)
    .config(routerConfig)
    .run(runBlock)
    // .service('webDevTec', WebDevTecService)
    .controller('MainController', MainController)
    .controller('UsersController', UsersController)
    .directive('userSearchBar', userSearchBar);
}
