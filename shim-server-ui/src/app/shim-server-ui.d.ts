

interface ShimPropertyResource {
  shimName: string;
}

// API Authorizations
interface Authorizations extends angular.resource.IResource<Authorizations> {
  auths: string[];
  username: string;
}

interface AuthorizationsResource extends angular.resource.IResourceClass<Authorizations> {
  update( authorizations: Authorizations): Authorizations;
}


// Users
interface User {
    id: string;
    authorizations: any[];
    statistics?: any;
}


// Configurations
interface ConfigurationSetting {
    settingId: string;
    type: string;          // ['string','boolean','integer','float']
    required: boolean;
    label: string;
    description?: string;
    length?: number;
    min?: number;
    max?: number;
}
interface Configuration extends ShimPropertyResource {
    settings: ConfigurationSetting[];
    values: any;
}
interface ConfigurationResourceDefinition extends angular.resource.IResource<Configuration> {
}
interface ConfigurationResource extends angular.resource.IResourceClass<ConfigurationResourceDefinition> {
    update(configuration: Configuration): angular.IPromise<Configuration>;
}
interface ConfigurationValue {
    settingId: string,
    value: any;
}


// Schemas and lists of schemas
interface Schema {
    namespace: string;
    name: string;
    version: string;
}
interface SchemaList extends ShimPropertyResource {
    schemas: Schema[];
}
interface SchemaListResourceDefinition extends angular.resource.IResource<SchemaList> {
}
interface SchemaListResource extends angular.resource.IResourceClass<SchemaListResourceDefinition> {
    update(schema: SchemaList): SchemaList;
}


// Shims
interface Shim {
    name: string;
    schemas: Schema[];
    configuration: Configuration;
    statistics?: any;
    authenticated: boolean;
}
interface ShimMap {
    [shimName: string]: Shim;
}

//Requests
interface Request {
    shim: Shim;
    schema: Schema;
    dates: Date[];
    url: string;
}
