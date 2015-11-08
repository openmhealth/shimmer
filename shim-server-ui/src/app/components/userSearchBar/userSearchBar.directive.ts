import { ShimmerService } from '../shimmer/shimmer.service';
import { UserSearchBarService } from '../userSearchBar/userSearchBar.service';
import { UsersController } from '../../users/users.controller';

interface ProjectsScope extends angular.IScope {
  extraValues: any[];
}

/** @ngInject */
export function userSearchBar(): angular.IDirective {

    return {
        restrict: 'E',
        scope: {},
        templateUrl: 'app/components/userSearchBar/userSearchBar.html',
        link: linkFunc,
        controller: UserSearchBarController,
        controllerAs: 'searchBar',
        bindToController: true
    };

}

export class UserSearchBarController {

    private userList: User[];
    public service: UserSearchBarService;

    /** @ngInject */
    constructor(private shimmer: ShimmerService, userSearchBar: UserSearchBarService) {
        this.userList = [];
        this.service = userSearchBar;
    }

    public refresh(searchTerm: string): void {
        var self: UserSearchBarController = this;
        this.shimmer.searchUsers(searchTerm).then(function(authorizations: angular.resource.IResourceArray<AuthorizationsResourceDefinition>) {
            self.userList = authorizations.map(function(authorization: AuthorizationsResourceDefinition) {
              return { id: authorization.username, authorizations: authorization.auths };
        });
      });
    };

    public setSelectedUser( user: User ){
        console.info('changed: ', user);
        this.service.selectedUser = user;
        this.shimmer.updateAuthorizations( user.id );
    }

}

/** @ngInject */
function linkFunc(scope: ProjectsScope, el: JQuery, attr: any, searchBar: UserSearchBarController) {
}

