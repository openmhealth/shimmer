/// <reference path="../../../../.tmp/typings/angularjs/angular-resource.d.ts" />

export class ShimmerService {

  // static $inject = [
  //   '$resource'
  // ];

  public static API_DOMAIN: string = 'http://localhost';
  public static API_PORT: string = '3000';
  public static API_PATH: string = '/api';
  public shims: IShimMap;

  // Authorizations for the current user
  private AuthorizationsResource: IAuthorizationsResource;
  private ConfigurationResource: IConfigurationResource;
  private SchemaResource: ISchemaResource;
  private apiUrl: string;

  /* @ngInject */
  constructor(private $resource: angular.resource.IResourceService) {

    //this.apiUrl = ShimmerService.API_DOMAIN + ':' + ShimmerService.API_PORT + ShimmerService.API_PATH;
      this.apiUrl = ShimmerService.API_PATH;
      this.shims = {};

    this.AuthorizationsResource = <IAuthorizationsResource>$resource(
      this.apiUrl + '/authorizations', {
        update: (authorizations: IAuthorizations): IAuthorizations => {
          return <IAuthorizations>null;
        }
      }
    );

    this.ConfigurationResource = <IConfigurationResource>$resource(
      this.apiUrl + '/configuration', {
        update: (configuration: IConfiguration): IConfiguration => {
          return <IConfiguration>null;
        }
      }
    );

    this.SchemaResource = <ISchemaResource>$resource(
      this.apiUrl + '/schemas', {
            update: (schemaList: ISchemaList): ISchemaList => {
                return <ISchemaList>null;
        }
      }
    ); 


    this.updateConfigurations().then((newConfigurations: IConfiguration[]): void => {
        console.info('shims from config:', this.shims);
    });
    this.updateSchemas().then((newSchemas: ISchemaList[]): void => {
        console.info('shims:', this.shims);
    });
  }

  public searchUsers(searchTerm: string): angular.IPromise<IAuthorizations[]> {
    return this.AuthorizationsResource.query( { username: searchTerm } ).$promise;
  }

  private pruneShims(shimNames: string[]){
    // clear out shims that are nolonger there
    Object.keys(this.shims).forEach((shimName: string): void => {
        if (shimNames.indexOf(shimName) < 0) {
            delete this.shims[shimName];
        }
    });
    // add empty new shims
    shimNames.forEach((shimName: string): void => {
      if ( !this.shims.hasOwnProperty(shimName) ){
          this.shims[ shimName ] = {
            name: null,
            schemas: [],
            configuration: null,
            authenticated: false
          };
      }
    });
  }
  private responseShimNames( responseArray: any): string[]{
      return responseArray.map((response: any): string => { return response.shimName; });
  }

  // generic method for updating a property of a shim using a callback that
  // handles the specific type of resource queried.
  private updateShimProperty(propertyCallback: (property: IShimPropertyResource) => void): (newProperties: IShimPropertyResource[])=>IShimPropertyResource[] {

      var callback = propertyCallback;

      return (newProperties: IShimPropertyResource[]) => {

        var shimNames = this.responseShimNames(newProperties);
        this.pruneShims(shimNames);

        newProperties.forEach((newProperty: IShimPropertyResource): void => {
            callback( newProperty );
        });

        return newProperties;

      }

  }

  public updateConfigurations(): angular.IPromise<angular.resource.IResourceArray<IConfigurationResourceDefinition>> {

      return this.ConfigurationResource.query({}, this.updateShimProperty((newConfig: IConfiguration): void => {
          var map = {};
          newConfig.values.forEach((value: IConfigurationValue): void=> {
              var key = Object.keys(value)[0];
              console.log(value);
              map[key] = value[key];
          });
          newConfig.values = map;
          newConfig.directives.forEach( (directive: IConfigurationDirective): void=> { 
              if (directive.type == 'integer') {
                  newConfig.values[directive.configPath] = parseInt(newConfig.values[directive.configPath]);
              }
              if (directive.type == 'float') {
                  newConfig.values[directive.configPath] = parseFloat(newConfig.values[directive.configPath]);
              }
          });
          this.shims[newConfig.shimName].configuration = newConfig;
      })).$promise;

  }

  public updateSchemas(): angular.IPromise<angular.resource.IResourceArray<ISchemaListResourceDefinition>> {

      return this.SchemaResource.query({}, this.updateShimProperty((newSchemaList: ISchemaList): void => {
          this.shims[newSchemaList.shimName].schemas = newSchemaList.schemas;
      })).$promise;

  }


  public getConfiguration( shimName: string ): IConfiguration{
      return this.shims[shimName].configuration;
  }

}
