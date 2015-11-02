// import { GithubContributor } from '../githubContributor/githubContributor.service';
import { UsersController } from '../../users/users.controller';

interface IProjectsScope extends angular.IScope {
  extraValues: any[];
}

/** @ngInject */
export function userSearchBar(): angular.IDirective {

    return {
        restrict: 'E',
        scope: {
          users: '=',
        },
        templateUrl: 'app/components/userSearchBar/userSearchBar.html',
        link: linkFunc,
        controller: UserSearchBarController,
        controllerAs: 'searchBar',
        bindToController: true
    };

}

export class UserSearchBarController {

    public users: UsersController;

    /** @ngInject */
    constructor() {
    }

}

/** @ngInject */
function linkFunc(scope: IProjectsScope, el: JQuery, attr: any, searchBar: UserSearchBarController) {
}

