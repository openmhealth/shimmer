### still a scratch pad for now...

# general
main page: https://build.misfit.com/docs/

# getting started on Misfit

1. sign up at https://build.misfit.com/signup
1. create an app https://build.misfit.com/apps
  1. set the Application Domain to wherever you will redirect your users 
  1. take note of the "App Key" and "App Secret" 
 
# api
api url: https://api.misfitwearables.com
supports retrieval based on modification date: no 

## authentication

- protocol: OAuth 2.0
- https://build.misfit.com/docs/references#APIReferences-Authorize3rd-partyapptoaccessShinedata
- authorization URL: https://api.misfitwearables.com/auth/dialog/authorize
- flows 
  - authorization code
    - https://build.misfit.com/docs/references#APIReferences-Getaccesstokenfromauthorizedcode
    - exchange URL: https://api.misfitwearables.com/auth/tokens/exchange
  - implicit
- scope: public,birthday,email (full list not yet supported)
- supports refresh tokens: yes/no
- access token: access_token=USER_ACCESS_TOKEN or Authorization: Bearer USER_ACCESS_TOKEN


# rate limit

- total: n/a
- per user: 150/hr
- limit header: X-RateLimit-Limit:150
- remaining header: X-RateLimit-Remaining:148
- next reset time header: X-RateLimit-Reset:1404298869

# endpoints

profile
- https://build.misfit.com/docs/references#APIReferences-Profile

device
- https://api.misfitwearables.com/move/resource/v1/user/:userId/device
- https://build.misfit.com/docs/references#APIReferences-Device
- product:
  -  shine

summary
- description: daily summary
- https://api.misfitwearables.com/move/resource/v1/user/:userId/activity/summary?start_date=X&end_date=Y&detail=true
- https://build.misfit.com/docs/references#APIReferences-Summary
- limited to 31 days, error if longer
- supports time zone: no
measures:
    steps
    calories
    activityCalories
    distance

sessions
- description: workouts
- https://build.misfit.com/docs/references#APIReferences-Session
- https://api.misfitwearables.com/move/resource/v1/user/:userId/activity/sessions
- limited to 31 days, error if longer
- supports time zone: offset
- measures:
  -  activity
  -  duration
  -  steps
  -  calories Burned
  -  distance
    
sleep
- description: sleep information
- https://api.misfitwearables.com/move/resource/v1/user/:userId/activity/sleeps
- https://build.misfit.com/docs/references#APIReferences-Sleep
- limited to 31 days, error if longer
- measures:
  -  duration
  -  sleep details (awake, deep sleep, sleep)

## issues

how do we decouple summary from sessions, if summaries *contain* sessions?
