# Privacy-safe analytics and growth plan

Status: architecture implemented; measurement is opt-in and not published as a growth claim.

Health Metrics Tracker should learn which product surfaces help people return without turning personal measurements into a marketing dataset. The app therefore has a vendor-neutral `ProductAnalytics` contract, an allowlisted event policy, and a Firebase Analytics adapter whose collection is disabled by default.

## Consent and data boundary

- Product analytics is off on a new install.
- Users can change **Settings > Privacy > Share optional product usage** at any time.
- The Android manifest and the adapter both keep Firebase collection disabled until that setting is enabled.
- Feature code can send only event names plus small allowlisted labels such as `calculator_id`, `surface`, `tracker_type`, `source`, `permission_type`, `report_type`, `format`, and `reminder_type`.
- The policy drops unknown keys, free-form text, numbers and values outside a fixed vocabulary.
- Never send measurements, calculator results, scores, goals, dates, notes, symptoms, profile fields, AI prompts/responses, exported content, email addresses or account identifiers as event parameters.
- Firebase may process standard pseudonymous app/device information required for its service when a user opts in; the app does not add direct identifiers.

The analytics adapter is intentionally small. If the provider changes, feature code and the privacy policy should remain unchanged.

## Event contract

| Event | Safe dimensions | Product question |
| --- | --- | --- |
| `app_opened` | launch source | Are people returning after install or a notification/widget entry? |
| `onboarding_completed` | none | Where does activation stop? |
| `surface_opened` | home, track, calculators, insights, profile | Which primary area earns repeat use? |
| `calculator_opened` / `calculator_completed` | calculator id, entry point | Which calculators are discovered and completed? |
| `tracker_opened` | tracker type, entry point | Which daily trackers are adopted? |
| `water_logged` / `weight_logged` | source only | Do quick logging paths create a second session? |
| `health_connect_connected` | permission type (steps or weight) | Is optional Health Connect useful after explanation? |
| `insight_opened` | insight type | Do explainable trends lead to deeper review? |
| `ai_assistant_opened` | entry point | Is the optional assistant discoverable without becoming the product? |
| `weekly_report_opened` | report type | Are summaries a healthy return reason? |
| `report_exported` | report type, format | Which user-controlled sharing paths are useful? |
| `reminder_enabled` | reminder type | Do opt-in prompts support retention without spam? |

Event names are stable snake case and are tested for uniqueness. Add a new event only when it answers a product question and can be represented without sensitive values.

## Funnel and cohort definitions

The first release should use a simple funnel, with aggregate counts and no health-value segmentation:

1. Install / first app open
2. Onboarding completed
3. First meaningful action (a calculator completion or first tracker log)
4. Second session (`app_opened` on a later day)
5. Tracker activation (two tracker opens or a log event)
6. Reminder activation (only after the user enables a reminder)
7. Optional Health Connect adoption
8. Weekly retention (weekly report opened or a return session in days 7–13)

Use privacy-preserving cohorts such as app version, country/region supplied by the platform, Android version and acquisition source only where Play/Firebase provides them lawfully. Do not create cohorts from BMI, weight, blood pressure, symptoms, age, sex, pregnancy status or free-form text.

## Metrics to review

- D1, D7 and D30 retention
- Onboarding completion rate
- First meaningful action rate
- Calculator open-to-completion rate by calculator id
- Tracker adoption and repeat logging rate
- Health Connect connection rate by permission type
- AI assistant opens and safe-response completion rate (counts only)
- Weekly report open and export usage
- Reminder enablement and disablement (counts only)

Review small cohorts cautiously; suppress or aggregate dashboards when counts are too small to avoid re-identification. These metrics describe product behaviour, not health outcomes.

## Implementation and release sequence

1. Keep the default-off consent and event policy covered by unit tests.
2. Verify Firebase Analytics is disabled on a fresh install and after the user turns the setting off.
3. In Firebase Console, register the stable event names and mark only the approved dimensions as custom definitions.
4. Add a debug-only event inspector or local recording implementation for QA; never log payloads in production.
5. Run a closed test with empty data, denied permissions, offline mode, export flows and both consent states.
6. Set retention windows and access roles in Firebase Console; export to BigQuery only after a separate privacy review.
7. Establish a baseline for two weeks before changing onboarding, reminders or paywall experiments.

Console configuration, data-safety declarations, retention windows, access roles and any BigQuery export remain release-owner tasks. No analytics claim should be made until those settings are verified.
