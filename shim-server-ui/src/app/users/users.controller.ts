// import { WebDevTecService, ITecThing } from '../components/webDevTec/webDevTec.service';
import { ShimmerService } from '../components/shimmer/shimmer.service';


export class UsersController {

  public currentUser: User;
  public list: User[];
  private shimmer: ShimmerService;

  public refresh(searchTerm: string): void {
    var self = this;
    this.shimmer.searchUsers(searchTerm).then(function(authorizations: Authorizations[]) {
      self.list = authorizations.map(function(authorization: Authorizations) {
        return { id: authorization.username, authorizations: authorization.auths };
      });
    });
  };


  /* @ngInject */
  constructor(shimmer: ShimmerService) {
    this.list = [];
    this.currentUser = { id: '', authorizations: [] };
    this.shimmer = shimmer;
  }

}
