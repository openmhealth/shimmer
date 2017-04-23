### still a rough draft

# general
docs main page: https://dev.fitbit.com/docs

# getting started on Fitbit

1. Create a Fitbit account to use for development at https://www.fitbit.com/login
1. Create a new app from https://dev.fitbit.com/apps/new
  1. take note of the "Client Id" and "App Secret"
1. If you would like to access intraday activity data, email api@fitbit.com to make the request

# api
- api url: https://api.fitbit.com
- version: 1

## authentication

### OAuth 1.0a
- protocol: OAuth 1.0a for production environments
- reference: https://dev.fitbit.com/docs/oauth1/
- authorization URL: https://www.fitbit.com/oauth/authorize
 - request token: https://www.api.fitbit.com/oauth/request_token
- access token: https://www.api.fitbit.com/oauth/access_token
- location: auth_header
- method: HMAC-SHA1

### OAuth 2.0
- protocol: OAuth 2.0
- status: Recently exited beta status, will be supported in a future shimmer release

# pagination
- supported: Mostly no, however newer api calls (currently in beta) are adding support, such as activities list

# rate limit
- public data and non-user authenticated data: 150 / hour 
- per user: 150 / hr
- limit header: Fitbit-Rate-Limit-Limit
- remaining header: Fitbit-Rate-Limit-Remaining
- next reset time header: Fitbit-Rate-Limit-Remaining (seconds until reset)

# time zone and time representation
- time zone information is not captured for dates and times; it is not possible to infer timezone through any means and therefore it is impossible to know the point in time that any activity or measurement occurred

# endpoints

## get user info
- Uri: https://api.fitbit.com/{api-version}/user/{user-id}/profile.json
- Reference: https://dev.fitbit.com/docs/user/

Returns information from the user’s profile in the form of a json object “user” with properties ranging from “city” and  “dateOfBirth” to “gender” and “weight”, and “timezone”. Unfortunately the timezone is not related to the data points from other endpoints, so it should not be used to set the timezone for timestamps in data points. 

## get activities
- Uri: https://api.fitbit.com/{api-version}/user/{user-id}/activities/date/{date}.json
- Reference: https://dev.fitbit.com/docs/activity/

### measures
- physical activity: mapped
- heart rate: mapped
- calories burned: not mapped, uncertainty around the different calorie values

### description
Returns all the activities for a user on a specified day and contains a list of activities, heart-rate, goals, and summary of the day. The request takes an optional request header of Accept-Language, which can be used to specify the measurement unit system for the values. By default, uses metric system. “activities” is a JSON array of activity JSON objects with details about each activity logged on that day. “goals” is a JSON object with activity-oriented goals - “caloriesOut”, “distance”, “floors”, “steps”. “summary” is a JSON object that summarizes key outcomes aggregated over all activities for that day  - “activityCalories”, “activityOut”, “elevation”, etc, and an array “distances,” which breaks down the distances of various activities completed that day.  

## get daily step count summary
- Endpoint: /activities/steps/date/{date-parameters}
- Reference: https://dev.fitbit.com/docs/activity/#get-activity-time-series

### date parameters:
1. {start-date}/{end-date}.json
1. {end-date}/{period}.json

### measures
- step count: mapped

### description
Retrieves a series of step count summaries for each of the dates within the specified date parameters.

## get intraday step count 
- Endpoint: https://api.fitbit.com/1/user/-/activities/steps/date/{date-parameters}
- Reference: https://dev.fitbit.com/docs/activity/#get-activity-intraday-time-series

### date parameters
1. {date}/1d/{detail-level}.json - used by shimmer currently
1. {date}/{date}/{detail-level}.json
1. {date}/{date}/{detail-level}/time/{start-time}/{end-time}.json
1. {date}/1d/{detail-level}/time/{start-time}/{end-time}.json
 
Detail-level represents the granularity of the intraday data to return. By default it uses 1min. It can be set explicitly to either 1min or 15min.

Start-time and end-time use the format HH:mm and are interpreted as local time when retrieving data from the API.

### measures
- step count: mapped

### description
Retrieves a series of step counts that occurred within a single day. It can be at the granularity of every 1 minute or every 15 minutes. Requires special approval from Fitbit that can be acquired by submitting a request to api@fitbit.com.

## get body weight
- Uri: https://api.fitbit.com/{api-version}/user/-/body/log/weight/date/{date-parameters}
- Reference: https://dev.Fitbit.com/docs/body/#weight

### date parameters
1. {date}.json
1. {end-date}/{period}.json
1. {start-date}/{end-date}.json

### measures
- body weight: mapped
- body mass index: mapped

### description
Returns the body weight entries for a user for the time frames specified in the request. The date formats are either (1) a single day with given format YYYY-mm-dd, (2) {base-date} with format YYYY-mm-dd and <period> as one of the enumeration [1d, 7d, 30d, 1w, 1m], or (3) {base-date} with format YYYY-mm-dd and {end-date} with format YYYY-mm-dd. We use the base-date/end-date format when shimmer requests are made for more than one day and use the single date format when only a single day is requested.

The weight entries are returned as, “weight”, an array of JSON objects, each corresponding to a body weight entry within the time period. Each has a date, time, logId, weight, and bmi property. 

The request takes an optional request header of Accept-Language, which can be used to specify the measurement unit system for the values. By default, uses metric system. 

## get sleep
- Uri: https://api.fitbit.com/{api-version}/user/{user-id}/sleep/date/{date}.json
- Reference: https://dev.fitbit.com/docs/sleep/#get-sleep-logs

### measures
- sleep duration: mapped

Sleep data points are retrieved in requests for the date in which they ended. If an individual went to bed at 9:00p on 8/22/2015 and woke up at 8:00a on 8/23/2015 (in the same time zone), then this datapoint would be returned in the request for data on 2015-08-23, but NOT on the request for data on 2015-08-22. 

Returns the sleep log entries and summary for a user for a single day, specified in the request as a date in the format YYYY-mm-dd. The response content contains a JSON array, “sleep”, that contains objects related to sleep diary entries. Each entry has a “startTime” property for the time and date (in the format YYYY-mm-ddThh:mm:ss.nnn), “minutesAsleep” for that entry, “isMainSleep” which is a boolean stating whether its the main period of sleep for an individual, as well as a JSON array called “minuteData” that contains a JSON object for each minute the individual is asleep during that entry, with each object having two properties, “dateTime”, which is just a time (in the format hh:mm:ss) starting from the “startTime” date-time, and a “value” property which contains an integer from the enumeration from the following array ("1", "2", "3"), where "1" = "asleep", "2" = "awake", "3" = "really awake".  

An important consideration around sleep is that the data can be connected or unconnected and associated with a specific day. The key is to reference the startTime property of each sleep log to identify the date and time that the entry was created. The objects within the “minuteData” array then are times that immediately follow the “startTime”. So although they have no date associated, the date can be inferred from the “startTime”. If the individual starts sleeping on day 1 at 23:30, then the first “minuteData” entry with the “dateTime” of 23:30:00, will be from the date of day 1. However a few hours later, the “minuteData” entry with the “dateTime” 02:30:00 will actually be for the following day, so would have the date of the next day. 

# issues
The Fitbit API does not provide time zone information for any data points, making it impossible to determine the instant that events took place or data points were recorded. We currently use UTC as the time zone for all data, even data that would not have occured in that time zone, because we have no other information to work with. 
