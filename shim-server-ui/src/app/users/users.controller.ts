// import { WebDevTecService, ITecThing } from '../components/webDevTec/webDevTec.service';
import { ShimmerService } from '../components/shimmer/shimmer.service';


export class UsersController {

  public currentUser: IUser;
  public list: IUser[];
  private shimmer: ShimmerService;

  public refresh(searchTerm: string): void {
    var self = this;
    this.shimmer.searchUsers(searchTerm).then(function(authorizations: IAuthorizations[]) {
      self.list = authorizations.map(function(authorization: IAuthorizations) {
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
