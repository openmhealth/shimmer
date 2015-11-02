/// <reference path="../../../../.tmp/typings/angularjs/angular-resource.d.ts" />

export class ShimmerService {

  // static $inject = [
  //   '$resource'
  // ];

  public static API_DOMAIN: string = 'http://localhost';
  public static API_PORT: string = '3000';
  public static API_PATH: string = '/api';
  public shims: ShimMap;

  // Authorizations for the current user
  private AuthorizationsResource: AuthorizationsResource;
  private ConfigurationResource: ConfigurationResource;
  private SchemaListResource: SchemaListResource;
  private apiUrl: string;

  /* @ngInject */
  constructor(private $resource: angular.resource.IResourceService) {

    //this.apiUrl = ShimmerService.API_DOMAIN + ':' + ShimmerService.API_PORT + ShimmerService.API_PATH;
      this.apiUrl = ShimmerService.API_PATH;
      this.shims = {};

    this.AuthorizationsResource = <AuthorizationsResource>$resource(
      this.apiUrl + '/authorizations', {
        update: (authorizations: Authorizations): Authorizations => {
          return <Authorizations>null;
        }
      }
    );

    this.ConfigurationResource = <ConfigurationResource>$resource(
      this.apiUrl + '/configuration', {
        update: (configuration: Configuration): Configuration => {
          return <Configuration>null;
        }
      }
    );

    this.SchemaListResource = <SchemaListResource>$resource(
      this.apiUrl + '/schemas', {
            update: (schemaList: SchemaList): SchemaList => {
                return <SchemaList>null;
        }
      }
    ); 


    this.updateConfigurations().then((newConfigurations: ConfigurationResourceDefinition[]): void => {
        console.info('shims from config:', this.shims);
    });
    this.updateSchemas().then((newSchemas: SchemaListResourceDefinition[]): void => {
        console.info('shims:', this.shims);
    });
  }

  public searchUsers(searchTerm: string): angular.IPromise<Authorizations[]> {
    return this.AuthorizationsResource.query( { username: searchTerm } ).$promise;
  }

  public updateConfigurations(): angular.IPromise<angular.resource.IResourceArray<ConfigurationResourceDefinition>> {

      return this.ConfigurationResource.query({}, this.updateShimProperty((newConfig: Configuration): void => {
          var map = {};
          newConfig.values.forEach((value: ConfigurationValue): void=> {
              map[ value.settingId ] = value;
          });
          newConfig.values = map;
          newConfig.settings.forEach((setting: ConfigurationSetting): void=> {
              if (setting.type == 'integer') {
                  newConfig.values[setting.settingId].value = parseInt(newConfig.values[setting.settingId].value);
              }
              if (setting.type == 'float') {
                  newConfig.values[setting.settingId].value = parseFloat(newConfig.values[setting.settingId].value);
              }
          });
          this.shims[newConfig.shimName].configuration = newConfig;
      })).$promise;

  }

  public updateSchemas(): angular.IPromise<angular.resource.IResourceArray<SchemaListResourceDefinition>> {

      return this.SchemaListResource.query({}, this.updateShimProperty((newSchemaList: SchemaList): void => {
          this.shims[newSchemaList.shimName].schemas = newSchemaList.schemas;
      })).$promise;

  }

  public getConfiguration(shimName: string): Configuration {
      return this.shims[shimName].configuration;
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
  private updateShimProperty( propertyCallback: (property: ShimPropertyResource) => void): (newProperties: ShimPropertyResource[]) => ShimPropertyResource[] {

      var callback = propertyCallback;

      return (newProperties: ShimPropertyResource[]) => {

        var shimNames = this.responseShimNames(newProperties);
        this.pruneShims(shimNames);

        newProperties.forEach((newProperty: ShimPropertyResource): void => {
            callback( newProperty );
        });

        return newProperties;

      }

  }

}
