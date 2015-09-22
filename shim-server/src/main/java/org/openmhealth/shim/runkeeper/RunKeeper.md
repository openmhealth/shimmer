### still a scratch pad for now...

# general
docs main page: https://http://developer.runkeeper.com/healthgraph/overview/

# getting started on RunKeeper

1. sign up at http://runkeeper.com/partner
1. create an app http://runkeeper.com/partner/applications/view
  1. check the Read Health Information and Retain Health Information permission requests  
  1. click Keys and URLs and take note of the "Client ID" and "Client Secret" 
 
# api
api url: https://api.runkeeper.com
supports retrieval based on modification date: modifiedNoEarlierThan and modifiedNoLaterThan query parameters  

## authentication

- protocol: OAuth 2.0
- http://runkeeper.com/developer/healthgraph/registration-authorization
- authorization URL: https://runkeeper.com/apps/authorize
- deauthorization URL: https://runkeeper.com/apps/de-authorize
- flows 
  - authorization code
    - reference: http://runkeeper.com/developer/healthgraph/registration-authorization
    - exchange URL: https://runkeeper.com/apps/token
- scope: does not support scopes
- supports refresh tokens: no, access tokens do not expire
- access token: access_token=USER_ACCESS_TOKEN or Authorization: Bearer USER_ACCESS_TOKEN

# pagination
- supported: by feed endpoints
- page size defaults to 25, overridden using pageSize query parameter
- size gives total number of entries across all pages
- items are listed newest to oldest
- 'next' property in the response body, if it exists, provides the endpoint for the next page
- 'previous' property in the response body, if it exists, provides the endpoint for the previous page

# time zone and time representation
- RunKeeper sourced datapoints contain a utc_offset field 
- API does not allow third parties to provide time zone information meaning that any data provided from a third party will not have timezone information

# other
- If-Modified-Since supported, times are specified in GMT according to HTTP spec

# rate limit

- n/a

# endpoints

## time frame parameters
- noEarlierThan
- noLaterThan
- values: take dates in the form ‘YYYY-MM-DD’ and are inclusive, meaning that they will include the dates that 
are provided as the parameters and exclude items on days that are on the next day in the case of noLaterThan or on the 
previous day in the case of noEarlierThan

## get fitness activities
- uri: /fitnessActivities
- description: workouts
- datatype: application/vnd.com.runkeeper.FitnessActivity+json 
- reference: http://developer.runkeeper.com/healthgraph/fitness-activities
- supports time zone: utc offset for runkeeper datapoints only, but not fractional
measures:
    physical activity: mapped
    calories burned: mapped
    heart rate: not mapped, not retrieved from the activity history (list of all activity datapoints)
    
## get weight
- uri: /weight
- datatype: application/vnd.com.runkeeper.WeightSetFeed+json
- description: weight
- http://runkeeper.com/developer/healthgraph/weight-sets
- supports time zone: no
measures:
    body weight: not mapped because of time zones

## issues
