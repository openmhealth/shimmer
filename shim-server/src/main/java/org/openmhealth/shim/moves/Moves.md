# general

main page: https://dev.moves-app.com/

# getting started on Moves

1. sign in at https://dev.moves-app.com. (This requires a Google account.)
1. create an app https://dev.moves-app.com/apps
  1. set the Redirect URI on the Development tab to wherever you will redirect your users
  1. take note of the "Client ID" and "Client Secret"
  
  
# api
api url: https://api.moves-app.com/api/1.1
supports retrieval based on modification date: yes
supports ETag header: yes

# authentication

- protocol: OAuth 2.0
- https://dev.moves-app.com/docs/authentication
- authorization url: https://api.moves-app.com/oauth/v1/authorize?response_type=code&client_id=<client_id>&scope=<scope>
- also supports non-browser authorization for mobile apps
- supports refresh token: yes

# pagination

- supported: no

# rate limit

- total: 2000/hr and 60/min
- per user: n/a
- limit header: X-RateLimit-HourLimit:2000, X-RateLimit-MinuteRemaining:60
- remaining header: X-RateLimit-HourRemaining:1990, X-RateLimit-MinuteRemaining:58
- too many response: 429 Too Many Requests

**NOTE: Connected Apps have double the rate limit.**

# endpoints

The storyline, activities, and summaries endpoints are similar. Storyline contains a superset of activities
data (adds track points and locations), and activities contains a superset of summary data (adds segments).

## get daily storyline

- endpoint: /user/storyline/daily
- required scopes: activity, location
- reference: https://dev.moves-app.com/docs/api_storyline
- limited to 7 day ranges

# measures
- calories-burned: not yet mapped
- step-count: mapped
- geo-position: not yet mapped
- physical-activity: mapped

## get daily activities

- endpoint: /user/activities/daily
- required scopes: activity
- reference: https://dev.moves-app.com/docs/api_activities
- probably limited to 31 days, not explicitly mentioned

# measures
- calories-burned: use storyline instead
- step-count: use storyline instead
- physical-activity: use storyline instead

## get daily summaries

- endpoint: /user/summary/daily
- required scopes: activity
- reference: https://dev.moves-app.com/docs/api_activities
- limited to 31 days

# measures
- calories-burned: use storyline instead
- step-count: use storyline instead
- physical-activity: use storyline instead





# notifications

supports callback notifications for updates: yes (shim does not support)

# issues

- Daily step count values sometimes differ from what is shown on mobile app.
