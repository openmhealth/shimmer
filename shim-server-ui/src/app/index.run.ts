/** @ngInject */
export function runMock($httpBackend: angular.IHttpBackendService) {

    var configuration = {
        shimName: 'Withings',
        directives: <IConfigurationDirective[]>[
            <IConfigurationDirective>{
                configPath: 'string.test.path',
                type: 'string',
                length: 12,
                label: 'String mock',
                description: 'A required string mock that must be 12 characters',
                required: true
            },
            <IConfigurationDirective>{
                configPath: 'boolean.test.path',
                type: 'boolean',
                label: 'Boolean mock',
                description: 'An optional boolean with a super long description that must be truncated.',
                required: false
            },
            <IConfigurationDirective>{
                configPath: 'integer.test.path',
                type: 'integer',
                min: 0,
                max: 100,
                label: 'Integer mock with a really long label',
                required: true
            },
            <IConfigurationDirective>{
                configPath: 'float.test.path',
                type: 'float',
                label: 'Float mock',
                required: false
            }
        ],
        values: [
          { settingId:'string.test.path', value:'dfhg29h92020' },
          { 'boolean.test.path': 'false' },
          { 'integer.test.path': '3' },
          { 'float.test.path': '1.01' },
        ]
    };

    var schemas = <ISchemaList>{
        shimName: 'Withings',
        schemas: <ISchema[]>[
                <ISchema>{
                    'namespace': 'granola',
                    name: 'schema-name',
                    version: '1.x',
                    measures: [
                      'distance',
                      'pies eaten'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-name',
                    version: '1.2',
                    measures: [
                        'step count',
                        'meatball index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-name',
                    version: '1.1',
                    measures: [
                        'step count',
                        'melbatoast index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-name',
                    version: '1.2',
                    measures: [
                        'step count',
                        'meatball index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-with-name',
                    version: '1.11',
                    measures: [
                        'step count',
                        'melbatoast index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-schema-name',
                    version: '1.2',
                    measures: [
                        'step count',
                        'meatball index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-other-name',
                    version: '1.1',
                    measures: [
                        'step count',
                        'melbatoast index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-name',
                    version: '1.2',
                    measures: [
                        'step count',
                        'meatball index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schema-name-also',
                    version: '1.1',
                    measures: [
                        'step count',
                        'melbatoast index'
                    ]
                },
                <ISchema>{
                    'namespace': 'omh',
                    name: 'schemas-name',
                    version: '1.2',
                    measures: [
                        'step count',
                        'meatball index'
                    ]
                },
                <ISchema>{
                    'namespace': 'granola',
                    name: 'schema-name',
                    version: '1.1',
                    measures: [
                        'step count',
                        'melbatoast index'
                    ]
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
  $httpBackend.whenGET(/app\//).passThrough();

}


