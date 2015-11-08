import { ShimmerService } from '../components/shimmer/shimmer.service';
import { UserSearchBarController } from '../components/userSearchBar/userSearchBar.directive';
import { UserSearchBarService } from '../components/userSearchBar/userSearchBar.service';


export class UsersController {

  public shims: ShimMap;
  public searchBar: UserSearchBarService;

  /* @ngInject */
  constructor(private shimmer: ShimmerService, userSearchBar: UserSearchBarService) {
    this.shimmer = shimmer;
    this.shims = shimmer.shims;
    this.searchBar = userSearchBar;
  }

  public connectSelectedUser($event: angular.IAngularEvent, shimName: string) {
      var self: UsersController = this;
      console.log(arguments);
      this.shimmer.initOAuthFlow(self.searchBar.selectedUser.id, shimName, () => {
          console.log('closed popup');
          self.shimmer.updateAuthorizations(self.searchBar.selectedUser.id);
      });
      $event.preventDefault();
      $event.stopPropagation();
  }

  public disconnectSelectedUser($event: angular.IAngularEvent, shimName: string) {
      var self: UsersController = this;
      console.log(arguments);
      this.shimmer.disconnect(self.searchBar.selectedUser.id, shimName, () => {
          console.log('disconnected user');
          self.shimmer.updateAuthorizations(self.searchBar.selectedUser.id);
      });
      $event.preventDefault();
      $event.stopPropagation();
  }

}
