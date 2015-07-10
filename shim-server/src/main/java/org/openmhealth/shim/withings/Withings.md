### still work in progress

# api
api url: https://wbsapi.withings.net/

# getting started on Withings

1. register as a developer at http://oauth.withings.com/api 
2. Store your consumer key and secret somewhere safe

## authentication

### OAuth 1.0a
- protocol: OAuth 1.0 for production environments
- reference: http://oauth.withings.com/api/doc
- authorization URL: https://oauth.withings.com/account/authorize
- request token: https://oauth.withings.com/account/request_token
- access token:  https://oauth.withings.com/account/access_token
- scope: Read only, can read all data, provided user allows
- supports refresh tokens: no, user access tokens do not expire
- signature placement: Query signing
- signature method: HMAC-SHA1
- access token: user access token, oauth_token=<resource_owner_key>&oauth_token_secret=<resource_owner_secret>

# pagination
- supported: Not explicitly specified

# rate limit

- 60 requests per minute, 120 requests per minute for GetIntradayActivity
- limit header: none
- remaining header: none
- next reset time header: none

# data request limit

- GetActivity and GetSleepSummary - up to 200 days of data per request
- GetIntradyActivity - up to 24 hours of data per request
- GetSleep - up to 7 days of data per request

# incremental data

- supported: Most requests support incremental data retrieval based on the “lastmodified” or “updatetime” properties 

# time zone and time representation
Depends on endpoint, most endpoints return time values in unix epoch seconds which are aligned to UTC
- The body measures endpoint uses unix epoch seconds timestamps aligned to UTC for each individual datapoint, however the entire request will return a timezone, which is the user’s profile timezone and this will change as the user changes this and it will be attributed to all of the datapoints returned in the request, regardless of whether those datapoints were generated in that time zone or not.
- Intraday activity uses unix epoch seconds timestamps aligned to UTC as the start and end date for each data point.
- Sleep summary data uses dates that are in the format YYYY-mm-dd to indicate the date, in the user’s time zone, that the sleep session ended. In addition each session has a startdate and enddate property that are aligned to UTC for each individual datapoint
- Activity measures contain a date property in the format YYYY-mm-dd to indicate that date during which all of the activity data has occurred. Withings creates an activity measures datapoint at the end of the day, when the user reaches midnight in whatever time zone they currently occupy. The datapoint is then stamped with that timezone, wherever they end up at the end of the day.

# endpoints

## get activity measures
- Endpoints: /v2/measure?action=getactivity
- Reference: http://oauth.withings.com/api/doc#api-Measure-get_activity

Retrieves a summary of activity information for a user for a given date or range of dates (up to 200 days). The summary includes information about the step count, active calories burned, seconds of soft, moderate, and vigorous activity,

### Parameters
- Required parameters: userid, oauth authentication information
- Optional parameters: date, startdateymd, enddateymd (all in the form YYYY-mm-dd), without date the response behavior is unknown

### Response
The response itself contains two properties, “status” and “body”. The “body” property contains the actual content relevant to the request. A request for a single date will be a single object within the “body” property, while a request for multiple days will have an array, “activities”, with a series of JSON objects representing each day.

There is a time zone returned with each data point in this response and it is the timezone that the user was in when the data point was generated, which is the timezone they are in at midnight at the end of that day.

## get intraday activity
- Endpoint: /v2/measure?action=getintradayactivity
- Reference: http://oauth.withings.com/api/doc#api-Measure-get_intraday_measure

Retrieves activity information for active time periods within a single time hour time period. The response contains information includes calories burned, elevation traveled, steps taken, distance traveled, and duration of activity.

### Parameters
- Required parameters: userid, startdate, enddate (these dates are in the form of unix epoch seconds time stamps, indicating a specific start and end time of interest, within a 24 hour time period)

### Response
The endpoint returns two properties, “status” and “body.” Body contains one property, “series,” which is a dictionary of activity periods where the key is a unix epoch seconds value for the start time of the activity period and the value is an object describing the activity period by the calories, elevation, steps, distance, and duration.

Each data point is a defined time period within the day with a unix epoch seconds timestamp value as the start of the activity along with a duration in seconds. Only time periods when activity data exists are returned, so time periods not captured within this response are implicitly defined to be 0 activity periods. It appears that the maximum granularity is at 15 minutes, so data points within the response are separated by 15 minutes or less. A 30 minute activity period would be represented by two 15 minute activity periods and a 25 minute activity period would be represented by a 15 minute activity period followed by a 10 minute activity period. There is no timezone information associated with responses from this endpoint.

## get body measures
- endpoint: /measure?action=getmeas
- reference: http://oauth.withings.com/api/doc#api-Measure-get_measure

Retrieves different body measurements for the user, such as weight, height, and blood pressure.

### Parameters
- required parameters: userid
- optional parameters: meastype, category, limit, offset, startdate, enddate, lastupdated - it is intended to use either lastupdated or startdate/enddate (the behavior for the response to a request without them in unspecified date time frame or lastupdated)

### Response
The endpoint returns a JSON document with two properties, “status” and “body.” The “body” property contains three properties - “timezone”, “updatetime”, and “measuregrps.” Measuregrps is an array of data points, each capturing a series of measure that were taken at the same time. Each data point in the array has a date field that is the unix epoch seconds value at which the data point was captured. Within each data point, there is an array, “measures,” that contains a list of measures that were taken at that time point. Each measure has a type, which corresponds to the meastype enumeration in Withings (indicating blood pressure vs weight vs height, etc), a value, which is the actual numerical value of the measure (appears to only be integers), and unit, which is not actually a unit value, but instead a decimal shift value indicating the power of ten the "value" parameter should be multiplied to to get the real value.

## get sleep measures
- endpoint: /v2/sleep?action=get
- reference: http://oauth.withings.com/api/doc#api-Measure-get_sleep

Retrieves information regarding the time spent in different stages of sleep during sleep periods.

### Parameters
- required parameters: userid, startdate, enddate (startdate and enddate are timestamps based on unix epoch in seconds)
- optional parameters: none

### Response
The endpoint returns a JSON document with two properties, “status” and “body.” The “body” property contains a field, “model,” that is an integer that refers to the specific device model used to capture the data point. The body also contains an array, “series,” which contains objects representing different sleep phases during the sleep period specified in the request. Each object has a “startdate” and “enddate” field, which are in unix epoch seconds, and then a “state” field that represents the sleep phase the individual was in during that time (0: awake, 1: light sleep, 2: deep sleep, 3: REM sleep (only if model is Aura)). These data points do not have any time zone data associated with them.

## get sleep summary
- endpoint: /v2/sleep?action=getsummary
- reference: http://oauth.withings.com/api/doc#api-Measure-get_sleep_summary

Retrieves the summary of the sleep for each night within the specified date range. This summary includes the seconds of light sleep, deep sleep, rem sleep, the time to sleep, and other aspects of the sleep experience.

### Parameters
- required parameters: either startdateymd/enddateymd or lastmodified
- optional parameters:  none

### Response
The endpoint returns a JSON document with two properties, “status” and “body.” The “body” property contains a field “more,” that is undescribed and an array “series,” which contains a list of sleep summaries that correspond to individual days within the requested time period. Each item in the list has a date (in YYYY-mm-dd form), startdate and enddate fields (in unix epoch seconds), a timezone, an identifier (no description of the id), the durations of different types of sleep (in seconds), and the counts of sleep variables (such as the number of wakeups).
