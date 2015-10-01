'use strict';

/**
 * Simple UI for managing shim data on the shim server.
 */
angular.module('sandboxConsoleApp')
    .controller('AuthorizationCompleteCtrl', ['$scope', '$http', '$window', '$timeout','$routeParams', function ($scope, $http, $window, $timeout, $routeParams) {
        $timeout(function() {
            $window.close();
        }, 3000);
        $scope.error = $routeParams.errorState=='failure';
    }])
    .controller('MainCtrl', ['$scope', '$http', '$window', function ($scope, $http, $window) {

        var API_ROOT_URL = "/omh-shims-api";

        /**
         * A list of all the shims currently supported by the shim server. Whether configured
         * or not.
         * @type {Array}
         */
        $scope.availables = [];

        /**
         * Configurations for shim server shims.
         */
        $scope.shims = [];

        /**
         * Records received from the server based on search term.
         */
        $scope.records = [];

        /**
         * Simple flag for navigating between settings and list page.
         * @type {boolean}
         */
        $scope.settingsOpen = false;

        /**
         * Retrieves an HTML-safe ID for a username. This is used to flag
         * components within the UI's templates
         */
        $scope.getHtmlId = function(username){
            return username.replace(/\W+/g, "_");
        };

        /**
         * Default date parameters for date pickers.
         */
        $scope.fromDate = moment().subtract(2, "days").format("MM/DD/YYYY");
        $scope.toDate = moment().add(1, "days").format("MM/DD/YYYY");

        /**
         * Simple function to flip between shims and settings.
         */
        $scope.goto = function ($event, section) {
            $event.preventDefault();
            $event.stopPropagation();

            $(".shim-nav li").removeClass("active");

            var elm = $($event.currentTarget)[0];
            $(elm).parent().addClass("active");
            $scope.settingsOpen = section == 'settings';
        };

        /**
         * Allows user to save clientId/clientSecret settings
         * in the form.
         * @param shimKey - The shim whose settings we're trying
         * to save.
         */
        $scope.saveSettings = function (shimKey) {
            var clientId = $($("#clientId-" + shimKey)[0]).val();
            var clientSecret = $($("#clientSecret-" + shimKey)[0]).val();
            var spinner = $("#settings-spinner-" + shimKey);

            spinner.show();

            var url = API_ROOT_URL + "/shim/" + shimKey + "/config";
            url += "?clientId=" + clientId + "&clientSecret=" + clientSecret;

            $http({
                url: url,
                method: 'PUT'
            }).success(function () {
                console.info("successfully updated settings.");
                spinner.hide();
                $scope.loadShims();
            }).error(function (data, status) {
                spinner.hide();
                console.error("Could not update settings, " +
                    "error occurred.", data, status);
            });
        };

        /**
         * Loads all the available authorizations
         * from the shim server
         */
        $scope.loadShims = function () {
            var url = API_ROOT_URL + "/registry";
            $http.get(url)
                .success(function (data) {
                    $scope.shims = data;
                }).error(function (data, status) {
                    console.error("Error querying the registry, try again.", status);
                });
            $http.get(url + "?available=true")
                .success(function (data) {
                    $scope.availables = data;
                }).error(function (data, status) {
                    console.error("Error querying the registry, " +
                        "could not retrieve available shims.", status);
                });
        };

        /**
         * Retrieve settings for a specific shim.
         */
        $scope.getShim = function (shimKey) {
            var elm = {};
            if (!$scope.shims || $scope.shims.length == 0) {
                return elm;
            }
            angular.forEach($scope.shims, function (value, key) {
                if (value.shimKey == shimKey) {
                    elm = value;
                }
            });
            return elm;
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
            var url = API_ROOT_URL + "/authorizations?username=" + searchTerm.trim();
            $http.get(url)
                .success(function (data) {
                    console.info("The data is: ", data);
                    $scope.records = data;
                    if (data.length == 0) {
                        $scope.records.push({username: searchTerm, auths: []})
                    }
                }).error(function (data, status) {
                    console.error("Error doing lookup, nothing found!", status);
                });
        };

        /**
         * Initiate OAuth flow for the given username
         * @param record - user record for which the flow is being authorized.
         * generated.
         * @param shimKey   - Shim to authorize
         */
        $scope.initOAuthFlow = function (record, shimKey) {
            var url = API_ROOT_URL + "/authorize/" + shimKey + "?username=" + record.username;
            $http.get(url)
                .success(function (data) {
                    console.info("Retrieved authorization URL: ", data.authorizationUrl);
                    var left = (screen.width / 2) - (500 / 2);
                    var top = (screen.height / 2) - (500 / 2);
                    var specs = 'resizable=0,scrollbars=1,width=500'
                        + ',height=500,left=' + left + ",top=" + top;
                    var newTab = $window.open(data.authorizationUrl, '_blank', specs);
                    /**
                     * Continuously check if the window has been closed.
                     * then refresh.
                     */
                    var interval = window.setInterval(function () {
                        try {
                            if (newTab == null || newTab.closed) {
                                window.clearInterval(interval);
                                $scope.doLookup();
                            }
                        }
                        catch (e) {
                        }
                    }, 1000);
                    newTab.focus();
                }).error(function (data, status) {
                    console.error("Error querying the registry, try again.", status);
                });
        };

        /**
         * Disconnects a user from a shim, removes all authorizations.
         * @param $event
         * @param record
         * @param shimKey
         */
        $scope.disconnect = function ($event, record, shimKey) {

            if ($window.confirm("Disconnect this shim, are you sure?")) {
                $event.preventDefault();
                $event.stopPropagation();

                var url = API_ROOT_URL + "/de-authorize/" + shimKey + "?username=" + record.username;
                $http({
                    url: url,
                    method: 'DELETE'
                }).success(function () {
                    console.info("successfully disconnected.");
                    $scope.doLookup();
                }).error(function (data, status) {
                    console.error("Could not disconnect, " +
                        "error occurred.", data, status);
                });
            }
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
            var suffix = $scope.getHtmlId(record.username) + "-" + shimKey + "-" + endPoint;
            var error = $("#shim-error-" + suffix)[0];
            var spinner = $("#shim-spinner-" + suffix)[0];
            var responseBox = $("#shim-results-" + suffix)[0];
            var fromDate = moment(new Date($($("#fromDate-" + suffix)[0]).val())).format("YYYY-MM-DD");
            var toDate = moment(new Date($($("#toDate-" + suffix)[0]).val())).format("YYYY-MM-DD");

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

            var url = API_ROOT_URL + "/data/" + shimKey + "/" + endPoint + "?"
                + "username=" + record.username
                + "&dateStart=" + fromDate + "&dateEnd=" + toDate
                + (doNormalize ? "&normalize=true" : "&normalize=false");

            console.info("The URL to be used is: ", url);

            $http.get(url)
                .success(function (data) {
                    $(responseBox).val(JSON.stringify(data, undefined, 2));
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
         * @param event - event that triggered this.
         * @param record
         * @param shimKey
         */
        $scope.openEndpoints = function (event, record, shimKey) {
            var elm = $("#shim-panel-" + $scope.getHtmlId(record.username) + "-" + shimKey)[0];
            console.info("Looking for element: ",
                    "#shim-panel-" + $scope.getHtmlId(record.username) + "-" + shimKey);
            console.info("Element is: ", elm.id);

            if ($(elm).css("display") == "none") {
                $(elm).slideDown(250);
            } else {
                $(elm).slideUp(250);
            }
        };

        /**
         * Opens the date picker to retrieve data from a specific endpoint.
         * @param type
         * @param record
         * @param shimKey
         * @param endpoint
         */
        $scope.pickDate = function (type, record, shimKey, endpoint) {
            var elm = $("#"+type + "Date-" +
                $scope.getHtmlId(record.username) + "-" + shimKey + "-" + endpoint)[0];
            $(elm).focus();
        };

        /*
         * Loads the shims from the shim registry.
         */
        $scope.loadShims();
    }]);
