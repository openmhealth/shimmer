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
     - May be a bit off of standard OAuth token uri here
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

- The limit is set at 50 and there is no ability to control page size or limit the number of responses.

# rate limit 
- 5000 requests per hour per user

# query updated by date
- unsupported

# query created by date
- unsupported

# time zone and time representation
- Each datapoint contains a “timezone” property, which is a utc-offset in the form “-0800”
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
  - start_time:  the unix epoch second to constrain the search, when it is empty, the data would start from one year ago 
  - page_index:  First page The page index of data, which starts from 1
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

### measures mapped
omh:body-weight
omh:body-mass-index

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

### measures mapped
omh:blood-glucose

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

### measures mapped
omh:heart-rate

## get sports activities
- endpoint: /sport.json/
- reference: http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSport.htm

### description
Retrieves the physical activities an individual has engaged in from the iHealth API

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

### measures mapped
omh:physical-activity

## future endpoint support
We hope to support step-count and sleep-duration measures from the activity and sleep endpoints in iHealth, however we are unable to ascertain the time frame on step data from the documentation and also need real device data to test uncertainties and ambiguities in these endpoints that are not clear from the documentation.
