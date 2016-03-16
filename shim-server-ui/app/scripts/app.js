'use strict';

angular
    .module('sandboxConsoleApp', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.date'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainCtrl'
            })
            .when('/authorizationComplete/:errorState', {
                templateUrl: 'views/authorizationComplete.html',
                controller: 'AuthorizationCompleteCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    });
