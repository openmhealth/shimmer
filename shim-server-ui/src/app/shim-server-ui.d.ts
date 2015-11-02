interface IAuthorizations extends angular.resource.IResource<IAuthorizations> {
  auths: string[];
  username: string;
}

interface IAuthorizationsResource extends angular.resource.IResourceClass<IAuthorizations> {
  update( authorizations: IAuthorizations): IAuthorizations;
}

interface IUser {
    id: string;
    authorizations: any[];
    statistics?: any;
}

interface IConfigurationDirective {
    configPath: string;
    type: string;          // ['string','boolean','integer','float']
    required: boolean;
    label: string;
    description?: string;
    length?: number;
    min?: number;
    max?: number;
}

interface IShimPropertyResource {
  shimName: string;
}



interface IConfiguration extends IShimPropertyResource {
    directives: IConfigurationDirective[];
    values: any;
}
interface IConfigurationResourceDefinition extends angular.resource.IResource<IConfiguration> {
}
interface IConfigurationResource extends angular.resource.IResourceClass<IConfigurationResourceDefinition> {
    update(configuration: IConfiguration): angular.IPromise<IConfiguration>;
}
interface IConfigurationValue {
    [settingId: string]: any;
}



interface ISchema {
    namespace: string;
    name: string;
    version: string;
    measures: string[];
}



interface ISchemaList extends IShimPropertyResource {
    schemas: ISchema[];
}
interface ISchemaResourceDefinition extends angular.resource.IResource<ISchemaList> {
}
interface ISchemaResource extends angular.resource.IResourceClass<ISchemaResourceDefinition> {
    update(schema: ISchemaList): ISchemaList;
}



interface IShim {
    name: string;
    schemas: ISchema[];
    configuration: IConfiguration;
    statistics?: any;
    authenticated: boolean;
}

interface IShimMap {
    [shimName: string]: IShim;
}

interface IRequest {
    shim: IShim;
    schema: ISchema;
    dates: Date[];
    url: string;
}


/*************************


User
  Authorizations
  id
  Statistics
    ...

Configuration
  Directives[]
    configPath
    type          // ['string','boolean','integer','float']
    length        // (optional)
    label
    description    // (optional)
    required

Shim
  name
  Schemas
  Statistics
    ...
  Configuration

Request
  Shim
  Schema
  Dates
    start
    end
  url

**************************/