# Security operations

## Firebase Android configuration

`app/google-services.json` contains the Firebase Android client configuration
for `com.healthmetrics.tracker`. Its `current_key` is a Firebase-provisioned
Android client API key, not a service-account private key, password or signing
credential. Firebase documents these client identifiers as public by design;
authorization for Firebase data comes from Firebase Security Rules and App
Check, not from hiding this value.

The key must remain scoped to Firebase-related APIs. Never add a Gemini
Developer API key, service-account key, OAuth secret, signing key or other
server credential to this file or to the Android repository. Those credentials
belong in a server-side secret store.

## GitHub secret-scanning response

When GitHub flags this file:

1. Confirm the location is `app/google-services.json` and the value is the
   Firebase Android `current_key`.
2. In Google Cloud API Keys, verify that the key is Firebase-provisioned and
   restricted to Firebase-related APIs. Review quota and usage for unexpected
   clients.
3. If the key was reused for a non-Firebase API or shows unexpected usage,
   restrict it immediately, create a replacement Firebase key if required,
   download the updated config, and revoke the old key.
4. If it is the Firebase client key described above and has the required
   restrictions, resolve the GitHub alert as a false positive with a note that
   the value is public Firebase client configuration. Closing an alert does not
   replace the Google Cloud restriction review.

This repository intentionally does not contain service-account credentials,
release signing material or Gemini Developer API keys.
