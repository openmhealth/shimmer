# Shimmer [![Build Status](https://travis-ci.org/openmhealth/shimmer.svg?branch=master)](https://travis-ci.org/openmhealth/shimmer)

Shimmer is an application that makes it easy to pull health data from popular third-party APIs like Runkeeper and Fitbit.
It converts that data into an [Open mHealth](http://www.openmhealth.org) compliant format, letting your application work with clean and clinically meaningful data.   

We currently support the following APIs

* [Fitbit](https://www.fitbit.com/)
* [Google Fit](https://developers.google.com/fit/?hl=en)
* [Jawbone UP](https://jawbone.com/up)
* [Misfit](http://misfit.com/)
* [RunKeeper](https://runkeeper.com/index)
* [Withings](http://www.withings.com/)
* [iHealth](http://www.ihealthlabs.com/)

And the following APIs are in the works

* [Moves](https://www.moves-app.com/)
* [Strava](https://www.strava.com/)
* [FatSecret](https://www.fatsecret.com/)
* [Ginsberg](https://www.ginsberg.io/)

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
A *shim* is a library that can communicate with a specific third-party API, e.g. Withings. It handles the process of authenticating with the API, requesting data from it, and mapping that data into an Open mHealth compliant data format. 

A shim generates *data points*, which are self-contained pieces of data that not only contain the health data of interest, but also include header information such as date of creation, acquisition provenance, and data source. This metadata helps describe the data and where it came from. The library is called a shim because such clean and clinically significant data in not provided natively by the third-party API.

### Resource server
The *resource server* exposes an API to retrieve data points. The server handles API requests by delegating them to the correct shim. As more and more shims are developed and added to the resource server, it becomes capable of providing data points from more and more third-party APIs. The resource server also manages third-party access tokens on behalf of shims.

### Console
The *console* provides a simple web interface that helps users interact with the resource server. It can set configuration parameters, trigger authentication flows, and request data using date pickers and drop downs.

## Installation

There are two ways to install Shimmer. 

1. You can download and run pre-built Docker images. 
1. You can build all the code from source and run it natively or in Docker.

### Option 1. Download and run Docker images

If you don't have Docker, Docker Compose, and Docker Machine installed, download [Docker Toolbox](https://www.docker.com/toolbox) and follow the installation instructions for your platform. If you don't have a running Docker machine, follow these instructions to [deploy one locally](https://docs.docker.com/machine/get-started/), or these instructions to [deploy to the cloud](https://docs.docker.com/machine/get-started-cloud/) on any of these [cloud platforms](https://docs.docker.com/machine/drivers/). 

Once you have a running Docker host, in a terminal 

1. Clone this Git repository.
1. Run `docker-machine ls` to find the name and IP address of your active Docker host.
1. Run `eval "$(docker-machine env host)"` to prepare environment variables, *replacing `host` with the name of your Docker host*.
1. Run the `./update-compose-files.sh` script.
  * This step should be removed once Compose 1.5 is released.
1. Download and start the containers by running
  * `docker-compose up -d`
  * If you want to see logs and keep the containers in the foreground, omit the `-d`.
  * This will download up to 1 GB of Docker images if you don't already have them, the bulk of which are MongoDB, nginx and OpenJDK base images. 
  * It can take up to a minute for the containers to start up. You can check their progress using `docker-compose logs` if you started with `-d`.
1. Visit `http://<your-docker-host-ip>:8083` in a browser.

### Option 2. Build the code and run it natively or in Docker

If you prefer to build the code yourself,  

1. You must have a Java 8 or higher JDK installed. You can use either [OpenJDK](http://openjdk.java.net/install/) or the [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. You technically don't need to run the console, but it makes your life easier. If you're building the console,
  1. You need [Node.js](http://nodejs.org/download/).
  1. You need [Xcode Command Line Tools](https://developer.apple.com/xcode/) if you're on a Mac.
1. To run the code natively,
  1. You need a running [MongoDB](http://docs.mongodb.org/manual/) instance.
1. To run the code in Docker,
  1. You need Docker, Docker Compose, and Docker Machine, available in [Docker Toolbox](https://www.docker.com/toolbox).
  1. You need a running Docker host. If you don't have a running Docker host, follow these instructions to [deploy one locally](https://docs.docker.com/machine/get-started/), or these instructions to [deploy to the cloud](https://docs.docker.com/machine/get-started-cloud/) on any of these [cloud platforms](https://docs.docker.com/machine/drivers/). 

If you want to build and run the code natively, in a terminal
 
1. Clone this Git repository.
1. Run the `./run-natively.sh` script and follow the instructions.
1. When the script blocks with the message `Started Application`, the components are running.
  * Press Ctrl-C to stop them.
  * The script creates a WAR file which you can alternatively drop into an application server. [This issue](https://github.com/openmhealth/shimmer/issues/31) has details.
1. Visit `http://localhost:8083` in a browser.

If you want to build and run the code in Docker, in a terminal 
 
1. Clone this Git repository.
1. Run `docker-machine ls` to find the name of your active Docker host.
1. Run `eval "$(docker-machine env host)"` to prepare environment variables, *replacing `host` with the name of your Docker host*.
1. Run the `./run-dockerized.sh` script and follow the instructions.
  * The containers should now be running on your Docker host and expose port 8083.
  * It can take up to a minute for the containers to start up.
1. Visit `http://<your-docker-host>:8083` in a browser.

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
[RunKeeper](http://runkeeper.com/partner)                         | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/runkeeper/callback
[Withings](http://oauth.withings.com/api)                         | ?                  | ?                        | ?                   | ?                | ?                 | http://localhost:8083/authorize/withings/callback

<sup>1</sup> *Fitbit has deprecated OAuth 1.0a authorization in favour of OAuth 2.0. You will need OAuth 2.0 credentials.*

<sup>2</sup> *You'll need to copy the iHealth SC and SV values found via the [application management page](http://developer.ihealthlabs.com/developermanagepage.htm)
into the `openmhealth.shim.ihealth.serialValues` map of the `application.yaml` file.*

<sup>3</sup> *The [documentation](https://jawbone.com/up/developer/authentication) states TLS is required, but authorization does work without it.*

> If any of the links or fields are incorrect or out of date, please [submit an issue](https://github.com/openmhealth/shimmer/issues) to let us know.
> This table will be fully populated in in the coming days.

Visit the links to register and configure your application for each of the APIs you want to use. Once credentials are
obtained for a particular API, navigate to the settings tab of the console and fill them in.

> If you didn't build the console, uncomment and replace the corresponding `clientId` and `clientSecret` placeholders in the `application.yaml` file
with your new credentials and rebuild.



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
 
1. Make a GET request to `http://<host>:8083/authorize/{shim}?username={userId}`
  * The `shim` path parameter should be one of the names listed [below](#supported-apis-and-endpoints), e.g. `fitbit`. 
  * The `username` query parameter can be set to any unique identifier you'd like to use to identify the user. 
1. Find the `authorizationUrl` value in the returned JSON response and redirect your user to this URL. Your user will land on the third-party website where they can login and authorize access to their third-party user account. 
1. Once authorized, they will be redirected to `http://<host>:8083/authorize/{shim_name}/callback`. 

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
 
`http://<host>:8083/data/{shim}/{endPoint}?username={userId}&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize={true|false}`

The URL can be broken down as follows
* The `shim` and `username` path variables are the same as [above](#authorizing-access-to-a-third-party-user-account).
* The `endPoint` path variable corresponds to the type of data to retrieve. There's a table of these [below](#supported-apis-and-endpoints).
* The `normalize` parameter controls whether the shim returns data in a raw third-party API format (`false`) or in an Open mHealth compliant format (`true`).  

> N.B. This API will be changing significantly in the near future to provide greater consistency across Open mHealth applications and to improve expressivity and ease of use. The data points it returns will not be affected, only the URLs used to request data and perhaps some book-keeping information at the top level of the response.
 
## Supported APIs and endpoints

The following is a table of the currently supported shims, their endpoints, and the Open mHealth compliant data that each endpoint can produce. The values in the `shim` and `endPoint` columns are the values for the parameters of the same names used in [programmatic access](#reading-data-programmatically) of the API.

The currently supported shims are:

| shim         | endPoint          | OmH data produced by endpoint |
| ------------ | ----------------- | -------------------------- |
| fitbit<sup>1</sup> | activity    | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| fitbit<sup>1</sup> | steps<sup>2</sup>             | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count) |
| fitbit<sup>1</sup> | weight            | [omh:body-weight](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight) |
| fitbit<sup>1</sup> | body_mass_index   | [omh:body-mass-index](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index)|
| fitbit<sup>1</sup> | sleep             | [omh:sleep-duration](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration) |
| googlefit    | activity          | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| googlefit    | body_height       | [omh:body-height](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-height) |
| googlefit    | body_weight       | [omh:body-weight](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight) |
| googlefit    | heart_rate        | [omh:heart-rate](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate)
| googlefit    | step_count        | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)
| googlefit    | calories_burned   | [omh:calories-burned](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned) |
| jawbone      | activity          | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| jawbone      | weight            | [omh:body-weight](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight)
| jawbone      | body_mass_index   | [omh:body-mass-index](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index) |
| jawbone      | steps             | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count) |
| jawbone      | sleep             | [omh:sleep-duration](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration) |
| jawbone      | heart_rate<sup>3</sup>        | [omh:heart-rate](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate) |
| misfit       | activities        | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| misfit       | steps             | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count)
| misfit       | sleep             | [omh:sleep-duration](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration) |
| runkeeper    | activity          | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| runkeeper    | calories          | [omh:calories-burned](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned) |
| withings     | blood_pressure    | [omh:blood-pressure](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-pressure)|
| withings     | body_height       | [omh:body-height](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-height)|
| withings     | body_weight       | [omh:body-weight](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight) |
| withings     | heart_rate        | [omh:heart-rate](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate) |
| withings     | steps<sup>4</sup> | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count) |
| withings     | calories<sup>4</sup> | [omh:calories-burned](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_calories-burned) |
| withings     | sleep<sup>5</sup> | [omh:sleep-duration](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration) |
| ihealth      | physical_activity | [omh:physical-activity](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_physical-activity) |
| ihealth      | blood_glucose     | [omh:blood-glucose](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-glucose) |
| ihealth      | blood_pressure    | [omh:blood-pressure](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_blood-pressure) |
| ihealth      | body_weight       | [omh:body-weight](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-weight) |
| ihealth      | body_mass_index   | [omh:body-mass-index](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_body-mass-index) |
| ihealth      | heart_rate        | [omh:heart-rate](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_heart-rate) |
| ihealth      | step_count        | [omh:step-count](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_step-count) |
| ihealth      | sleep_duration    | [omh:sleep-duration](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_sleep-duration) |

<sup>1</sup> *The Fitbit API does not provide time zone information for the data points it returns. Furthermore, it is not possible to infer the time zone from any of the information provided. Because Open mHealth schemas require timestamps to have a time zone, we need to assign a time zone to timestamps. We set the time zone of all timestamps to UTC for consistency, even if the data may not have occurred in that time zone. This means that unless the event actually occurred in UTC, the timestamps will be incorrect. Please consider this when working with data normalized into OmH schemas that are retrieved from the Fitbit shim. We will fix this as soon as Fitbit makes changes to their API to provide time zone information.* 

<sup>2</sup> *Uses the daily step summary when partner access is disabled (default) and uses intraday step count (at 1 minute granularity) when partner access is enabled. Intraday activity requests are limited to 24 hours worth of data per request. See the YAML configuration file (application.yaml) to enable partner access if your API credentials have been granted partner access. Attempting to generate normalized data with the partner access property set to true, but when your API credentials have not been granted partner access will result in an error.*

<sup>3</sup> *The Heart rate mapper has not been tested on real data from Jawbone devices. They have been tested on example data provided in Jawbone API documentation. Please help us out by testing Shimmer with real-world data of one of these types from a Jawbone device and letting us know whether or not it works correctly.*

<sup>4</sup> *Uses the daily activity summary when partner access is disabled (default) and uses intraday activity when partner access is enabled. See the YAML configuration file for details. Intraday activity requests are limited to 24 hours worth of data per request.*

<sup>5</sup> *Sleep data has not been tested using real data directly from a device. It has been tested with example data provided in the Withings API documentation. Please help us out by testing real-world Withings sleep data with Shimmer and letting us know whether or not it works correctly.*

### Contributing

The list of supported third-party APIs will grow over time as more shims are added. If you'd like to contribute a shim to work with your API or a third-party API, or contribute any other code,

1. [Open an issue](https://github.com/openmhealth/shimmer/issues) to let us know what you're going to work on.
  1. This lets us give you feedback early and lets us put you in touch with people who can help.
2. Fork this repository.
3. Create your feature branch from the `develop` branch.
4. Commit and push your changes to your fork.
5. Create a pull request.
