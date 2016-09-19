### still a scratch pad for now...

# general
main page: https://developer.microsoftband.com/
docs: https://developer.microsoftband.com/Content/docs/MS%20Health%20API%20Getting%20Started.pdf
3rd party swagger description: https://raw.githubusercontent.com/DickvdBrink/MSHealthCloudApi.Net/master/SwaggerTypeGenerator/api/cloudapi.json

# getting started on Microsoft Health (also referred to as Microsoft Band)

1. sign up at https://account.live.com/developers/applications
  1. Add an app (give it any name)
  1. take note of the "Application ID". 
  1. Generate a new password - this becomes your client_secret
  1. Click add platform, select Web and as a redirect URL point that to wherever Shimmer is accessible eg http://localhost:8083/authorize/microsoft/callback
 
# api
api url: https://api.microsofthealth.net/v1/me
supports retrieval based on modification date: no 

## authentication

- protocol: OAuth 2.0
- auth url:  https://login.live.com/oauth20_authorize.srf?client_id={client_id}&scope={scope}&response_type=code&redirect_uri={redirect_uri}
- flows 
  - implicit
- scope: mshealth.ReadProfile, mshealth.ReadActivityHistory, mshealth.ReadDevices, mshealth.ReadActivityLocation, offline_access
- supports refresh tokens: yes
- access token:  Authorization: Bearer USER_ACCESS_TOKEN

# pagination
- supported: yes

# rate limit

- total: n/a
- max requests: 60/sec, 500/min
- response bandwidth: 1MB/min
- rate limit lockout per violation: 30sec

# endpoints

profile
- https://api.microsofthealth.net/v1/me/Profile/

device
-  https://api.microsofthealth.net/v1/me/Devices/{DeviceId}


summary
- https://api.microsofthealth.net/V1/me/Summaries/{period}?start_date=X&end_date=Y&detail=true
- description: daily summary
- supports time zone: yes, ISO8601 format
measures:
    steps: mapped
    calories: mapped
    heartRate: mapped
    physicalActivity: mapped, tested with data from docs
    sleep data: mapped, tested with data from docs

