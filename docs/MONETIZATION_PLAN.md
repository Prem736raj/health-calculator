# Health Metrics Tracker monetization plan

Health Metrics Tracker is a personal wellness companion, not a medical device. Monetization must preserve trust, privacy and useful daily value.

## Product principles

- Keep all informational calculators, basic logging and safety/disclaimer content usable without payment.
- Never put an ad, paywall or upgrade prompt beside a concerning result or urgent-care guidance.
- Do not use health measurements, symptoms, notes or profile details for ad targeting or purchase messaging.
- Use clear, calm pricing and a visible way to manage or cancel a purchase through Google Play.
- Do not create urgency, shame, streak pressure or claims that a paid tier improves health outcomes.
- Do not show a purchase flow until Play products, pricing, entitlement verification and refund handling are configured.

## Proposed boundary

### Free plan

- All informational calculators and their methods, limitations and references.
- Basic weight, water, food and blood-pressure logging with local history.
- Deterministic insights and weekly wellness summaries.
- CSV/JSON wellness-data exports and optional read-only Health Connect connections.
- Privacy, medical-disclaimer and data-management controls.

### Optional Plus plan

The first paid tier can add convenience and depth without withholding core safety information:

- Extended trend windows and richer comparisons.
- Detailed report layouts and additional export customization.
- More widget customization.
- A clearly bounded, optional AI Wellness Assistant usage allowance.

These are product proposals, not active entitlements. The app currently has no billing dependency or purchasable product IDs.

## Implementation gate before selling

1. Configure Play Console products, regional prices, tax/subscription disclosures and a support contact.
2. Add the current supported Google Play Billing library and a small billing adapter behind `PremiumFeaturePolicy`.
3. Verify purchases and restore entitlement state using Play Billing responses; never trust a local boolean alone.
4. Handle pending, cancelled, refunded, expired, offline and account-change states without blocking free features.
5. Add an account-independent privacy explanation for purchase state and a “Manage subscription” link.
6. Test the complete purchase, restore, refund and failure flows in Play Billing license-test accounts before release.
7. Use a remote-configured feature rollout only after entitlement verification and analytics checks are live.

## Ads

Ads are not included in the current build. If they are tested later, use Google test ad units only during development, keep ads away from sensitive results, and never send health data as ad-request parameters. Replace test IDs only after an approved AdMob account and Play data-safety review exist.

## Measurement

Measure conversion and retention with aggregate, non-health events only. Do not log the value of a metric, calculator result, symptom, profile field or report contents to analytics.
