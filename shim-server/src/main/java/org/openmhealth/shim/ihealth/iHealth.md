### This is currently a work in progress

# api
api url: https://api.ihealthlabs.com:8443/openapiv2/user/{userid}

version: v2

api reference: http://developer.ihealthlabs.com/dev_documentation_openapidoc.htm

## authentication

### OAuth 2.0
- protocol: OAuth 2.0 
- reference: https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/
- flows: authorization code
  - authorization URL: https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/
  - access token:  https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/
- supports refresh tokens: yes
  - refresh token: https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/
    - response_type=refresh_token
- redirect_uri: uri must match the uri specified as a redirect_uri for the API developer account of the client key/secret being used
- scope: iHealth has its own name for the scope parameter in their authorization process: APIName. They do not refer to as scope in their API, but nonetheless it works the same as scope. During the authorization request you use the query parameter, “APIName” with values of one or more of the following, separated by spaces:
  - OpenApiSleep
  - OpenApiBP
  - OpenApiWeight
  - OpenApiActivity
  - OpenApiBG 
  - OpenApiSpO2
  - OpenApiFood
  - OpenApiSport
- access token: Authorization: Bearer token
  - token lifetime: 3672000 seconds

# pagination
- supported: yes, responses will have a “NextPageUrl” property if there is more data to be retrieved that is the URI for the endpoint uri for the next page. The next page url is endcoded such that it is in what appears to be an unusable in its current form, so it would need to be decoded in some way. However, there is a “page_index” parameter that can be incremented to step through pages of size 50 and reach all of the data. page_index = 1, 2, 3, 4, etc. 

- The limit is set at 50 and there is no ability to control page size or change the limit of the number of responses.

# rate limit 
- 5000 requests per hour per user

# query updated by date
- unsupported

# query created by date
- unsupported

# time zone and time representation
- Each datapoint contains a “timezone” property, which is a utc-offset in the form of a string (“-0800” or "0800") or integer (-8 or 8)
- Timestamps are represented as unix epoch seconds offset by the zone offset found in the “timezone” property, so the timestamps are, in essence, in local time
- Requests take unix epoch seconds and match on local time

# endpoints

## endpoint query parameters
- Required parameters: 
  - client_id: The ID for the client request 
  - client_secret: The key for the request 
  - access_token: The token for access
  - sc: The serial number for the client application
  - sv: A unique identifier for the client’s use of the specific endpoint (one for each endpoint per project) 

- Optional parameters
  - start_time:  the unix epoch second to start the search for datapoints to return, when it is empty, the data would start from one year ago.   
  - locale: Default (example in Appendix) Set the locale / units returned 
  - end_time: the Unix epoch second to constrain the end of the search when the Activity data ends, it must be later than start_time

## get weight
- Endpoint: /weight.json/
- Reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofWeight.htm

### description
The endpoint returns body weight-related measurements that include weight, BMI, bone weight, and others. 

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “WeightDataList,” which contains the data. Each item in the list contains a set of unique properties describing the weight-related measurements taken/entered during that session and when that session occurred:

- WeightValue
- MDate: measurement date
- TimeZone: time zone that the measurement occurred within
- BMI
- Note
- DataSource: ( Manual | FromDevice )
- DataID

It appears that the value is zero when it is missing, so zero values are not actually zero values, they are non values.

All data in the response are rendered using the same unit, though it can change per response. The unit is contained in a property “WeightUnit” and is an integer between 0 - 2 corresponding to the following enum: {kg : 0}, {lbs : 1}, {stone : 2}.

### measures 
body-weight: mapped
body-mass-index: mapped

## get blood pressure
- endpoint: /bp.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBloodPressure.htm

### description
Retrieves blood pressure measurements that are created and stored in iHealth.

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “BPDataList,” which contains the data. Each item in the list contains a set of properties describing the blood-pressure measurements:

- BPL: some sort of WHO blood pressure level/rating
- DataID
- DataSource:( Manual | FromDevice)
- HP: systolic blood pressure
- HR: heart rate
- IsArr: whether the user has an arrythemia
- LP: diastolic blood pressure
- MDate: measurement date            
- Note 
- TimeZone: utc offset for the zone where the measurement occurred

All data in the response are rendered using the same unit, though it can change per response. The unit is contained in a property “BPUnit” and is an integer between 0 - 1 corresponding to the following enum: {mmHg : 0}, {KPa : 1}

### measures mapped
omh:blood-pressure
omh:heart-rate

## get blood glucose
- endpoint: /glucose.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBG.htm

### description 
Retrieves a list of blood glucose measurements from the iHealth API. 

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “BGDataList,” which contains the data. Each item in the list contains a set of properties describing the blood glucose measurement:

- BG: glucose value
- DataID: the unique identity
- DinnerSituation - ( Before_Breakfast | After_breakfast | Before_lunch | After_lunch | 
- Before_dinner | After_dinner | At_midnight ) 
- DrugSituation - ( Before_taking_pills | After_taking_pills ) 
- MDate - measurement date time
- Note: the note of this data
- DataSource: ( Manual | FromDevice )
- TimeZone: Time zone of measurement location

All data in the response are rendered using the same unit, though it can change per response. The unit is contained in a property “BGUnit” and is an integer between 0 - 1 corresponding to the following enum: {mg/dl : 0}, {mmol/l : 1}

### measures
- blood-glucose: mapped

## get oxygen saturation
- endpoint: /spo2.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBloodOxygen.htm

### description
Retrieves blood oxygen saturation and heart rate information stored in the iHealth API. 

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “BODataList,” which contains the data. Each item in the list contains a set of properties describing the blood glucose measurement:

- BO: blood oxygen % saturation
- DataID: the unique identity
- HR: Heart rate
- MDate - measurement date time
- Note: the note of this data
- DataSource: ( Manual | FromDevice )
- TimeZone: Time zone of measurement location

### measures
heart-rate: mapped
oxygen-saturation: not mapped, schema and schema-sdk support is in development 

## get sports activities
- endpoint: /sport.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSport.htm

### description
Retrieves the physical activities an individual has engaged in from the iHealth API

### request
A note on the sport activity request: the start and end 

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “SPORTDataList,” which contains the data. Each item in the list contains a set of properties describing a unique physical activity:

- SportName: the name of the activity
- SportStartTime: start time of the activity in unix epoch seconds
- SportEndTime: end time of the activity in unix epoch seconds
- TimeZone: Time zone of measurement location
- DataID: the unique identity
- Calories: The total calories consumed
- LastChangeTime: Time of last change (UTC)
- DataSource: ( Manual | FromDevice )

### measures
- physical-activity: mapped

## get activity
- endpoint: /activity.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofActivityReport.htm

### description
Retrieves daily summaries of activity information (steps, calories, etc) that comes from an iHealth activity tracker device. 

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “ARDataList,” which contains the data. Each item in the list contains a set of properties describing a daily summary of activities for a given day:

Calories: The total number of calories burned including BMR and activity calories
Steps: The total number of steps counted by the device
MDate: the datetime that this entry was last updated with new information or the end of the day on the date that this entry represents (if the day has already completed)
TimeZone: Time zone of measurement location
DataID: the unique identity
LastChangeTime: Time of last change (UTC)
DataSource: ( Manual | FromDevice )

### measures 
- step-count: mapped
- calories-burned: not mapped because activity calories are combined with BMR

## get sleep
- endpoint: /sleep.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSleepReport.htm

### description
Returns a list of sleep activities for the user, including sleep summary information.

### response
The response contains meta information in the body, such as page length, record count, etc, as well as an array property “SRDataList,” which contains the data. Each item in the list contains a set of properties describing the sleep event:

“Awaken” - Number of times awoken 
“Fallsleep” - Time until asleep 
“HoursSlept” - The length of the sleep in minutes
“SleepEfficiency” - Sleep efficiency - unspecified
“StartTime” - Start time of sleep (in unix epoch probably)
“EndTime” - End time of sleep (in unix epoch probably)
DataSource: ( Manual | FromDevice )
“TimeZone” - Time zone of measurement location

### measures 
sleep-duration: mapped

## future endpoint support
We hope to support oxygen-saturation from the spo2 endpoint in iHealth, however we are finalizing a schema and schema-sdk support to represent that data. 
