// import { GithubContributor } from '../githubContributor/githubContributor.service';

interface IProjectsScope extends ng.IScope {
  extraValues: any[];
}

/** @ngInject */
export function userSearchBar(): ng.IDirective {

    return {
        restrict: 'E',
        scope: {
          userList: '=',
          currentUser: '='
        },
        templateUrl: 'app/components/userSearchBar/userSearchBar.html',
        link: linkFunc,
        controller: UserSearchBarController,
        controllerAs: 'search',
        bindToController: true
    };

}

/** @ngInject */
export class UserSearchBarController {

  public getUsers = function(searchTerm: string) {
    return this.userList;
  };

}

function linkFunc(scope: IProjectsScope, el: JQuery, attr: any, vm: UserSearchBarController) {
    console.dir(scope);
}

