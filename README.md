## Open mHealth Shim Server

### Overview

The shim server is a standalone application which can be used to read data from third-party data sources. The data can be read in the 'raw' format from the third-party or in an Open mHealth compliant format.

Each [Shim](http://www.openmhealth.org/) is a third-party adapter that runs within the shim server and reads/converts data. The shim server will only run as many shims as are configured (see below).    

The shims currently supported are:
* RunKeeper
* Fitbit
* Fat Secret
* Withings
* Microsoft HealthVault
* Jawbone UP

### Technical Requirements

1. You must have [Java 7](https://java.com/en/) or higher installed. 
2. A Running [Mongo DB](http://http://docs.mongodb.org/manual/) installation is required.
3. [Gradle](http://www.gradle.org/) or [Maven](http://maven.apache.org/) to build the source code.  

### Installation

1. Clone this git repo.
2. Navigate to the src/main/resources/ directory and edit the application.yaml file.
3. Ensure mongodb parameter points to your locally running mongo instance.
3. Obtain a clientId/clientSecret for each shim you'd like to run. These are obtained from the third-party developer websites.
4. Un-comment and replace the obtained clientId/clientSecret their respective section(s).
5. If using maven you can use the spring-boot goal from the root directory: mvn spring-boot:run
6. If using gradle you can use the command: gradle bootRun
7. The server should now be running on the default port 8083. You can change the port number in the application.yaml file as well.

### Authorizing A Data Source

In order to read data from a third-party we must initiate the oauth process and authorize data from a third-party account.

To initiate the OAuth process, do the following:
 
1. Go to the url http://localhost:8083/authorize/<shim_name>?username=<unique_identifier>. 
Unique identifier can be any unique string you'd like to use. 
2. In the JSON response, find the value 'authorizationUrl' and open this URL in a new browser window. 
You should be redirected to the third-party website where you can login and authorize access to your third party user account. 
3. Once authorized you should be redirected to http://localhost:8083/authorize/<shim_name>/callback and you'll see an approved JSON response.

### Reading Data
Now you can pull data from the third party's available end points by going to 
http://localhost:8083/data/<shim_name>/<end_point>?username=<unique_identifier>&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd

The available shims and end points are:
