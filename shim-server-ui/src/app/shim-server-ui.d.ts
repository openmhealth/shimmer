

interface ShimPropertyResource {
  shimName: string;
}

// API Authorizations
interface Authorizations {
  auths: string[];
  username: string;
}
interface AuthorizationsResourceDefinition extends angular.resource.IResource<Authorizations>, Authorizations {
}
interface AuthorizationsResource extends angular.resource.IResourceClass<AuthorizationsResourceDefinition> {
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
    minlength?: number;
    maxlength?: number;
    min?: number;
    max?: number;
}
interface Configuration extends ShimPropertyResource {
    settings: ConfigurationSetting[];
    values: any;
}
interface ConfigurationImpl extends ShimPropertyResource {
    getSetting(serttingId: string): ConfigurationSetting;
    getValue(serttingId: string): ConfigurationValue;
}
interface ConfigurationResourceDefinition extends angular.resource.IResource<Configuration>, Configuration {
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
interface RequestParameters {
    shim: Shim;
    schema: Schema;
    dateType: string;
    startDate: string,
    endDate: Date,
    url: string;
}
