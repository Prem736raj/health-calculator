package com.health.calculator.bmi.tracker.calculator

data class EthnicityWaistCutoff(
    val groupName: String,
    val maleWaistCm: Float,
    val femaleWaistCm: Float,
    val regions: String
)

data class StandardResult(
    val standardName: String,
    val shortName: String,
    /** A marker count that mirrors the published definition; not a diagnosis. */
    val isMet: Boolean,
    val criteriaMetCount: Int,
    val totalCriteria: Int,
    val requiredForDiagnosis: String,
    val notes: List<String>,
    val criteriaDetails: List<StandardCriterionDetail>,
    val isClinicallyValidated: Boolean = false
)

data class StandardCriterionDetail(
    val name: String,
    val threshold: String,
    val userValue: String,
    val isMet: Boolean,
    val isRequired: Boolean
)

data class MultiStandardComparison(
    val atpResult: StandardResult,
    val idfResult: StandardResult,
    val whoResult: StandardResult,
    val allAgree: Boolean,
    val disagreementNote: String?,
    val selectedEthnicity: Ethnicity,
    val idfEthnicityWaistThreshold: Float
)

enum class Ethnicity(
    val displayName: String,
    val maleWaistCm: Float,
    val femaleWaistCm: Float,
    val description: String
) {
    EUROPID("European / Caucasian", 94f, 80f, "Europe, Sub-Saharan Africa, Eastern Mediterranean, Middle East"),
    SOUTH_ASIAN("South Asian / Chinese", 90f, 80f, "South Asia, China, South & Central America"),
    JAPANESE("Japanese", 90f, 80f, "Japan"),
    ETHNIC_SOUTH_ASIAN("South Asian (Specific)", 90f, 80f, "India, Pakistan, Bangladesh, Sri Lanka"),
    US_ATP("US / ATP III Standard", 102f, 88f, "United States (ATP III cutoffs)");

    companion object { fun getAll(): List<Ethnicity> = entries.toList() }
}

/**
 * Keeps historical definitions visible for education, but never fabricates a
 * WHO result from measurements this app cannot collect (e.g. microalbuminuria
 * or a glucose-tolerance test).
 */
object MetabolicSyndromeStandards {
    fun evaluateAllStandards(
        waistCm: Float,
        isMale: Boolean,
        systolic: Float,
        diastolic: Float,
        fastingGlucoseMgDl: Float,
        triglyceridesMgDl: Float,
        hdlMgDl: Float,
        onWaistMedication: Boolean,
        onBpMedication: Boolean,
        onGlucoseMedication: Boolean,
        onTriglyceridesMedication: Boolean,
        onHdlMedication: Boolean,
        ethnicity: Ethnicity,
        bmi: Float? = null,
        hasConfirmedInsulinResistance: Boolean = false
    ): MultiStandardComparison {
        val atp = buildFiveMarkerResult(
            name = "ATP III / NCEP (historical reference)",
            shortName = "ATP III",
            waistCm = waistCm,
            waistThreshold = if (isMale) 102f else 88f,
            isMale = isMale,
            systolic = systolic,
            diastolic = diastolic,
            glucose = fastingGlucoseMgDl,
            triglycerides = triglyceridesMgDl,
            hdl = hdlMgDl,
            waistMedication = onWaistMedication,
            bpMedication = onBpMedication,
            glucoseMedication = onGlucoseMedication,
            triglycerideMedication = onTriglyceridesMedication,
            hdlMedication = onHdlMedication,
            notes = listOf("Any 3 of 5 markers meet the historical ATP III definition.", "This app reports a screening reference, not a diagnosis.")
        )
        val idfWaist = if (isMale) ethnicity.maleWaistCm else ethnicity.femaleWaistCm
        val idf = buildFiveMarkerResult(
            name = "IDF (reference)",
            shortName = "IDF",
            waistCm = waistCm,
            waistThreshold = idfWaist,
            isMale = isMale,
            systolic = systolic,
            diastolic = diastolic,
            glucose = fastingGlucoseMgDl,
            triglycerides = triglyceridesMgDl,
            hdl = hdlMgDl,
            waistMedication = onWaistMedication,
            bpMedication = onBpMedication,
            glucoseMedication = onGlucoseMedication,
            triglycerideMedication = onTriglyceridesMedication,
            hdlMedication = onHdlMedication,
            notes = listOf(
                "Central waist measurement is required, plus any 2 of the other 4 markers.",
                "The waist reference is ethnicity-specific: ${ethnicity.displayName}.",
                "This app reports a screening reference, not a diagnosis."
            ),
            required = true
        )
        val who = StandardResult(
            standardName = "WHO (historical reference)",
            shortName = "WHO",
            isMet = false,
            criteriaMetCount = 0,
            totalCriteria = 5,
            requiredForDiagnosis = "Requires laboratory/clinical information not collected by this app",
            notes = listOf(
                "Not scored here: the historical WHO definition requires confirmed insulin resistance and urine albumin or equivalent clinical testing.",
                "Fasting glucose alone is not a validated substitute for insulin-resistance testing.",
                "Use this screen for education only and ask a clinician about formal assessment."
            ),
            criteriaDetails = emptyList()
        )
        val allAgree = atp.isMet == idf.isMet
        return MultiStandardComparison(
            atpResult = atp,
            idfResult = idf,
            whoResult = who,
            allAgree = allAgree,
            disagreementNote = if (!allAgree) "Definitions use different waist references and rules. A difference is expected and is not a diagnosis." else null,
            selectedEthnicity = ethnicity,
            idfEthnicityWaistThreshold = idfWaist
        )
    }

    private fun buildFiveMarkerResult(
        name: String,
        shortName: String,
        waistCm: Float,
        waistThreshold: Float,
        isMale: Boolean,
        systolic: Float,
        diastolic: Float,
        glucose: Float,
        triglycerides: Float,
        hdl: Float,
        waistMedication: Boolean,
        bpMedication: Boolean,
        glucoseMedication: Boolean,
        triglycerideMedication: Boolean,
        hdlMedication: Boolean,
        notes: List<String>,
        required: Boolean = false
    ): StandardResult {
        val hdlThreshold = if (isMale) 40f else 50f
        val details = listOf(
            StandardCriterionDetail("Central waist measurement", "≥ %.0f cm".format(waistThreshold), "%.1f cm".format(waistCm), waistCm >= waistThreshold || waistMedication, required),
            StandardCriterionDetail("Elevated triglycerides", "≥ 150 mg/dL", "%.0f mg/dL".format(triglycerides), triglycerides >= 150f || triglycerideMedication, false),
            StandardCriterionDetail("Reduced HDL", "< %.0f mg/dL".format(hdlThreshold), "%.0f mg/dL".format(hdl), hdl < hdlThreshold || hdlMedication, false),
            StandardCriterionDetail("Elevated blood pressure", "≥ 130 systolic or ≥ 85 diastolic", "%.0f/%.0f mmHg".format(systolic, diastolic), systolic >= 130f || diastolic >= 85f || bpMedication, false),
            StandardCriterionDetail("Elevated fasting glucose", "≥ 100 mg/dL", "%.0f mg/dL".format(glucose), glucose >= 100f || glucoseMedication, false)
        )
        val count = details.count { it.isMet }
        val central = details.first().isMet
        val otherCount = details.drop(1).count { it.isMet }
        val meetsDefinition = if (required) central && otherCount >= 2 else count >= 3
        return StandardResult(
            standardName = name,
            shortName = shortName,
            isMet = meetsDefinition,
            criteriaMetCount = count,
            totalCriteria = 5,
            requiredForDiagnosis = if (required) "Central waist marker + any 2 of 4 other markers" else "Any 3 of 5 markers",
            notes = notes,
            criteriaDetails = details
        )
    }

    fun getEthnicityWaistCutoffs(): List<EthnicityWaistCutoff> = listOf(
        EthnicityWaistCutoff("European / Caucasian", 94f, 80f, "Europe, Sub-Saharan Africa, Mediterranean, Middle East"),
        EthnicityWaistCutoff("South Asian", 90f, 80f, "India, Pakistan, Bangladesh, Sri Lanka"),
        EthnicityWaistCutoff("Chinese", 90f, 80f, "China, Hong Kong, Taiwan"),
        EthnicityWaistCutoff("Japanese", 90f, 80f, "Japan"),
        EthnicityWaistCutoff("South & Central American", 90f, 80f, "South America, Central America"),
        EthnicityWaistCutoff("US / ATP III", 102f, 88f, "United States (NCEP standard)")
    )
}
