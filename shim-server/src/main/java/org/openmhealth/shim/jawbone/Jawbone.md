### still a scratch pad for now...

# general
docs main page: https://jawbone.com/up/developer/

# getting started on Jawbone

1. sign up at https://jawbone.com/start/signup
1. create an app from https://jawbone.com/up/developer/account
  1. set the "OAuth redirect URLs" to point to your application   
  1. take note of the "Client Id" and "App Secret" 
 
# api
- api url: https://jawbone.com/nudge/api/v.1.1/users/@me
- version: v.1.1
- supports retrieval based on modification date: updated_after parameter  

## authentication

- protocol: OAuth 2.0
- reference: https://jawbone.com/up/developer/authentication
- authorization URL: https://jawbone.com/auth/oauth2/auth
- flows 
  - authorization code
    - https://jawbone.com/up/developer/authentication
    - exchange URL: https://jawbone.com/auth/oauth2/token
- refresh token: https://jawbone.com/auth/oauth2/token
  - grant_type=refresh_token
- scope: basic_read, extended_read, location_read, friends_read, mood_read, mood_write, move_read, move_write, sleep_read, sleep_write, meal_read, meal_write, weight_read, weight_write, generic_event_read, generic_event_write, heartrate_read
- supports refresh tokens: yes
- access token: Authorization: Bearer USER_ACCESS_TOKEN
  - lifetime: 1 year
- redirect_uri: domain must match one of the domains specified as a redirect_uri for the API developer account of the client key/secret being used

# pagination
- supported: yes
- default page size: 10
- page size parameter:  limit
- next page parameter: next, endpoint uri that can be appended to "https://jawbone.com/"
- previous page parameters: prev, not always available
- size gives number of entries in current page
- ordering of pages:
  - items are listed newest to oldest when no time frame is specified in the request
  - items are listed from oldest to newest when a time frame is specified in the request

## incremental data
- endpoints accept a ‘updated_after’ parameter that retrieves items that were updated after a specific date

## time zone and time representation
- Most data points return datetime as unix epoch nanoseconds that are aligned to UTC

## rate limit
- n/a

## other
- If-Modified-Since supported, times are specified in GMT according to HTTP spec

## endpoints
All endpoint responses contain two properties, one named ‘meta’ and one named ‘data’, both of which are JSON objects. 

### endpoint parameters
- Required parameters: None

- Optional parameters
  - date -  formatted as YYYYMMDD, a single date of data for which to return 
  - page_token - timestamp, in unix epoch seconds, used to paginate the list of sleeps, used to refer to a specific page
  - start_time -  Epoch timestamp, in seconds, denoting the beginning of the time frame to retrieve, used along with end_time
  - end_time - epoch timestamp, in seconds, of the end of the time frame to retrieve, used along with start_time.  
  - limit - the maximum number of datapoints to be retrieved 
  - updated_after - epoch timestamp to list events that have been updated later than the timestamp

### get body events
- Endpoint: /nudge/api/v.1.1/users/@me/body_events
- Reference: https://jawbone.com/up/developer/endpoints/body

#### measures
- body weight: mapped
- bmi: mapped

#### description
The endpoint returns different body measurements for an individual, each of which contains body measures such as weight, bmi, and body fat percentage.

#### response
The ‘data’ property contains an array, ‘items’, that contains a list of data points (JSON objects) each of which represents a different body measurement. Each body measurement is a set of measures (e.g., body weight, body mass index, etc) taken at a specific time.  

### get moves
- endpoint: /nudge/api/v.1.1/users/@me/moves
- reference: https://jawbone.com/up/developer/endpoints/moves

#### measures
- step count: mapped
- calories burned: not mapped, problematic representation

#### description
Returns activity information for each day. There is only one moves entry per day, so that entry contains all of the summarized activity information for that day as well as detailed activity information within that moves datapoint. Moves data is only created through the Up app and jawbone wearables. 

#### response
The ‘data’ property contains an array, ‘items’, that contains a list of data points (JSON objects) each of which represents a single day and the activities that occurred on that day. Within each item there is summary information as well as a ‘details’ property which is a JSON object containing properties describing the activities of the day, such as ‘distance’ and ‘active_time.’ The ‘details’ object contains a dictionary, ‘hourly_totals,’ where the key is a timestamp for a given hour of that day and the value is an object with key/value pairs capturing the different activities that occurred during that hour. There is also a ‘tzs’ array that is a list of arrays representing the timezones that the user was in at different times during the hour. Each array is a timestamp paired with a timezone, representing transitions to new timezones. 

### get sleeps
- endpoint: /nudge/api/v.1.1/users/@me/sleeps
- reference: https://jawbone.com/up/developer/endpoints/sleeps

#### description
Returns a list of sleep activities for the user, including sleep details and summary information.

#### measures
- sleep duration: mapped

#### response
The ‘data’ property contains an array, ‘items’, that contains a list of data points (JSON objects) each of which represents a sleep session and the information regarding that session. Each datapoint contains a ‘details’ object with more information about the sleep session including the timezone and timeslept.  

‘Duration’ is the total time spent for the sleep session, intended to be a “total time in bed” time. ‘Duration’ - ‘awake’ = the actual sleeping duration = ‘light’ + ‘sound’ (or ‘deep’) + ‘rem’

### get workouts
- endpoint: /nudge/api/v.1.1/users/@me/workouts
- reference: https://jawbone.com/up/developer/endpoints/workouts

#### measures
- physical activity: mapped
- calories-burned: not mapped, concerns about the formulas used in calculating BMR and effective calories

#### description 
Retrieves a list of workouts that the user has completed. Workouts can be logged through Jawbone devices and applications as well as by 3rd parties. 

#### response
The ‘data’ property contains an array, ‘items’, that contains a list of data points (JSON objects) each of which represents a discrete workout activity in which the user engaged. Within each datapoint there is a ‘details’ object that contains information about the activity as well as the timezone. 

### get heartrates
- endpoint: /nudge/api/v.1.1/users/@me/heartrates
- reference: https://jawbone.com/up/developer/endpoints/heartrate

#### measures
- heart rate: mapped

#### description
Retrieves heart rate measurements from Jawbone specific devices. 

#### response
The ‘data’ property contains an array, ‘items’, that contains a list of data points (JSON objects) each of which represents a heart rate measurement recorded by a Jawbone device. Within each datapoint there is a ‘details’ object that contains information about the activity as well as the timezone. 



