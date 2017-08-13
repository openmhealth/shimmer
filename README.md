# Shimmer [![Build Status](https://travis-ci.org/openmhealth/shimmer.svg?branch=develop)](https://travis-ci.org/openmhealth/shimmer) [![Join the chat at https://gitter.im/openmhealth/shimmer](https://badges.gitter.im/openmhealth/shimmer.svg)](https://gitter.im/openmhealth/shimmer?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Shimmer is an application that makes it easy to pull health data from popular third-party APIs like Runkeeper and Fitbit.
It converts that data into an [Open mHealth](http://www.openmhealth.org) compliant format, letting your application work with clean and clinically meaningful data.   

We currently support the following APIs

* [Fitbit](https://www.fitbit.com/)
* [Google Fit](https://developers.google.com/fit/?hl=en)
* [iHealth](http://www.ihealthlabs.com/)
* [Jawbone UP](https://jawbone.com/up)
* [Misfit](http://misfit.com/)
* [Moves](https://www.moves-app.com/)
* [RunKeeper](https://runkeeper.com/index)
* [Withings](http://www.withings.com/)

This README should have everything you need to get started. If you have any questions, feel free to [open an issue](https://github.com/openmhealth/shimmer/issues), [email us](mailto://support@openmhealth.org), [post on our form](https://groups.google.com/forum/#!forum/omh-developers), or [visit our website](http://www.openmhealth.org/documentation/#/data-providers/get-started).

## Contents
- [Overview](#overview)
	- [Shims](#shims)
	- [Resource server](#resource-server)
	- [Console](#console)
- [Installation](#installation)
	- [Option 1. Download and run Docker images](#option-1-download-and-run-docker-images)
	- [Option 2. Build the code and run it natively or in Docker](#option-2-build-the-code-and-run-it-natively-or-in-docker)
- [Setting up your credentials](#setting-up-your-credentials)
- [Authorizing access to a third-party user account](#authorizing-access-to-a-third-party-user-account)
	- [Authorize access from the console](#authorize-access-from-the-console)
	- [Authorize access programmatically](#authorize-access-programmatically)
- [Reading data](#reading-data)
	- [Read data using the console](#read-data-using-the-console)
	- [Read data programmatically](#read-data-programmatically)
- [Supported APIs and endpoints](#supported-apis-and-endpoints)
- [Contributing](#contributing)

## Overview 
Shimmer is made up of different components - individual shims, a resource server, and a console - which are each described below.

### Shims
A *shim* is a library that can communicate with a specific third-party API, e.g. Fitbit. It handles the process of authenticating with the API, requesting data from it, and mapping that data into an Open mHealth compliant data format. 

A shim generates *data points*, which are self-contained pieces of data that not only contain the health data of interest, but also include header information such as date of creation, acquisition provenance, and data source. This metadata helps describe the data and where it came from. The library is called a shim because such clean and clinically significant data is not provided natively by the third-party API.

### Resource server
The *resource server* exposes an API to retrieve data points. The server handles API requests by delegating them to the correct shim. As more and more shims are developed and added to the resource server, it becomes capable of providing data points from more and more third-party APIs. The resource server also manages third-party access tokens on behalf of shims.

### Console
The *console* provides a simple web interface that helps users interact with the resource server. It can set configuration parameters, trigger authentication flows, and request data using date pickers and drop downs.

## Installation

There are two ways to install Shimmer. 

1. You can download and run pre-built Docker images. 
1. You can build all the code from source and run it natively or in Docker.

### Option 1. Download and run Docker images

If you don't have Docker and Docker Compose, please set them up. ([Docker for Mac](https://docs.docker.com/docker-for-mac/), [Docker for Windows](https://docs.docker.com/docker-for-windows/).

Once you're set up, in a terminal 

1. Clone this Git repository.
1. Download and start the containers using either
  * `docker-compose up -d`
    * to bring up the resource server and the console
  * `docker-compose up -d resourceserver`
    * to bring up only the resource server
  * If you want to see logs and keep the containers in the foreground, omit the `-d`.
  * This will download up to 0.5 GB of Docker images if you don't already have them, the bulk of which are the underlying MongoDB, nginx and OpenJDK images. 
1. It can take up to a minute for the containers to start up. You can check their progress using `docker-compose logs` if you started with `-d`.
1. The console container publishes port 8083 and the resource server container publishes port 8084.
  * The console container proxies all API requests to the resource server container, so you can send API requests to port 8083 or port 8084.
1. Visit `http://<shimmer-host>:8083` in a browser to open the console.

### Option 2. Build the code and run it natively or in Docker

If you prefer to build the code yourself,  

1. You must have a Java 8 or higher JDK installed. You can use either [OpenJDK](http://openjdk.java.net/install/) or the [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. You technically don't need to run the console, but it makes your life easier. If you're building the console,
  1. You need [Node.js](http://nodejs.org/download/).
  1. You need [Xcode Command Line Tools](https://developer.apple.com/xcode/) if you're on a Mac.
1. To run the code natively,
  1. You need a running [MongoDB](http://docs.mongodb.org/manual/) instance.
1. To run the code in Docker,
  1. You need Docker and Docker Compose.

If you want to build and run the code natively, in a terminal
 
1. Clone this Git repository.
1. Run the `./run-natively.sh` script and follow the instructions.
1. When the script blocks with the message `Started Application`, the components are running.
  * Press Ctrl-C to stop them.
  * The script creates a WAR file which you can alternatively drop into an application server. [This issue](https://github.com/openmhealth/shimmer/issues/31) has details.
1. Visit `http://<shimmer-host>:8083` in a browser to open the console.

If you want to build and run the code in Docker, in a terminal 
 
1. Clone this Git repository.
1. Run the `./run-dockerized.sh` script and follow the instructions.
  * The containers should now be running on your Docker host and expose ports 8083 and 8084.
  * It can take up to a minute for the containers to start up.
1. Visit `http://<shimmer-host>:8083` in a browser to open the console.

> If you can't run the Bash scripts on your system, open them and take a look at the commands they run. The important commands are marked with a "#CMD" comment.

## Registering with third-party APIs

To get data from a third-party API, you need to visit the developer website of that API and register a
*client application*. The registration information that you give to the third-party lets them show relevant information to
their end users about your application, and lets them manage other operational concerns like authorization and rate limits.

You will be given a set of client credentials, usually an OAuth client ID and client secret, for each application you register.
You may also need to enter a redirect URL, which is the URL a user is sent to after granting your application access to
their data.

The following table contains a link to the developer portal of each API and information about redirect URL restrictions.
 The restrictions can be good to know about during development, but TLS and full URLs should be used during production.


API                                                               | requires TLS       | allows non-FQDN hostname | allows IP addresses | allows localhost | requires URL path | example
----------------------------------------------------------------- | ------------------ | ------------------------ | ------------------- | ---------------- | ----------------- | --------
[Fitbit](http://dev.fitbit.com/)<sup>1</sup>                      | false              | true                     | true                | true             | true              | http://localhost:8083/authorize/fitbit/callback
[Google Fit](https://console.developers.google.com/start)         | false              | false                    | false               | true             | ?                 | http://localhost:8083/authorize/googlefit/callback
[iHealth](http://developer.ihealthlabs.com/index.htm)<sup>2</sup> | ?                  | ?                        | ?                   | ?                | false             | http://localhost:8083/authorize/ihealth/callback
[Jawbone UP](https://jawbone.com/up/developer)                    | false <sup>3</sup> | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/jawbone/callback
[Misfit](https://build.misfit.com/)                               | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/misfit/callback
[Moves](https://dev.moves-app.com/)                               | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/moves/callback
[RunKeeper](http://runkeeper.com/partner)                         | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/runkeeper/callback
[Withings](http://oauth.withings.com/api)                         | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/withings/callback

<sup>1</sup> *Fitbit has deprecated OAuth 1.0a authorization in favour of OAuth 2.0. You will need OAuth 2.0 credentials.*

<sup>2</sup> *You'll need to copy the iHealth SC and SV values found via the [application management page](http://developer.ihealthlabs.com/developermanagepage.htm)
into the `application.yaml` or `resource-server.env` file.*

<sup>3</sup> *The [documentation](https://jawbone.com/up/developer/authentication) states TLS is required, but authorization does work without it.*

> If any of the links or fields are incorrect or out of date, please [submit an issue](https://github.com/openmhealth/shimmer/issues) to let us know.
> This table will be fully populated in in the coming days.

Visit the links to register and configure your application for each of the APIs you want to use. Once credentials are
obtained for a particular API, you can either set the corresponding values in the `application.yaml` file and rebuild, or if you're running using Docker, set the corresponding values in the `resource-server.env` file.


## Authorizing access to a third-party user account

The data produced by a third-party API belongs to some user account registered on the third-party system. To allow 
 a shim to read that data, you'll need to initiate an authorization process. This process lets the user account holder explicitly grant the shim access to their data.

### Authorize access from the console

To initiate the authorization process from the console,
 
1. Type in an arbitrary user handle. This handle can be anything, it's just your way of referring to third-party API users. 
1. Press *Find* and the console will show you a *Connect* button for each API with [configured](#setting-up-your-credentials) authentication credentials.
1. Click *Connect* and a pop-up will open.
1. Follow the authorization prompts. 
1. After following the prompts, you should see an `authorization successful` response in the pop-up. 
1. The pop-up will then automatically close.

### Authorize access programmatically

To initiate the authorization process programmatically,
 
1. Make a GET request to `http://<shimmer-host>:8083/authorize/{shimKey}?username={userId}`
  * Use port 8084 if you're not running the console container.
  * The `shimKey` path parameter should be one of the keys listed [below](#supported-apis-and-endpoints), e.g. `fitbit`. 
  * The `username` query parameter can be set to any unique identifier you'd like to use to identify the user. 
1. Find the `authorizationUrl` value in the returned JSON response and redirect your user to this URL. Your user will land on the third-party website where they can login and authorize access to their third-party user account. 
1. Once authorized, they will be redirected to `http://<<shimmer-host>:8083/authorize/{shimKey}/callback`. 

## Reading data
A shim can produce JSON data that is either *normalized* to Open mHealth schemas or in the *raw* format produced by the third-party API. Raw data is passed through from the third-party API. Normalized data conforms to [Open mHealth schemas](http://www.openmhealth.org/documentation/#/schema-docs/schema-library).

The following is an example of a normalized step count data point retrieved from Jawbone:

```json
{
    "header": {
        "id": "243c773b-8936-407e-9c23-270d0ea49cc4",
        "creation_date_time": "2015-09-10T12:43:39.138-06:00",
        "acquisition_provenance": {
            "source_name": "Jawbone UP API",
            "modality": "sensed",
            "source_updated_date_time": "2015-09-10T18:43:39Z"
        },
        "schema_id": {
            "namespace": "omh",
            "name": "step-count",
            "version": "1.0"
        }
    },
    "body": {
        "effective_time_frame": {
            "time_interval": {
                "start_date_time": "2015-08-06T05:11:09-07:00",
                "end_date_time": "2015-08-06T23:00:36-06:00"
            }
        },
        "step_count": 7939
    }
}
```

### Read data using the console

To pull data from a third-party API using the console,
 
1. Click the name of the connected third-party API.
1. Fill in the date range you're interested in.
1. Press the *Raw* button for raw data, or the *Normalized* button for data that has been converted to an Open mHealth compliant data format. 

### Read data programmatically

To pull data from a third-party API programmatically, make requests in the format
 
`http://<<shimmer-host>>:8083/data/{shimKey}/{endpoint}?username={userId}&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize={true|false}`

Use port 8084 if you're not running the console container.

The URL can be broken down as follows
* The `shimKey` and `username` path variables are the same as [above](#authorizing-access-to-a-third-party-user-account).
* The `endpoint` path variable corresponds to the type of data to retrieve. There's a table of these [below](#supported-apis-and-endpoints).
* The `normalize` parameter controls whether the shim returns data in a raw third-party API format (`false`) or in an Open mHealth compliant format (`true`).  

> N.B. This API may change significantly in the future to provide greater consistency across Open mHealth applications and to improve expressivity and ease of use. The data points it returns will not be affected, only the URLs used to request data and perhaps some book-keeping information at the top level of the response.
 
## Supported APIs and endpoints

The following is a table of the currently supported shims, their endpoints, the Open mHealth compliant data produced, and the corresponding mapper. The values in the `shimKey` and `endpoint` columns are the values for the parameters of the same names used in [programmatic access](#reading-data-programmatically) of the API.

The currently supported shims are:

| shim key              | endpoint                                                                                                         | OmH data produced by endpoint                                                                                                     | mapper                                                                                                                                                                                                                  |
| --------------------- | ---------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------   |
| fitbit<sup>1</sup>    | [body_mass_index](https://dev.fitbit.com/docs/body/#weight)                                                      | [omh:body-mass-index:2.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index)      | [FitbitBodyMassIndexDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitBodyMassIndexDataPointMapper.java)                           |
| fitbit<sup>1</sup>    | [body_weight](https://dev.fitbit.com/docs/body/#weight)                                                          | [omh:body-weight:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)              | [FitbitBodyWeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitBodyWeightDataPointMapper.java)                                 |
| fitbit<sup>1</sup>    | [heart_rate](https://dev.fitbit.com/docs/heart-rate/#get-heart-rate-intraday-time-series) (intraday)             | [omh:heart-rate:1.1](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)                | [FitbitIntradayHeartRateDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitIntradayHeartRateDataPointMapper.java)                   | 
| fitbit<sup>1</sup>    | [physical_activity](https://dev.fitbit.com/docs/activity)                                                        | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [FitbitPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitPhysicalActivityDataPointMapper.java)                     |
| fitbit<sup>1</sup>    | [sleep_duration](https://dev.fitbit.com/docs/sleep/#get-sleep-logs)                                              | [omh:sleep-duration:2.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration)        | [FitbitSleepDurationDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitSleepDurationDataPointMapper.java)                           |
| fitbit<sup>1</sup>    | [sleep_episode](https://dev.fitbit.com/docs/sleep/#get-sleep-logs)                                               | [omh:sleep-episode:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-episode)          | [FitbitSleepEpisodeDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitSleepEpisodeDataPointMapper.java)                             |
| fitbit<sup>1</sup>    | [step_count](https://dev.fitbit.com/docs/activity/#get-activity-time-series)<sup>2</sup>                         | [omh:step-count:2.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [FitbitStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitStepCountDataPointMapper.java)                                   |
| fitbit<sup>1</sup>    | [step_count](https://dev.fitbit.com/docs/activity/#get-activity-intraday-time-series) (intraday)<sup>2</sup>     | [omh:step-count:2.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [FitbitIntradayStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/fitbit/mapper/FitbitIntradayStepCountDataPointMapper.java)                   |
| googlefit             | [body_height](https://developers.google.com/fit/rest/v1/data-types)                                              | [omh:body-height:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-height)              | [GoogleFitBodyHeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitBodyHeightDataPointMapper.java)                        |
| googlefit             | [body_weight](https://developers.google.com/fit/rest/v1/data-types)                                              | [omh:body-weight:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)              | [GoogleFitBodyWeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitBodyWeightDataPointMapper.java)                        |
| googlefit             | [calories_burned](https://developers.google.com/fit/rest/v1/data-types)                                          | [omh:calories-burned:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned)      | [GoogleFitCaloriesBurnedDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitCaloriesBurnedDataPointMapper.java)                |
| googlefit             | [heart_rate](https://developers.google.com/fit/rest/v1/data-types)                                               | [omh:heart-rate:1.1](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)                | [GoogleFitHeartRateDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitHeartRateDataPointMapper.java)                          |
| googlefit             | [physical_activity](https://developers.google.com/fit/rest/v1/data-types)                                        | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [GoogleFitPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitPhysicalActivityDataPointMapper.java)            |
| googlefit             | [step_count](https://developers.google.com/fit/rest/v1/data-types)                                               | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [GoogleFitStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/googlefit/mapper/GoogleFitStepCountDataPointMapper.java)                          |
| ihealth               | [blood_glucose](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBG.htm)                       | [omh:blood-glucose:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-glucose)          | [IHealthBloodGlucoseDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthBloodGlucoseDataPointMapper.java)                          |
| ihealth               | [blood_pressure](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBloodPressure.htm)           | [omh:blood-pressure:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-pressure)        | [IHealthBloodPressureDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthBloodPressureDataPointMapper.java)                        |
| ihealth               | [body_mass_index](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofWeight.htm)                 | [omh:body-mass-index:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index)      | [IHealthBodyMassIndexDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthBodyMassIndexDataPointMapper.java)                        |
| ihealth               | [body_weight](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofWeight.htm)                     | [omh:body-weight:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)              | [IHealthBodyWeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthBodyWeightDataPointMapper.java)                              |
| ihealth               | [heart_rate](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofActivityReport.htm)              | [omh:heart-rate:1.1](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)                | [IHealthHeartRateDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthHeartRateDataPointMapper.java)                                |
| ihealth               | [physical_activity](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSport.htm)                | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [IHealthPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthPhysicalActivityDataPointMapper.java)                  |
| ihealth               | [sleep_duration](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSleepReport.htm)             | [omh:sleep-duration:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration)        | [IHealthSleepDurationDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthSleepDurationDataPointMapper.java)                        |
| ihealth               | [step_count](http://developer.ihealthlabs.com/dev_documentation_RequestfordataofActivityReport.htm)              | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [IHealthStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/ihealth/mapper/IHealthStepCountDataPointMapper.java)                                |
| jawbone               | [body_mass_index](https://jawbone.com/up/developer/endpoints/body)                                               | [omh:body-mass-index:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index)      | [JawboneBodyMassIndexDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawboneBodyMassIndexDataPointMapper.java)                        |
| jawbone               | [body_weight](https://jawbone.com/up/developer/endpoints/body)                                                   | [omh:body-weight:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)              | [JawboneBodyWeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawboneBodyWeightDataPointMapper.java)                              |
| jawbone               | [heart_rate](https://jawbone.com/up/developer/endpoints)<sup>3</sup>                                             | [omh:heart-rate:1.1](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)                | [JawboneHeartRateDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawboneHeartRateDataPointMapper.java)                                |
| jawbone               | [physical_activity](https://jawbone.com/up/developer/endpoints/workouts)                                         | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [JawbonePhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawbonePhysicalActivityDataPointMapper.java)                  |
| jawbone               | [sleep_duration](https://jawbone.com/up/developer/endpoints/sleeps)                                              | [omh:sleep-duration:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration)        | [JawboneSleepDurationDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawboneSleepDurationDataPointMapper.java)                        |
| jawbone               | [step_count](https://jawbone.com/up/developer/endpoints)                                                         | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [JawboneStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/jawbone/mapper/JawboneStepCountDataPointMapper.java)                                |
| misfit                | [physical_activity](https://build.misfit.com/docs/cloudapi/api_references#session)                               | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [MisfitPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/misfit/mapper/MisfitPhysicalActivityDataPointMapper.java)                     |
| misfit                | [step_count](https://build.misfit.com/docs/cloudapi/api_references#steps)                                        | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [MisfitStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/misfit/mapper/MisfitStepCountDataPointMapper.java)                                   |
| misfit                | [sleep_duration](https://build.misfit.com/docs/cloudapi/api_references#sleep)                                    | [omh:sleep-duration:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration)        | [MisfitSleepDurationDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/misfit/mapper/MisfitSleepDurationDataPointMapper.java)                           |
| moves<sup>4</sup>     | [physical_activity](https://dev.moves-app.com/docs/api_activities)                                               | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [MovesPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/moves/mapper/MovesPhysicalActivityDataPointMapper.java)                        |
| moves<sup>4</sup>     | [step_count](https://dev.moves-app.com/docs/api_storyline)                                                       | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [MovesStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/moves/mapper/MovesStepCountDataPointMapper.java)                                      |
| runkeeper             | [calories_burned](http://runkeeper.com/developer/healthgraph/fitness-activities#past)                            | [omh:calories-burned:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned)      | [RunkeeperCaloriesBurnedDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/runkeeper/mapper/RunkeeperCaloriesBurnedDataPointMapper.java)                |
| runkeeper             | [physical_activity](http://runkeeper.com/developer/healthgraph/fitness-activities#past)                          | [omh:physical-activity:1.2](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity)  | [RunkeeperPhysicalActivityDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/runkeeper/mapper/RunkeeperPhysicalActivityDataPointMapper.java)            |
| withings              | [blood_pressure](https://oauth.withings.com/api/doc#api-Measure-get_measure)                                     | [omh:blood-pressure:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-pressure)        | [WithingsBloodPressureDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsBloodPressureDataPointMapper.java)                     |
| withings              | [body_height](https://oauth.withings.com/api/doc#api-Measure-get_measure)                                        | [omh:body-height:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-height)              | [WithingsBodyHeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsBodyHeightDataPointMapper.java)                           |
| withings              | [body_weight](https://oauth.withings.com/api/doc#api-Measure-get_measure)                                        | [omh:body-weight:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)              | [WithingsBodyWeightDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsBodyWeightDataPointMapper.java)                           |
| withings              | [calories_burned](http://oauth.withings.com/api/doc#api-Measure-get_activity)<sup>5</sup>                        | [omh:calories-burned:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned)      | [WithingsDailyCaloriesBurnedDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsDailyCaloriesBurnedDataPointMapper.java)         |
| withings              | [calories_burned](http://oauth.withings.com/api/doc#api-Measure-get_intraday_measure) (intraday)<sup>5</sup>     | [omh:calories-burned:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned)      | [WithingsIntradayCaloriesBurnedDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsIntradayCaloriesBurnedDataPointMapper.java)   |
| withings              | [heart_rate](http://oauth.withings.com/api/doc#api-Measure-get_measure)                                          | [omh:heart-rate:1.1](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)                | [WithingsHeartRateDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsHeartRateDataPointMapper.java)                             |
| withings              | [sleep_duration](http://oauth.withings.com/api/doc#api-Measure-get_sleep)<sup>6</sup>                            | [omh:sleep-duration:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration)        | [WithingsSleepDurationDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsSleepDurationDataPointMapper.java)                     |  
| withings              | [step_count](http://oauth.withings.com/api/doc#api-Measure-get_activity)<sup>5</sup>                             | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [WithingsDailyStepCountDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsDailyStepCountDataPointMapper.java)                   |
| withings              | [step_count](http://oauth.withings.com/api/doc#api-Measure-get_intraday_measure) (intraday)<sup>5</sup>          | [omh:step-count:1.0](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)                | [WithingsIntradayStepCountBurnedDataPointMapper](https://github.com/openmhealth/shimmer/blob/master/shim-server/src/main/java/org/openmhealth/shim/withings/mapper/WithingsIntradayStepCountBurnedDataPointMapper.java) |
                                                                                                                                                                                                                                                                               
<sup>1</sup> *The Fitbit API doesn't provide time zone information for the data points it returns. Furthermore, it is not possible to infer the time zone from any of the information provided. Because Open mHealth schemas require timestamps to have a time zone, we need to assign a time zone to timestamps. We set the time zone of all timestamps to UTC for consistency, even if the data may not have occurred in that time zone. This means that unless the event actually occurred in UTC, the timestamps will contain an incorrect time zone. Please consider this when working with data normalized into OmH schemas that are retrieved from the Fitbit shim. We will fix this as soon as Fitbit makes changes to their API to provide time zone information.* 

<sup>2</sup> *The configuration file controls whether to serve Fitbit intraday or summary data and at what granularity (see `application.yaml` or `resource-server.env` for details). Intraday activity requests are limited to 24 hours worth of data per request. Fitbit must enable intraday access explicitly for your application (click the endpoint link for details). Attempting to generate normalized data with the intraday access property set to true, but when your API credentials have not been granted intraday access, will result in an error.*

<sup>3</sup> *The heart rate mapper has not been tested on real data from Jawbone devices. They have been tested on example data provided in Jawbone API documentation. Please help us out by testing Shimmer with real-world data of one of these types from a Jawbone device and letting us know whether or not it works correctly.*

<sup>4</sup> *Moves time zone handling needs to be tested further, as it's not clear if the time zone assumptions in the mappers are correct.*

<sup>5</sup> *The Withings configuration controls whether to serve intraday or summary data (see `application.yaml` or `resource-server.env` for details). Intraday activity requests are limited to 24 hours worth of data per request.*

<sup>6</sup> *Sleep data has not been tested using real data directly from a device. It has been tested with example data provided in the Withings API documentation. Please help us out by testing real-world Withings sleep data with Shimmer and letting us know whether or not it works correctly.*

### Contributing

The list of supported third-party APIs will grow over time as more shims are added. If you'd like to contribute a shim to work with your API or a third-party API, or contribute any other code,

1. [Open an issue](https://github.com/openmhealth/shimmer/issues) to let us know what you're going to work on.
  1. This lets us give you feedback early and lets us put you in touch with people who can help.
2. Fork this repository.
3. Create your feature branch from the `develop` branch.
4. Commit and push your changes to your fork.
5. Create a pull request.
