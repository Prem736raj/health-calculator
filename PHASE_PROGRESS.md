# Product development progress

This file is the source of truth for the sequential product-development phases. Phase 1 was complete before this takeover and was not reworked except where a genuine trust regression was found.

## Phase 1 — Production & Play-Store Safety

- **Status:** Complete (inherited)
- **Major changes:** Modern Android/Firebase configuration, release safety, App Check architecture, Health Connect steps permission, notification/reminder safety, Room migrations, privacy/legal pages, CI and release artifacts.
- **Tests:** Inherited green CI, unit tests, lint, debug/release APK and release AAB builds.
- **Known limitations:** Firebase Console, Play Console and signing credentials remain deployment-owner tasks.
- **Next phase:** Medical accuracy and trust audit.

## Phase 2 — Medical Accuracy & Trust

- **Status:** Complete
- **Major changes:** Centralized adult input policy; corrected BMI, BMR, ideal-weight, calorie/TDEE, hydration, waist-ratio, metabolic-marker, blood-pressure, BSA, macro, heart-rate and VO₂ estimate behavior; removed unsupported visceral-fat and population-average claims; reframed the custom Health Score as the non-clinical Wellness Score; fixed TEF double-counting; added transparent methodology, limitations, references and safer result copy.
- **Tests:** Added `MedicalCalculatorAccuracyTest` coverage for formulas, boundaries, invalid values, unit behavior, AHA-style blood-pressure categories, South Asian waist cutoffs, TDEE/TEF accounting, macro/BSA/heart-rate/VO₂ behavior and non-clinical score semantics. Debug unit tests pass.
- **Known limitations:** These calculators remain informational estimates. Clinical validation, personalized medical advice, pediatric/pregnancy interpretation and any medication decisions are intentionally outside the app scope.
- **Next phase:** Simplify navigation and product information architecture.

## Phase 3 — Simplify Product Structure

- **Status:** Complete
- **Major changes:** Replaced the old four-item Home/History/Profile/Settings bottom navigation with five focused areas: Home, Track, Calculators, Insights and Profile. Added dedicated Track, Calculators and Insights hubs that keep high-value actions discoverable while moving detailed logs, reports, education, settings and advanced tools behind progressive navigation. Preserved existing routes for deep links and older saved navigation state.
- **Tests:** Added `NavigationStructureTest` to lock the primary route set and ensure History/Settings remain secondary destinations. Debug unit tests pass.
- **Known limitations:** Existing feature screens remain available as secondary destinations; the next phase will reduce the Home surface itself and unify its daily-value hierarchy.
- **Next phase:** Redesign Home around a concise daily check-in and progressive disclosure.

## Phase 4 — Redesign Home Experience

- **Status:** Complete
- **Major changes:** Replaced the calculator-heavy, long Home grid with a compact daily wellness dashboard. Added a five-second greeting and context, a clearly non-clinical Wellness Score card, four priority daily metric cards (steps, water, weight and calories), latest saved metrics, at most two explainable recommendation previews, focused quick actions, concise calculator discovery and first-use/partial-data/Health Connect empty states. Kept deterministic app prompts visibly separate from the AI Wellness Assistant.
- **Tests:** Added `HomeDashboardPolicyTest` to lock the four daily-value metrics and progressive-disclosure limits for latest metrics and insight previews. Debug unit tests pass.
- **Known limitations:** Home step data remains optional and read-only through the existing minimal Health Connect permission. Weight history and comparisons are expanded in Phase 7.
- **Next phase:** Establish a cohesive, accessible Material 3 brand and visual system.

## Phase 5 — Brand & Visual Identity

- **Status:** Complete
- **Major changes:** Established a calm blue-green wellness palette with explicit light/dark foreground and container pairs, semantic interpretation colors, a restrained chart palette, shared spacing tokens, and a tighter Material 3 shape scale. Replaced runtime Google Font fetching with the platform sans-serif stack for reliable offline startup and accessibility settings. Updated widgets and onboarding accents, standardized exported/report branding to Health Metrics Tracker, and removed obsolete font certificate/dependency resources. Corrected onboarding copy that implied medical-grade or blanket WHO validation.
- **Tests:** Added `ThemeTokensTest` for primary/container contrast targets, spacing-token invariants and semantic color aliases. Debug unit tests pass after the theme refactor.
- **Known limitations:** Older calculator screens still contain some local legacy color literals; new and refreshed surfaces use the shared theme. A follow-up calculator UI pass will migrate remaining high-traffic screens to semantic tokens.
- **Next phase:** Upgrade every calculator with consistent input validation, explanations, sources, persistence and reusable result structure.

## Phase 6 — Upgrade Every Calculator

- **Status:** Complete
- **Major changes:** Added a catalog-driven quality contract for all ten calculator entry points with plain-language purpose, required inputs, method, interpretation, limitations, sources and related tools. The Calculators hub now exposes this context before launch through a consistent information dialog. Removed the obsolete WHR visceral-fat/advanced-metrics flow, model and share output because waist measurements cannot estimate visceral fat. Aligned calorie input validation with the adult 18–120 policy, accepted the domain-supported 2–75% body-fat range, and rejected malformed optional body-fat input.
- **Tests:** Added `CalculatorQualityCatalogTest` to guarantee one complete, linked definition per calculator and extended `MedicalCalculatorAccuracyTest` for adult and body-fat boundaries. Focused debug tests, `test`, `lintDebug`, `assembleDebug`, `assembleRelease` and `bundleRelease` all pass.
- **Known limitations:** Existing calculator result screens still have some legacy local styling and several calculators use their own history UI; the hub contract provides consistent discovery while a later UI consolidation can migrate remaining screens to shared components.
- **Next phase:** Improve daily tracking, trends, goals and healthy retention loops.

## Phase 7 — Tracking & Retention Engine

- **Status:** Complete
- **Major changes:** Added a shared tracking-quality policy for safe weight/water/note/date bounds, optional-clock tolerance and explainable period comparisons. Weight logging now supports edit and delete, metric/imperial validation, seven- and thirty-day average comparisons, goal progress that handles overshoot correctly, and trend-based estimates only when the observed direction supports the goal. Water logging now validates quick-add entries and surfaces save errors through a real snackbar. Home uses the latest logged weight instead of treating the profile baseline as current data, and the wellness summary includes that latest log.
- **Tests:** Added `TrackingQualityPolicyTest` coverage for inclusive boundaries, invalid values, note limits, future-date tolerance, average/percent comparisons, zero-baseline handling and consecutive-day streaks. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Weight and water remain manual trackers; Health Connect imports are intentionally handled in Phase 8. Streaks remain informational and are not used to shame users or gate safety content.
- **Next phase:** Add optional, feature-led Health Connect integrations with graceful permission handling.

## Phase 8 — Health Connect 2.0

- **Status:** Complete
- **Major changes:** Replaced the single global permission assumption with feature-scoped read-only permissions. Steps continues to request only `READ_STEPS`; weight is now a separate, explicitly requested `READ_WEIGHT` feature with a bounded latest-record reader. Settings and the Connections screen explain why each feature is optional, show refreshable values, handle denied/unavailable access without crashing, and point users to Android Health Connect settings for revocation. Removed the misleading Backup & Restore entry from Settings because no secure portable backup architecture is ready.
- **Tests:** Extended `HealthConnectScopeTest` to verify one-permission steps scope, separate weight scope and absence of write permissions. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Health Connect availability and records depend on the user’s installed provider and granted access. The app does not write records, import sleep/heart-rate data, or silently request permissions; Play Console health-data declarations remain required before publishing.
- **Next phase:** Build deterministic, explainable insights from local tracking history before any AI interpretation.

## Phase 9 — Smart Insights

- **Status:** Complete
- **Major changes:** Added a deterministic, explainable insight engine for weight, hydration, steps, blood-pressure logging frequency and saved weight-goal context. Insights use bounded seven-day comparisons, show their evidence, avoid causal or diagnostic claims, and link directly to the relevant tracker or connection screen. Home and the Insights hub now surface these observations before optional AI interpretation, with a useful empty state for people who have not logged recently.
- **Tests:** Added `DeterministicInsightEngineTest` coverage for week-over-week weight comparisons, small-change stability wording, hydration goal/tracked-day counts, no-data actions and recorded-day step comparisons. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Steps currently arrive as the latest Health Connect value rather than a persisted multi-day history, so step comparisons become richer after a history-backed reader is added. Insights remain informational pattern descriptions and do not infer causes, risk or diagnoses.
- **Next phase:** Strengthen the consent-based, context-aware AI Wellness Assistant.

## Phase 10 — Context-Aware AI Wellness Assistant

- **Status:** Complete
- **Major changes:** Added a second, independent consent switch for optional app context. When enabled, only a bounded summary of recent locally logged weight and water patterns is sent; notes, names, raw entries and calculator payloads are excluded, and the summary is not stored in chat history. Added untrusted-prompt delimiters, input-length and rapid-request limits, connectivity-aware offline messaging, failure classification, retry and clear-conversation actions, automatic scroll-to-latest behavior, and deterministic output screening for medication instructions and diagnostic certainty. Potentially urgent symptom prompts receive a clear local-care escalation while preserving the wellness-only role.
- **Tests:** Added prompt-policy, response-safety, context-minimization and failure-classification tests, including control-character cleanup, prompt injection delimiters, rate limits, medication/diagnosis blocking, emergency escalation, recent-window filtering and omission of notes/raw water amounts. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Firebase AI Logic availability, quotas, App Check and model behavior still depend on Firebase Console configuration and network service. Deterministic screening is deliberately conservative and is not a substitute for professional review; no health measurements are sent to analytics.
- **Next phase:** Build healthy retention loops around summaries, milestones, reminders and widgets.

## Phase 11 — Retention & Engagement

- **Status:** Complete
- **Major changes:** Made inactivity and evening check-ins explicitly opt-in and synchronized their Settings toggles with schedulers/receivers, including boot restoration and notification-permission prompting only when a feature is enabled. Reframed weekly reports from A–F health grades to a non-judgmental logging-rhythm snapshot while retaining legacy database fields for compatibility. Removed fabricated exercise shortfalls and hard-coded widget streaks, softened milestone/streak language, capped all notification categories with the rate limiter, switched repeating schedules to inexact alarms, and added private lock-screen redaction for health reminders and weekly summaries. Notification copy no longer exposes raw BP, weight, calorie or hydration values by default and avoids medication, “fat-burn” and shame-oriented claims.
- **Tests:** Added `WellnessEngagementPolicyTest` for opt-in defaults, rhythm copy and non-punitive streak language. Full unit tests, lint, debug APK, release APK and release AAB gates pass after the memory-safe Gradle configuration update.
- **Known limitations:** Widgets can still show intentionally selected tracker values because Android does not provide a universal widget lock-screen redaction API; users can remove widgets or adjust device privacy. Exercise minutes are omitted from weekly reports until a real exercise data source is persisted. Existing Android notification-channel importance choices remain under system/user control.
- **Next phase:** Make every report/export path user-controlled, privacy-explicit and safe to share.

## Phase 12 — Reports, Export & Sharing

- **Status:** Complete
- **Major changes:** Added a shared `ExportDisclosurePolicy` so text, CSV, JSON, PDF, image, blood-pressure and weekly/profile shares carry consistent provenance, informational-wellness labeling and a non-diagnostic disclaimer. JSON exports now identify the real package and export metadata, CSV output escapes user content safely, empty exports avoid divide-by-zero progress, and history preserves the selected export format when sharing. Weekly reports now require at least one selected section, generic export screens explain what is shared, and the obsolete user-facing Backup route was removed while legacy local backup code remains unrouteable for compatibility.
- **Tests:** Added `ExportDisclosurePolicyTest` for provenance, disclaimer, CSV metadata and idempotent share footers. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Legacy encrypted backup classes remain only to avoid breaking old local files; they are not exposed as a new backup/restore product. Play Console data-safety declarations and any future portable encrypted transfer design still require product-owner review.
- **Next phase:** Define a trust-preserving free tier and optional premium value without gating core wellness safety information.

## Phase 13 — Monetization

- **Status:** Complete (store-independent foundation)
- **Major changes:** Added a pure `PremiumFeaturePolicy` with stable future product IDs, an explicit Free/Plus boundary, and a safe entitlement seam for a later Play Billing adapter. Core calculators, basic tracking, local history, deterministic insights, weekly summaries, basic exports, privacy controls and optional Health Connect access remain free. Added `docs/MONETIZATION_PLAN.md` covering calm pricing, non-sensitive measurement, purchase/restore/refund requirements, and a no-ads-until-configured rule; no fake paywall, AdMob placement or local-only entitlement was introduced.
- **Tests:** Added `PremiumFeaturePolicyTest` for free-core coverage, unique product IDs, tier behavior and non-pressure copy. Full unit tests, lint, debug APK, release APK and release AAB gates pass.
- **Known limitations:** Play Billing products, regional pricing, entitlement verification, support/refund configuration and any AdMob account are console-owner tasks. The proposed Plus features are not active until those pieces are implemented and tested.
- **Next phase:** Research current competitors and prepare research-backed Play Store listing assets without publishing.

## Phase 14 — ASO & Launch Preparation

- **Status:** Complete (not published)
- **Major changes:** Added `docs/ASO_LAUNCH_PLAN.md` with a current competitor scan, policy-backed listing copy, keyword intent matrix, screenshot and feature-graphic brief, onboarding/promotional copy, release notes and a pre-publish checklist. Added `StoreListingPolicy` and tests for title/short-description limits, promotional/medical-certainty wording and safe screenshot overlays. Corrected public privacy, terms and support pages to use Health Metrics Tracker and removed inaccurate encrypted-Room/encrypted-backup claims; storage and export behavior now matches the implementation.
- **Tests:** Added `StoreListingPolicyTest`. Full `test`, `lintDebug`, `assembleDebug`, `assembleRelease` and `bundleRelease` gates pass.
- **Known limitations:** Play Console listing upload, screenshots/feature graphic, Health apps/Data Safety/content-rating/Health Connect declarations, public URL validation, developer account, signing and release review remain console-owner tasks. Public competitor pages do not expose reliable keyword volume or conversion data; acquisition/search data and listing experiments should validate the matrix after launch.
- **Next phase:** Add privacy-safe product analytics architecture and a measurable retention funnel.

## Phase 15 — Analytics-Driven Growth

- **Status:** Complete (privacy-safe, opt-in foundation)
- **Major changes:** Added a stable `ProductAnalytics` event contract and strict allowlist that drops sensitive, numeric and free-form values; added a Firebase Analytics reflection adapter with manifest default-off collection and an explicit Settings opt-in; instrumented app open/onboarding/navigation, calculators, trackers, Health Connect, reminders, AI and reports; documented the measurable funnel and retention metrics; and updated the privacy policy with the optional analytics disclosure.
- **Tests:** Added `ProductAnalyticsPolicyTest` for stable event names, allowlisted dimensions, sensitive-value dropping and required growth events. Full `test`, `lintDebug`, `assembleDebug`, `assembleRelease` and `bundleRelease` gates pass.
- **Known limitations:** Firebase Console event registration, retention windows, access roles, Data Safety declaration and any BigQuery export remain console-owner tasks. No health measurements are sent in app-defined parameters, and product analytics stays off until the user enables it.
- **Next phase:** All requested product phases are implemented; complete console-owned launch work and validate the funnel in a closed test.

## Phases 6–15

| Phase | Status | Next focus |
| --- | --- | --- |
| 6 — Calculator quality | Complete | Reusable calculator structure, validation, history |
| 7 — Tracking and retention engine | Complete | Fast logging, trends, goals and reminders |
| 8 — Health Connect 2.0 | Complete | Permission-led visible integrations |
| 9 — Smart insights | Complete | Deterministic explainable insight engine |
| 10 — Context-aware AI assistant | Complete | Consent-based context, safe UX and limits |
| 11 — Retention and engagement | Complete | Opt-in summaries, gentle reminders and truthful widgets |
| 12 — Reports, export and sharing | Complete | User-controlled wellness reports |
| 13 — Monetization | Complete | Trust-preserving free/premium boundaries |
| 14 — ASO and launch preparation | Complete | Research-backed store assets and copy |
| 15 — Analytics-driven growth | Complete | Console setup and closed-test funnel validation |
