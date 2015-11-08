import { ShimmerService } from '../components/shimmer/shimmer.service';

export class MainController {

  private shimmer: ShimmerService;
  public shims: ShimMap;

  public validBooleanSettings: any[] = [
      '',
      'true',
      'false'
  ];

  public booleanLabels: any = {
      '': '--',
      'true': 'true',
      'false': 'false'
  };

  public patterns: any = {
      integer: /^-?[0-9]+$/,
      float: /^[-+]?[0-9]*\.?[0-9]+$/,
      // booleanSelect: /true|false/
  }

  public configurationForm: angular.IFormController = null;
  public testFunction;
  public panelStates = {};


  /* @ngInject */
  constructor($scope: angular.IScope, shimmer: ShimmerService) {
    
    this.shimmer = shimmer;
    this.shims = shimmer.shims;

    // if the list of shims changes, update the panel states
    // to include any new shims
    $scope.$watchCollection('main.shims', (shims: ShimMap) => {
        if( shims ){
            var shimNames = Object.keys(shims);
            shimNames.forEach( (shimName)=>{
              if (!this.panelStates.hasOwnProperty(shimName)){
                  this.panelStates[shimName] = false;
              }
            });
        }
    });

  }

  public truncate( string: string, length: number): string {
      if (string.length > length) {
          string = string.substr(0, length - 3) + "...";
      }
      return string;
  }

  public getPattern(type){
      return this.patterns[type];
  }

}
