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

- **Status:** Not started
- **Major changes:** —
- **Tests:** —
- **Known limitations:** —
- **Next phase:** Audit navigation, feature hierarchy and dead/duplicate screens.

## Phases 4–15

| Phase | Status | Next focus |
| --- | --- | --- |
| 4 — Home experience | Not started | Daily-value home surface and empty states |
| 5 — Brand and visual identity | Not started | Coherent Material 3 design system |
| 6 — Calculator quality | Not started | Reusable calculator structure, validation, history |
| 7 — Tracking and retention engine | Not started | Fast logging, trends, goals and reminders |
| 8 — Health Connect 2.0 | Not started | Permission-led visible integrations |
| 9 — Smart insights | Not started | Deterministic explainable insight engine |
| 10 — Context-aware AI assistant | Not started | Consent-based context, safe UX and limits |
| 11 — Retention and engagement | Not started | Healthy summaries, milestones and widgets |
| 12 — Reports, export and sharing | Not started | User-controlled wellness reports |
| 13 — Monetization | Not started | Trust-preserving free/premium boundaries |
| 14 — ASO and launch preparation | Not started | Research-backed store assets and copy |
| 15 — Analytics-driven growth | Not started | Privacy-safe funnel and product analytics |
