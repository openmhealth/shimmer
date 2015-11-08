/// <reference path="../../.tmp/typings/tsd.d.ts" />
/// <reference path="shim-server-ui.d.ts" />

import { config } from './index.config';
import { routerConfig } from './index.route';
import { runMock } from './index.run';
import { MainController } from './main/main.controller';
import { UsersController } from './users/users.controller';
import { RequestsController } from './requests/requests.controller';
import { ShimmerService } from '../app/components/shimmer/shimmer.service';
import { UserSearchBarService } from '../app/components/userSearchBar/userSearchBar.service';
import { userSearchBar } from '../app/components/userSearchBar/userSearchBar.directive';

// declare var malarkey: any;
declare var moment: moment.MomentStatic;

module webApp {
  'use strict';

  angular.module('webApp', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngMessages', 'ngAria', 'ngResource', 'ui.router', 'ui.bootstrap', 'ui.select', 'ui.date', 'ngMockE2E'])
    .run(runMock)
    .constant('moment', moment)
    .config(config)
    .config(routerConfig)
    .service('shimmer', ShimmerService)
    .service('userSearchBar', UserSearchBarService)
    .controller('MainController', MainController)
    .controller('UsersController', UsersController)
    .controller('RequestsController', RequestsController)
    .directive('userSearchBar', userSearchBar);

    // angular.module('webAppDev', ['webApp', 'ngMockE2E'])
    //   .run(runMock);
}
