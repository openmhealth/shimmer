'use strict';

/**
 * Simple UI for managing shim data on the shim server.
 */
angular.module('sandboxConsoleApp')
    .controller('MainCtrl', function ($scope) {

        /**
         * Configurations for shim server shims.
         */
        $scope.shims = [
            {
                shimKey: 'fitbit',
                label: 'Fitbit',
                description: 'Description of fitbit',
                endpoints: [
                    {name: 'body', description: 'body measurements'},
                    {name: 'steps', description: 'steps stuff'}
                ]
            },
            {
                shimKey: 'healthvault',
                label: 'Microsoft Healthvault',
                description: 'Description of healthvault',
                endpoints: [
                    {name: 'blood-pressure', description: 'blood pressure'},
                    {name: 'activity', description: 'blood pressure'}
                ]
            },
            {
                shimKey: 'runkeeper',
                label: 'Runkeeper',
                description: 'Description of runkeeper',
                endpoints: [
                    {name: 'body', description: 'body measurements'}
                ]
            },
            {
                shimKey: 'fatsecret',
                label: 'Fat secret',
                description: 'Description of fatsecret',
                endpoints: [
                    {name: 'body', description: 'body measurements'}
                ]
            },
            {
                shimKey: 'jawbone',
                label: 'Jawbone UP',
                description: 'Description of jawbone',
                endpoints: [
                    {name: 'body', description: 'body measurements'}
                ]
            },
            {
                shimKey: 'withings',
                label: 'Withings',
                description: 'Description of withings endpoint',
                endpoints: [
                    {name: 'body', description: 'body measurements'}
                ]
            }
        ];

        /**
         * Records received from the server based on search term.
         */
        $scope.records = [
            {
                username: 'Anna',
                auths: ['fitbit', 'runkeeper']
            },
            {
                username: 'David',
                auths: ['healthvault']
            }
        ];

        /**
         * Perform a lookup against the shim server.
         *
         * @param searchTerm - term to
         */
        $scope.doLookup = function(searchTerm){

        };

        /**
         * Opens the 'endpoints' panel below a shim, which allows
         * the user to input a date range and retrieve data.
         *
         * @param event
         * @param record
         * @param shimKey
         */
        $scope.openEndpoints = function (event, record, shimKey) {
            var elm = $("#shim-panel-"+record.username+"-"+shimKey)[0];
            console.info("Looking for element: ","#shim-panel-"+record.username+"-"+shimKey);
            console.info("Element is: ", elm.id);

            if($(elm).css("display") == "none"){
                $(elm).slideDown(250);
            }else{
                $(elm).slideUp(250);
            }
        };
    });
