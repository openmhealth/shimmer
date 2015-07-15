# api
api url: https://www.googleapis.com/fitness/v1/

api reference: https://developers.google.com/fit/rest/v1/reference/

## authentication

### OAuth 2.0
- protocol: OAuth 2.0 for production environments
- reference: https://developers.google.com/fit/rest/v1/authorization
- authorization URL: https://accounts.google.com/o/oauth2/auth
- access token:  https://www.googleapis.com/oauth2/v3/token
- refresh token: https://www.googleapis.com/oauth2/v3/token
- scope: Scope is read and write across the different google fit domains depending on requested scope, you can request multiple scopes at once
  - https://www.googleapis.com/auth/fitness.activity.read
  - https://www.googleapis.com/auth/fitness.activity.write
  - https://www.googleapis.com/auth/fitness.body.read
  - https://www.googleapis.com/auth/fitness.body.write
  - https://www.googleapis.com/auth/fitness.location.write
  - https://www.googleapis.com/auth/fitness.location.read
- supports refresh tokens: yes, user access tokens expire, has a field “expires_in” in the response to the access token request
- signature placement: header signing
- access token: user access token

# pagination
- supported: yes, responses will have a “nextPageToken” property if there is more data to be retrieved, the value of this property can be used in the header of the next response as the pageToken parameter. Requests can also have an optional “limit” parameter, however this is currently broken and will not set the nextPageToken property in the response. The whole pagination process may be currently broken. 

# rate limit
- Free quota:
  - 86,400 total requests/day 
  - Per-user limit: 5 requests/second/user
- limit header: information not in header
- remaining header: information not in header
- next reset time header: information not in header


# data request limit
- unspecified

# incremental data
datapoints have a “modifiedTimeMillis” property that lists the time they were last altered, so that datapoints can be updated if they change

# time zone and time representation
- Most data points return datetime as unix epoch nanoseconds that are aligned to UTC

# endpoints
In addition to the parameters listed within each specific endpoint, all endpoints accept, and in some cases require, the following request parameters: https://developers.google.com/fit/rest/v1/reference/parameters.

## get body weight
- Endpoint: /users/me/dataSources/derived:com.google.weight:com.google.android.gms:merge_weight/datasets/<time-frame-start-in-nanos>-<time-frame-end-in-nanos> this endpoint in theory provides all of the datapoints that have been measured by a device related to body weight. Google is not clear and does not provide documentation on their merge process. 

  - Alternative: /users/me/dataSources/raw:com.google.weight:com.google.android.apps.fitness:user_input/datasets/<time-frame-start-in-nanos>-<time-frame-end-in-nanos> this endpoint provides only body weight values that were input by the user

- Reference: https://developers.google.com/fit/rest/v1/data-types

### Description
This description relates to the primary endpoint above, for merged data points. This endpoint returns all of the body weight data points that were synced to the Google Fit platform from devices that are connected to Google Fit. The Body weight values are returned as floats in kilogram units. Each datapoint has a start datetime (startTimeNanos) and end datetime (endTimeNanos) and although they are likely the same, we will need to check that before creating the data point. The nanos values are unix epoch nanoseconds that are aligned to UTC. 
### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required, but is set to “me” in all cases to refer to the user whose authentication token is being used.

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination. 

“pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.

### Response
A list of data points contained in the array property named “point.” Each item in the list is a datapoint representing a body weight measurement reported by a device or recorded directly by the user. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.weight” indicating it is a weight value stored in the google fit platform
- originDataSourceId: the data source for the datapoint, representing as a namespace for the application along with some additional information. It could be a user input value, represented by the strting “raw:com.google.weight:com.google.android.apps.fitness:user_input” or from a third party device or third party application, such as “raw:com.google.weight:com.fatsecret.android:”
- value: an array that contains a single data point representing the value
  - fpVal: the weight value in kg, this property is contained within the object in the “value” array
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request

## get body height
- Endpoint: https://www.googleapis.com/fitness/v1/users/me/dataSources/derived:com.google.height:com.google.android.gms:merge_height/datasets/<time-frame-start-in-nanos>-<time-frame-end-in-nanos>

  - Alternative: /users/me/dataSources/raw:com.google.height:com.google.android.apps.fitness:user_input/datasets/<time-frame-start-in-nanos>-<time-frame-end-in-nanos>

- Reference: https://developers.google.com/fit/rest/v1/data-types

### Description
This description relates to the primary endpoint above, for merged data points. This endpoint returns all of the body height data points that were synced to the Google Fit platform from devices connected to Google Fit. The body height values are returned as floating point numbers with a unit of meters. Each datapoint has a start datetime (startTimeNanos) and end datetime (endTimeNanos) and although they are likely the same, we will need to check that before creating the data point. The nanos values are unix epoch nanoseconds that are aligned to UTC. 

### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required, but is set to “me” in all cases to refer to the user whose authentication token is being used. 

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination; “pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.

### Response
A list of data points contained in the array property named “point.” Each item in the list is a datapoint representing a body height measurement reported by a device or recorded directly by the user. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.height” indicating it is a height value stored in the google fit platform
- originDataSourceId: the data source for the datapoint, representing as a namespace for the application along with some additional information. It could be a user input value, represented by the string “raw:com.google.height:com.google.android.apps.fitness:user_input” or from a third party device or third party application.
- value: an array that contains a single data point representing the value
  - fpVal: the height value in meters, this property is contained within the object in the “value” array
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request

## get step count
- Endpoint: derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas

Merge appears to combine some of the step count periods into single datapoints and then cut off one or two steps. Actually, the estimated steps breaks out some of the merged segments and adds activity estimates and does other estimating to account for anomalous steps and biking/driving steps. We use the merge_step_deltas as the merged value from across all step counter sources which will be the raw data reported which is more accurate to what the devices report which will be less confusing.

### Description
Retrieves all of the step count data points that have been recorded to the Google Fit platform, across all devices and applications that are connected to Google Play. Step counts are merged using machine learning and other approaches so that there are not duplicate step counts during the same time period. 

### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required and is set to “me” in all cases to refer to the user whose authentication token is being used. 

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination; “pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.


### Response
Returns a list of data points contained in the array property named “point.” Each item in the list is a datapoint representing a step count measurement reported by a device or recorded directly by the user. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.step_count.delta” indicating it is a step count value stored in the google fit platform. The delta refers to the fact that it is the number of step counts during the specified period, since the last data point was created. 
- originDataSourceId: the data source for the datapoint, representing as a namespace for the application along with some additional information. It could be a user input value, represented by the string “raw:com.google.step_count.delta:com.google.android.apps.fitness:user_input” or from a third party device or third party application, “derived:com.google.step_count.cumulative:com.google.android.gms:samsung:Galaxy Nexus:32b1bd9e:soft_step_counter”. This means it is possible to ask for step counts provided from specific devices or applications. 
- value: an array that contains a single data point representing the value
  - intVal: the number of steps taken during the time period as a raw count, this property is contained within the object in the “value” array
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request

## get calories
- Endpoint: derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended

It appears that between these two, there is some merging of calorie counts during overlapping time periods. For example, from_activities just generates calorie estimates from actual activity entries, but ignores the calorie values that the user enters. If the user enters calories as part of entering that activity, than the user entered value overwrites the estimate from activity. 

Seems that the best approach would be to use merge_calories_expended and filter out the metabolic rate (BMR) calories, which are datapoints with a originDataSourceId of “derived:com.google.calories.expended:com.google.android.gms:from_bmr”.

### Description
Retrieves a set of ‘calories expended’ data points that is merged from all sources accessible on the Google Fit platform. The merging process is based on a set of heuristics, machine learning, and other algorithms and does not contain duplicate calories expended datapoints during any time period. The response also contains datapoints that contain ‘calories expended’ data based on basal metabolic resting rate in addition to user input data, inferred calories expended from activities, and sensed or measured data from devices. 

### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required and is set to “me” in all cases to refer to the user whose authentication token is being used. 

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination; “pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.


### Response
Returns a list of data points contained in the array property named “point.” Each item in the list is a datapoint representing a period of calories being expended by activity or body activity. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.calories.expended” indicating it represents a number of calories expended that is stored on the google fit platform
- originDataSourceId: the data source for the datapoint, representing as a namespace for the application along with some additional information. It could be a user input value, represented by the string “raw:com.google.calories.expended:com.google.android.apps.fitness:user_input” or from inference based on activity or other measures (derived:com.google.calories.expended:com.google.android.gms:from_activities)
- value: an array that contains a single data point representing the value
  - fpVal: the measured calories expended value associated with the datapoint in kcal represented as a float number
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request

## get heart rate
- Endpoint: derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm

- reference: https://developers.google.com/fit/rest/v1/data-types

### Description
Retrieves heart rate measurements that have been stored on the Google Fit platform through a third-party device or application. Currently there is no way to add heart rate measurements as a user. 

### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required and is set to “me” in all cases to refer to the user whose authentication token is being used. 

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination; “pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.

### Response
Returns a list of data points contained in the array property named “point.” Each item in the list is a datapoint representing a heart rate measurement reported by a device. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.heart_rate.bpm” indicating it is a heart rate measurement stored on the google fit platform
- originDataSourceId: the data source for the datapoint, represented as a namespace for the application along with some additional information. It identifies the third party device or third party application that created the datapoint, such as  “raw:com.google.heart_rate.bpm:si.modula.android.instantheartrate:”
- value: an array that contains a single data point representing the value
  - fpVal: the measured heart rate value associated with the datapoint in beats per minute and using a float type
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request

## get activities
- Endpoint: derived:com.google.activity.segment:com.google.android.gms:merge_activity_segments

- Reference: https://developers.google.com/fit/rest/v1/reference/activity-types

### Description
Retrieves information about continuous activities that were performed by the user during different time period, but not information about the outcomes of those sessions (calories burned, distance traveled, etc). These activity segments can be created through user input, application data, or inferred through sensors and other data. 

### Parameters
- Required parameters: time-frame-start-in-nanos, time-frame-end-in-nanos are required to specify the range of dates/times for which the data should be retrieved. Userid is required and is set to “me” in all cases to refer to the user whose authentication token is being used. 

- Optional parameters: “limit”, which is the maximum number of datapoints to be returned in the response and if the there are more data points in the dataset, nextPageToken will be set in the dataset response for pagination; “pageToken” which is the continuation token, which is used to page through large datasets. To get the next page of a dataset, set this parameter to the value of nextPageToken from the previous response.

### Response
Returns a list of data points contained in the array property named “point.” Each item in the list is a datapoint representing the performance of a physical activity reported by a device or recorded directly by the user. Each data point contains the following properties: 
- startTimeNanos: the start date/time timestamp in unix epoch nanos
- endTimeNanos: the end date/time timestamp in unix epoch nanos
- dataTypeName: indicates the type of data that is contained within the data point, in this case the value is “com.google.activity.segment” indicating it is an activity segment value stored in the google fit platform. 
- originDataSourceId: the data source for the datapoint, represented as a namespace for the application along with some additional information. It could be a user input value, represented by the string “raw:com.google.activity.segment:com.google.android.apps.fitness:user_input” or from a third party device or third party application, “derived:com.google.activity.sample:com.google.android.gms:samsung:Galaxy Nexus:32b1bd9e:detailed”. This means it is possible to ask for physical activity sessions recorded from specific devices or applications. 
- value: an array that contains a single data point representing the value
  - intVal: the identifier of the activity which maps into the list maintained by google (https://developers.google.com/fit/rest/v1/reference/activity-types)
- modifiedTimeMillis: Indicates the last time this data point was modified.

In addition to the datapoints array, there are three other properties in the response:
- minStartTimeNs: the start datetime of the request that generated the response
- maxEndTimeNs: the end datetime of the request that generated the response
- dataSourceId: the id of the data source that responded to the request
