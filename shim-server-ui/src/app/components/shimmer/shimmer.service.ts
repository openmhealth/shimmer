/// <reference path="../../../../.tmp/typings/angularjs/angular-resource.d.ts" />

export class ConfigurationImpl implements Configuration {

    public settings: ConfigurationSetting[];
    public values: any;

    constructor(public shimName: string) {

    }

    public getSetting(settingId: string): ConfigurationSetting {
        var match: ConfigurationSetting = null;
        this.settings.forEach((setting: ConfigurationSetting): void => {
            if (setting.settingId === settingId) {
                match = setting;
            }
        });
        return match;
    }

    public getValue(settingId: string): ConfigurationValue {
        return this.values[settingId];
    }

}

export class ShimmerService {

    public static API_DOMAIN: string = 'http://localhost';
    public static API_PORT: string = '3000';
    public static API_PATH: string = '/api';
    public shims: ShimMap;

    // Authorizations for the current user
    private AuthorizationsResource: AuthorizationsResource;
    private ConfigurationResource: ConfigurationResource;
    private SchemaListResource: SchemaListResource;
    private apiUrl: string;

    private endpoints = [];


    /* @ngInject */
    constructor(private $resource: angular.resource.IResourceService, private $http: angular.IHttpService, private $window: angular.IWindowService) {

        // this.apiUrl = ShimmerService.API_DOMAIN + ':' + ShimmerService.API_PORT + ShimmerService.API_PATH;
        this.apiUrl = ShimmerService.API_PATH;
        this.shims = {};

        this.defineEndpointsList();

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

        this.updateConfigurations(); //.then((newConfigurations: ConfigurationResourceDefinition[]): void => { });
        this.updateSchemas(); //.then((newSchemas: SchemaListResourceDefinition[]): void => {});
        
    }

    private defineEndpointsList(){
      
      var schemas = [];

      schemas['fitbit'] = [];
      schemas['googlefit'] = [];
      schemas['jawbone'] = [];
      schemas['misfit'] = [];
      schemas['runkeeper'] = [];
      schemas['withings'] = [];

      schemas['fitbit']['activity'] = 'omh:physical-activity';
      schemas['fitbit']['steps'] = 'omh:step-count';
      schemas['fitbit']['weight'] = 'omh:body-weight';
      schemas['fitbit']['body_mass_index'] = 'omh:body-mass-index';
      schemas['fitbit']['sleep'] = 'omh:sleep-duration';
      schemas['googlefit']['activity'] = 'omh:physical-activity';
      schemas['googlefit']['body_height'] = 'omh:body-height';
      schemas['googlefit']['body_weight'] = 'omh:body-weight';
      schemas['googlefit']['heart_rate'] = 'omh:heart-rate';
      schemas['googlefit']['step_count'] = 'omh:step-count';
      schemas['googlefit']['calories_burned'] = 'omh:calories-burned';
      schemas['jawbone']['activity'] = 'omh:physical-activity';
      schemas['jawbone']['weight2'] = 'omh:body-weight';
      schemas['jawbone']['body_mass_index2'] = 'omh:body-mass-index';
      schemas['jawbone']['steps'] = 'omh:step-count';
      schemas['jawbone']['sleep'] = 'omh:sleep-duration';
      schemas['jawbone']['heart_rate2'] = 'omh:heart-rate';
      schemas['misfit']['activities'] = 'omh:physical-activity';
      schemas['misfit']['steps'] = 'omh:step-count';
      schemas['misfit']['sleep'] = 'omh:sleep-duration';
      schemas['runkeeper']['activity'] = 'omh:physical-activity';
      schemas['runkeeper']['calories'] = 'omh:calories-burned';
      schemas['withings']['blood_pressure'] = 'omh:blood-pressure';
      schemas['withings']['body_height'] = 'omh:body-height';
      schemas['withings']['body_weight'] = 'omh:body-weight';
      schemas['withings']['heart_rate'] = 'omh:heart-rate';
      schemas['withings']['steps3'] = 'omh:step-count';
      schemas['withings']['calories3'] = 'omh:calories-burned';
      schemas['withings']['sleep4'] = 'omh:sleep-duration';

      for (var shim in schemas) {
          for (var endpoint in schemas[shim]) {
              this.endpoints[schemas[shim][endpoint]] = { shim: shim, endpoint: endpoint };
          }
      }

    }

    public searchUsers(searchTerm: string): angular.IPromise<angular.resource.IResourceArray<AuthorizationsResourceDefinition>> {
        return this.AuthorizationsResource.query({ username: searchTerm }).$promise;
    }

    public updateAuthorizations(userId): angular.IPromise<angular.resource.IResourceArray<AuthorizationsResourceDefinition>> {
        return this.AuthorizationsResource.query({ username: userId },
            (authorizations: angular.resource.IResourceArray<Authorizations>): void => {
                // wipe out all auth flags
                for (var shimName in this.shims) {
                    this.shims[shimName].authenticated = false;
                }
                // set all auth flags present in response
                authorizations[0].auths.forEach((shimName: string): void => {
                    if (this.shims.hasOwnProperty(shimName)) {
                        this.shims[shimName].authenticated = true;
                    }
                });
            }).$promise;
    }

    public updateConfigurations(): angular.IPromise<angular.resource.IResourceArray<ConfigurationResourceDefinition>> {

        return this.ConfigurationResource.query({}, this.updateShimProperty((serverConfig: Configuration): void => {
            var map = {};

            serverConfig.values.forEach((value: ConfigurationValue): void=> {
                map[value.settingId] = value;
            });
            serverConfig.values = map;
            serverConfig.settings.forEach((setting: ConfigurationSetting): void=> {
                if (setting.type == 'integer') {
                    serverConfig.values[setting.settingId].value = parseInt(serverConfig.values[setting.settingId].value);
                }
                if (setting.type == 'float') {
                    serverConfig.values[setting.settingId].value = parseFloat(serverConfig.values[setting.settingId].value);
                }
            });

            if (!this.shims[serverConfig.shimName].configuration) {
                this.shims[serverConfig.shimName].configuration = new ConfigurationImpl(serverConfig.shimName);
            }
            this.shims[serverConfig.shimName].configuration.settings = serverConfig.settings;
            this.shims[serverConfig.shimName].configuration.values = serverConfig.values;

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



    private pruneShims(shimNames: string[]) {
        // clear out shims that are nolonger there
        Object.keys(this.shims).forEach((shimName: string): void => {
            if (shimNames.indexOf(shimName) < 0) {
                delete this.shims[shimName];
            }
        });
        // add empty new shims
        shimNames.forEach((shimName: string): void => {
            if (!this.shims.hasOwnProperty(shimName)) {
                this.shims[shimName] = {
                    name: shimName,
                    schemas: [],
                    configuration: null,
                    authenticated: false
                };
            }
        });
    }
    private responseShimNames(responseArray: any): string[] {
        return responseArray.map((response: any): string => { return response.shimName; });
    }

    // generic method for updating a property of a shim using a callback that
    // handles the specific type of resource queried.
    private updateShimProperty(propertyCallback: (property: ShimPropertyResource) => void): (newProperties: ShimPropertyResource[]) => ShimPropertyResource[] {

        var callback = propertyCallback;

        return (newProperties: ShimPropertyResource[]) => {

            var shimNames = this.responseShimNames(newProperties);
            this.pruneShims(shimNames);

            newProperties.forEach((newProperty: ShimPropertyResource): void => {
                callback(newProperty);
            });

            return newProperties;

        }

    }

    /**
     * Initiate OAuth flow for the given username
     * @param userId - user id for which the flow is being authorized.
     * generated.
     * @param shimKey   - Shim to authorize
     * @param callback   - function to call once the authorization popup is closed
     */
    public initOAuthFlow(userId: string, shimKey: string, callback: () => void): void {
        var url = this.apiUrl + "/authorize/" + shimKey + "?username=" + userId;
        var self = this;
        this.$http.get(url)
            .success(function(data: any) {
                console.info("Retrieved authorization URL: ", data.authorizationUrl);
                var left = (screen.width / 2) - (500 / 2);
                var top = (screen.height / 2) - (500 / 2);
                var specs = 'resizable=0,scrollbars=1,width=500'
                    + ',height=500,left=' + left + ",top=" + top;
                var newTab = self.$window.open(data.authorizationUrl, '_blank', specs);
                /**
                 * Continuously check if the window has been closed.
                 * then refresh.
                 */
                var interval = window.setInterval(function() {
                    try {
                        if (newTab == null || newTab.closed) {
                            window.clearInterval(interval);
                            callback();
                        }
                    }
                    catch (e) {
                    }
                }, 1000);
                newTab.focus();
            }).error(function(data, status) {
                console.error("Error querying the registry, try again.", status);
            });
    }


    /**
     * Disconnects a user from a shim, removes all authorizations.
     * @param userId
     * @param shimKey
     * @param callback   - function to call once the disconnection is successful
     */
    public disconnect(userId: string, shimKey: string, callback: () => void) {

        if (this.$window.confirm("Disconnect this shim, are you sure?")) {

            var url = this.apiUrl + "/de-authorize/" + shimKey + "?username=" + userId;
            this.$http({
                url: url,
                method: 'DELETE'
            }).success(function() {
                console.info("successfully disconnected.");
                callback();
            }).error(function(data, status) {
                console.error("Could not disconnect, " +
                    "error occurred.", data, status);
            });

        }
    }

    private getEndpoint( parameters: RequestParameters ): string{
        console.log(parameters.schema.namespace + ':' + parameters.schema.name + ' = ' + this.endpoints[parameters.schema.namespace + ':' + parameters.schema.name]);
        return this.endpoints[parameters.schema.namespace + ':' + parameters.schema.name];
    }
    private getShimKey( parameters: RequestParameters ): string{
        return parameters.shim.name.toLowerCase();
    }

    /**
     * Retrieves an HTML-safe ID for a username. This is used to flag
     * components within the UI's templates
     */
    private getHtmlId(username){
        return username.replace(/\W+/g, "_");
    };

    /**
     * Retrieve data from shim server for the given
     * endpoint.
     *
     * @param user - The record whose data is being retrieved.
     * @param shimKey - The shim to use to pull data.
     * @param endPoint - The shim endpoint.
     * @param doNormalize - if true, data will be converted to OMH format,
     * if not it will be retrieved raw.
     */
    public getData(parameters: RequestParameters): angular.IHttpPromise<any> {

        console.info("The URL to be used is: ", parameters.url);

        return this.$http.get(parameters.url)
            .success(function (data) {
                console.log(data);
                // $(responseBox).val(JSON.stringify(data, undefined, 2));
                // $(spinner).css("display", "none");
                // $(responseBox).css("display", "block");  
            })
            .error(function (data, status) {
                var msg = "Error, could not get data from server" + status;
                console.error(msg);
                // $(error).css("display", "block");
                // $(error).html(msg);
                // $(spinner).css("display", "none");
            });
    };

    public getRequestUrl(user: User, parameters: RequestParameters, doNormalize = true) {
      var endPoint = this.getEndpoint( parameters );
      var shimKey = this.getShimKey( parameters );

      var suffix = this.getHtmlId(user.id) + "-" + shimKey + "-" + endPoint;
      // var error = $("#shim-error-" + suffix)[0];
      // var spinner = $("#shim-spinner-" + suffix)[0];
      // var responseBox = $("#shim-results-" + suffix)[0];
      var startDate = moment( parameters.startDate ).format("YYYY-MM-DD");
      var endDate = moment( parameters.endDate ).format("YYYY-MM-DD");

      if (!startDate || startDate == "") {
          startDate = moment().subtract(2, "days").format("YYYY-MM-DD");
      }

      if (!endDate || endDate == "") {
          endDate = moment().add(1, "days").format("YYYY-MM-DD");
      }

      /*
       * Hide previous results, show spinner.
       */
      // $(error).css("display", "none");
      // $(responseBox).css("display", "none");
      // $(spinner).css("display", "block");

      return this.apiUrl + "/data/" + shimKey + "/" + endPoint + "?"
          + "username=" + user.id
          + "&dateStart=" + startDate + "&dateEnd=" + endDate
          + (doNormalize ? "&normalize=true" : "&normalize=false");

    }



}












