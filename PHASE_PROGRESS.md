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

## Phases 6–15

| Phase | Status | Next focus |
| --- | --- | --- |
| 6 — Calculator quality | Complete | Reusable calculator structure, validation, history |
| 7 — Tracking and retention engine | Complete | Fast logging, trends, goals and reminders |
| 8 — Health Connect 2.0 | In progress | Permission-led visible integrations |
| 9 — Smart insights | Not started | Deterministic explainable insight engine |
| 10 — Context-aware AI assistant | Not started | Consent-based context, safe UX and limits |
| 11 — Retention and engagement | Not started | Healthy summaries, milestones and widgets |
| 12 — Reports, export and sharing | Not started | User-controlled wellness reports |
| 13 — Monetization | Not started | Trust-preserving free/premium boundaries |
| 14 — ASO and launch preparation | Not started | Research-backed store assets and copy |
| 15 — Analytics-driven growth | Not started | Privacy-safe funnel and product analytics |
