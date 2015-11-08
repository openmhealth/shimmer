/** @ngInject */
export function routerConfig($stateProvider: ng.ui.IStateProvider, $urlRouterProvider: ng.ui.IUrlRouterProvider) {
  $stateProvider
    .state('home', {
      url: '/',
      templateUrl: 'app/main/main.html',
      controller: 'MainController',
      controllerAs: 'main'
    })
    .state('users', {
      url: '/users',
      templateUrl: 'app/users/users.html',
      controller: 'UsersController',
      controllerAs: 'users'
    })
    .state('requests', {
      url: '/requests',
      templateUrl: 'app/requests/requests.html',
      controller: 'RequestsController',
      controllerAs: 'requests'
  });

  $urlRouterProvider.otherwise('/');
}
