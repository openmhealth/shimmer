import { ShimmerService } from '../components/shimmer/shimmer.service';

export class MainController {

  private shimmer: ShimmerService;
  public shims: IShimMap;


  /* @ngInject */
  constructor($timeout: angular.ITimeoutService, shimmer: ShimmerService) {
    this.shimmer = shimmer;
    this.shims = shimmer.shims;
  }

  public truncate( string: string, length: number): string {
      if (string.length > length) {
          string = string.substr(0, length - 3) + "...";
      }
      return string;
  }

}
