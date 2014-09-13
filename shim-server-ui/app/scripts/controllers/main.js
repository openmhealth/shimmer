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
        $scope.doLookup = function (searchTerm) {

        };


        /**
         * Retrieve data from shim server for the given
         * endpoint.
         *
         * @param record - The record whose data is being retrieved.
         * @param shimKey - The shim to use to pull data.
         * @param endPoint - The shim endpoint.
         * @param doNormalize - if true, data will be converted to OMH format,
         * if not it will be retrieved raw.
         */
        $scope.getData = function (record, shimKey, endPoint, doNormalize) {
            var suffix = record.username + "-" + shimKey + "-" + endPoint;
            var spinner = $("#shim-spinner-" + suffix)[0];
            var responseBox = $("#shim-results-" + suffix)[0];
            var fromDate = $($("#fromDate-" + suffix)[0]).val();
            var toDate = $($("#toDate-" + suffix)[0]).val();

            if (!fromDate || fromDate == "") {
                fromDate = moment().subtract(2, "days").format("YYYY-MM-DD");
            }

            if (!toDate || toDate == "") {
                toDate = moment().add(1, "days").format("YYYY-MM-DD");
            }

            /*
             * Hide previous results, show spinner.
             */
            $(responseBox).css("display", "none");
            $(spinner).css("display", "block");

            var url = "data/" + shimKey + "/" + endPoint + "?"
                + "dateStart=" + fromDate + "&dateEnd=" + toDate
                + (doNormalize ? "&normalize=true" : "");

            console.info("The URL to be used is: ", url);

            setTimeout(function () {
                /*
                 * Hide spinner, show results.
                 */
                $(spinner).css("display", "none");
                $(responseBox).css("display", "block");
            }, 1000);
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
            var elm = $("#shim-panel-" + record.username + "-" + shimKey)[0];
            console.info("Looking for element: ",
                    "#shim-panel-" + record.username + "-" + shimKey);
            console.info("Element is: ", elm.id);

            if ($(elm).css("display") == "none") {
                $(elm).slideDown(250);
            } else {
                $(elm).slideUp(250);
            }
        };
    });
