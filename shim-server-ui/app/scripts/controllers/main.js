'use strict';

/**
 * Simple UI for managing shim data on the shim server.
 */
angular.module('sandboxConsoleApp')
    .controller('MainCtrl', ['$scope', '$http', function ($scope, $http) {

        /**
         * Configurations for shim server shims.
         */
        $scope.shims = [
            /*{
             shimKey: 'fitbit',
             label: 'Fitbit',
             endpoints: ['body', 'steps']
             },
             {
             shimKey: 'healthvault',
             label: 'Microsoft Healthvault',
             endpoints: ['blood-pressure', 'activity']
             },
             {
             shimKey: 'runkeeper',
             label: 'Runkeeper',
             endpoints: ['body']
             },
             {
             shimKey: 'fatsecret',
             label: 'Fat secret',
             endpoints: ['body']
             },
             {
             shimKey: 'jawbone',
             label: 'Jawbone UP',
             endpoints: ['body']
             },
             {
             shimKey: 'withings',
             label: 'Withings',
             endpoints: ['body']
             }*/
        ];

        /**
         * Records received from the server based on search term.
         */
        $scope.records = [
            /*{
             username: 'Anna',
             auths: ['fitbit', 'runkeeper']
             },
             {
             username: 'David',
             auths: ['healthvault']
             }*/
        ];


        /**
         * Loads all the available authorizations
         * from the shim server
         */
        $scope.loadShims = function () {
            var url = "/api/registry";
            $http.get(url)
                .success(function (data) {
                    $scope.shims = data;
                }).error(function (data, status) {
                    console.error("Error querying the registry, try again.", status);
                });
        };

        /**
         * Perform a lookup against the shim server.
         * Authorizations will be retrieved for records that
         * match the search terms.
         *
         * fragment of a name.
         */
        $scope.doLookup = function () {
            var searchTerm = $($("#uid-term")[0]).val();
            var url = "/api/authorizations?username=" + searchTerm.trim();
            $http.get(url)
                .success(function (data) {
                    $scope.records = data;
                }).error(function (data, status) {
                    console.error("Error querying the registry, try again.", status);
                });
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
            var error = $("#shim-error-" + suffix)[0];
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
            $(error).css("display", "none");
            $(responseBox).css("display", "none");
            $(spinner).css("display", "block");

            var url = "/api/data/" + shimKey + "/" + endPoint + "?"
                + "username=" + record.username
                + "&dateStart=" + fromDate + "&dateEnd=" + toDate
                + (doNormalize ? "&normalize=true" : "");

            console.info("The URL to be used is: ", url);

            $http.get(url)
                .success(function (data) {
                    $(responseBox).val(JSON.stringify(data,undefined,2));
                    $(spinner).css("display", "none");
                    $(responseBox).css("display", "block");
                })
                .error(function (data, status) {
                    var msg = "Error, could not get data from server" + status;
                    console.error(msg);
                    $(error).css("display", "block");
                    $(error).html(msg);
                    $(spinner).css("display", "none");
                });
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

        /*
         * Loads the shims from the shim registry.
         */
        $scope.loadShims();
    }]);
