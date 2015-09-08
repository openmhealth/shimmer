### still a scratch pad for now...

# general
docs main page: https://http://developer.runkeeper.com/healthgraph/overview/

# getting started on RunKeeper

1. sign up at http://runkeeper.com/partner
1. create an app http://runkeeper.com/partner/applications/view
  1. check the Read Health Information and Retain Health Information permission requests  
  1. click Keys and URLs and take note of the "Client ID" and "Client Secret" 
 
# api
api url: https://api.misfitwearables.com
supports retrieval based on modification date: modifiedNoEarlierThan and modifiedNoLaterThan query parameters  

## authentication

- protocol: OAuth 2.0
- http://runkeeper.com/developer/healthgraph/registration-authorization
- authorization URL: https://runkeeper.com/apps/authorize
- deauthorization URL: https://runkeeper.com/apps/de-authorize
- flows 
  - authorization code
    - http://runkeeper.com/developer/healthgraph/registration-authorization
    - exchange URL: https://runkeeper.com/apps/token
- scope: ?
- supports refresh tokens: yes/no
- access token: access_token=USER_ACCESS_TOKEN or Authorization: Bearer USER_ACCESS_TOKEN

# pagination
- supported: by feed endpoints
- page size defaults to 25, overridden using pageSize query parameter
- size gives total number of entries across all pages
- items are listed newest to oldest

# other
- If-Modified-Since supported, times are specified in GMT according to HTTP spec

# rate limit

- n/a

# endpoints

application/vnd.com.runkeeper.FitnessActivity+json
- description: workouts
- http://runkeeper.com/developer/healthgraph/fitness-activities
- supports time zone: utc offset, but not fractional
measures:
    physical activity: mapped
    average calories: not mapped, pending refactor
    calories: not mapped, pending refactor
    average heart rate: not mapped, pending refactor
    heart rate: not mapped, pending refactor
    
application/vnd.com.runkeeper.WeightSetFeed+json
- description: weight
- http://runkeeper.com/developer/healthgraph/weight-sets
- supports time zone: no
measures:
    body weight: not mapped because of time zones

## issues
