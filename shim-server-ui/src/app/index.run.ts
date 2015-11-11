/** @ngInject */
export function runMock($httpBackend: angular.IHttpBackendService) {

    var configuration = {
        shimName: 'withings',
        settings: <ConfigurationSetting[]>[
            <ConfigurationSetting>{
                settingId: 'string.test.path',
                type: 'string',
                minlength: 12,
                maxlength: 14,
                label: 'String mock',
                description: 'A required string mock that must be 12 characters',
                required: true
            },
            <ConfigurationSetting>{
                settingId: 'boolean.test.path',
                type: 'boolean',
                label: 'Boolean mock',
                description: 'An optional boolean with a super long description that must be truncated.',
                required: false
            },
            <ConfigurationSetting>{
                settingId: 'integer.test.path',
                type: 'integer',
                min: 0,
                max: 100,
                label: 'Integer mock with a really long label',
                required: true
            },
            <ConfigurationSetting>{
                settingId: 'float.test.path',
                type: 'float',
                label: 'Float mock',
                required: false
            }
        ],
        values: [
          { settingId:'string.test.path', value:'dfhg29h92020' },
          { settingId:'boolean.test.path', value: 'false' },
          { settingId:'integer.test.path', value: '3' },
          { settingId:'float.test.path', value: '1.01' },
        ]
    };

    var schemas = <SchemaList>{
        shimName: 'withings',
        schemas: <Schema[]>[
            <Schema>{
                'namespace': 'omh',
                name: 'body-weight',
                version: '1.0',
            },
            <Schema>{
                'namespace': 'omh',
                name: 'heart-rate',
                version: '1.0',
            }
        ]
      };

    // returns the current list of configurations
    $httpBackend.whenGET(/\/api\/configuration/).respond([configuration]);
    // returns the current list of schemas
    $httpBackend.whenGET(/\/api\/schemas/).respond([schemas]);

    // adds a new phone to the phones array
    $httpBackend.whenPOST(/^\/api\/.+\/configuration/).respond(function(method: any, url: string, data: string) {
    var newConfiguration = angular.fromJson(data);
    configuration = newConfiguration;
    return [200, newConfiguration, {}];
  });

    $httpBackend.whenGET(/^\/api\/[.+\/]?authorizations/).passThrough();
    $httpBackend.whenGET(/^\/api\/[.+\/]?authorize/).passThrough();
    $httpBackend.whenGET(/^\/api\/data\/.+/).passThrough();
    $httpBackend.whenDELETE(/^\/api\/[.+\/]?de-authorize\/.+/).passThrough();
    $httpBackend.whenGET(/app\//).passThrough();

}


