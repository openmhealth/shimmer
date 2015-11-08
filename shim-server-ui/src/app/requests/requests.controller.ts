// import { WebDevTecService, ITecThing } from '../components/webDevTec/webDevTec.service';
import { ShimmerService } from '../components/shimmer/shimmer.service';
import { UserSearchBarService } from '../components/userSearchBar/userSearchBar.service';


export class RequestsController {

  public shims: ShimMap;
  public parameters: RequestParameters;
  public dateOptions = {
    changeYear: true,
    changeMonth: true,
    yearRange: '1900:-0'
  };

  public dateTypes = [
    'effective_timeframe',
    'creation_date'
  ];


  /* @ngInject */
  constructor(private $scope:angular.IScope, private shimmer: ShimmerService, private userSearchBar: UserSearchBarService) {
    this.shims = shimmer.shims;

    // if the list of shims changes, update the panel states
    // to include any new shims
    $scope.$watchCollection('requests.shims', ((shims: ShimMap) => {
        if( shims ){
          this.parameters = {
            shim: this.shims[Object.keys(this.shims)[0]],
            schema: undefined,
            url: undefined,
            dateType: undefined,
            startDate: new Date(),
            endDate: new Date()
          };
          console.log(shims)
        }
    }).bind(this));

  }

  public getData(){
    this.shimmer.getData( this.parameters );
  }

  public updateUrl(){
    this.parameters.url = this.shimmer.getRequestUrl(this.userSearchBar.selectedUser, this.parameters);
  }

}
