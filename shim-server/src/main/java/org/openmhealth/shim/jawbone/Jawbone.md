### still a scratch pad for now...

# general
docs main page: https://jawbone.com/up/developer/

# getting started on RunKeeper

1. sign up at https://jawbone.com/start/signup
1. create an app from https://jawbone.com/up/developer/account
  1. set the "OAuth redirect URLs" to point to your application   
  1. take note of the "Client Id" and "App Secret" 
 
# api
api url: https://jawbone.com/nudge/api/v.1.1/users/@me
supports retrieval based on modification date: updated_after parameter  

## authentication

- protocol: OAuth 2.0
- https://jawbone.com/up/developer/authentication
- authorization URL: https://jawbone.com/auth/oauth2/auth
- flows 
  - authorization code
    - https://jawbone.com/up/developer/authentication
    - exchange URL: https://jawbone.com/auth/oauth2/token
- scope: extended_read, move_read, sleep_read, weight_read, heartrate_read, meal_read
- supports refresh tokens: yes
- access token: Authorization: Bearer USER_ACCESS_TOKEN

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

/workouts
- description: workouts
- https://jawbone.com/up/developer/endpoints/workouts
- supports time zone: yes
measures:
    physical activity: mapped
    step count: not mapped, pending refactor
    calories: not mapped, pending refactor
    

## issues
