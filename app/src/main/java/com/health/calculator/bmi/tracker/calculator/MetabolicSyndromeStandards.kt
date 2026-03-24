// File: app/src/main/java/com/health/calculator/bmi/tracker/calculator/MetabolicSyndromeStandards.kt
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
    val isMet: Boolean,
    val criteriaMetCount: Int,
    val totalCriteria: Int,
    val requiredForDiagnosis: String,
    val notes: List<String>,
    val criteriaDetails: List<StandardCriterionDetail>
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
    JAPANESE("Japanese", 85f, 90f, "Japan"),
    ETHNIC_SOUTH_ASIAN("South Asian (Specific)", 90f, 80f, "India, Pakistan, Bangladesh, Sri Lanka"),
    US_ATP("US / ATP III Standard", 102f, 88f, "United States (ATP III cutoffs)");

    companion object {
        fun getAll(): List<Ethnicity> = entries.toList()
    }
}

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

        val atpResult = evaluateATPIII(
            waistCm, isMale, systolic, diastolic,
            fastingGlucoseMgDl, triglyceridesMgDl, hdlMgDl,
            onWaistMedication, onBpMedication, onGlucoseMedication,
            onTriglyceridesMedication, onHdlMedication
        )

        val idfResult = evaluateIDF(
            waistCm, isMale, systolic, diastolic,
            fastingGlucoseMgDl, triglyceridesMgDl, hdlMgDl,
            onWaistMedication, onBpMedication, onGlucoseMedication,
            onTriglyceridesMedication, onHdlMedication,
            ethnicity
        )

        val whoResult = evaluateWHO(
            waistCm, isMale, systolic, diastolic,
            fastingGlucoseMgDl, triglyceridesMgDl, hdlMgDl,
            onWaistMedication, onBpMedication, onGlucoseMedication,
            onTriglyceridesMedication, onHdlMedication,
            bmi, hasConfirmedInsulinResistance
        )

        val allAgree = atpResult.isMet == idfResult.isMet && idfResult.isMet == whoResult.isMet

        val disagreementNote = if (!allAgree) {
            buildDisagreementNote(atpResult, idfResult, whoResult)
        } else null

        val idfWaistThreshold = if (isMale) ethnicity.maleWaistCm else ethnicity.femaleWaistCm

        return MultiStandardComparison(
            atpResult = atpResult,
            idfResult = idfResult,
            whoResult = whoResult,
            allAgree = allAgree,
            disagreementNote = disagreementNote,
            selectedEthnicity = ethnicity,
            idfEthnicityWaistThreshold = idfWaistThreshold
        )
    }

    private fun evaluateATPIII(
        waistCm: Float, isMale: Boolean,
        systolic: Float, diastolic: Float,
        glucoseMgDl: Float, trigMgDl: Float, hdlMgDl: Float,
        onWaistMed: Boolean, onBpMed: Boolean, onGlucoseMed: Boolean,
        onTrigMed: Boolean, onHdlMed: Boolean
    ): StandardResult {

        val waistThreshold = if (isMale) 102f else 88f
        val hdlThreshold = if (isMale) 40f else 50f

        val criteria = listOf(
            StandardCriterionDetail(
                name = "Central Obesity",
                threshold = "> ${waistThreshold.toInt()} cm (${if (isMale) "40" else "35"} in)",
                userValue = "%.1f cm".format(waistCm),
                isMet = waistCm > waistThreshold || onWaistMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Triglycerides",
                threshold = "≥ 150 mg/dL",
                userValue = "%.0f mg/dL".format(trigMgDl),
                isMet = trigMgDl >= 150f || onTrigMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Reduced HDL",
                threshold = "< ${hdlThreshold.toInt()} mg/dL",
                userValue = "%.0f mg/dL".format(hdlMgDl),
                isMet = hdlMgDl < hdlThreshold || onHdlMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Blood Pressure",
                threshold = "≥ 130/85 mmHg",
                userValue = "%.0f/%.0f mmHg".format(systolic, diastolic),
                isMet = systolic >= 130f || diastolic >= 85f || onBpMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Fasting Glucose",
                threshold = "≥ 100 mg/dL",
                userValue = "%.0f mg/dL".format(glucoseMgDl),
                isMet = glucoseMgDl >= 100f || onGlucoseMed,
                isRequired = false
            )
        )

        val metCount = criteria.count { it.isMet }

        return StandardResult(
            standardName = "ATP III / NCEP",
            shortName = "ATP III",
            isMet = metCount >= 3,
            criteriaMetCount = metCount,
            totalCriteria = 5,
            requiredForDiagnosis = "Any 3 of 5 criteria",
            notes = listOf(
                "Most widely used in the United States",
                "Adopted by the National Cholesterol Education Program",
                "No single criterion is mandatory",
                "Updated in 2005 (AHA/NHLBI) with lower glucose cutoff of 100 mg/dL"
            ),
            criteriaDetails = criteria
        )
    }

    private fun evaluateIDF(
        waistCm: Float, isMale: Boolean,
        systolic: Float, diastolic: Float,
        glucoseMgDl: Float, trigMgDl: Float, hdlMgDl: Float,
        onWaistMed: Boolean, onBpMed: Boolean, onGlucoseMed: Boolean,
        onTrigMed: Boolean, onHdlMed: Boolean,
        ethnicity: Ethnicity
    ): StandardResult {

        val waistThreshold = if (isMale) ethnicity.maleWaistCm else ethnicity.femaleWaistCm
        val hdlThreshold = if (isMale) 40f else 50f

        val waistMet = waistCm > waistThreshold || onWaistMed

        val additionalCriteria = listOf(
            StandardCriterionDetail(
                name = "Elevated Triglycerides",
                threshold = "≥ 150 mg/dL",
                userValue = "%.0f mg/dL".format(trigMgDl),
                isMet = trigMgDl >= 150f || onTrigMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Reduced HDL",
                threshold = "< ${hdlThreshold.toInt()} mg/dL",
                userValue = "%.0f mg/dL".format(hdlMgDl),
                isMet = hdlMgDl < hdlThreshold || onHdlMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Blood Pressure",
                threshold = "≥ 130/85 mmHg",
                userValue = "%.0f/%.0f mmHg".format(systolic, diastolic),
                isMet = systolic >= 130f || diastolic >= 85f || onBpMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Fasting Glucose",
                threshold = "≥ 100 mg/dL",
                userValue = "%.0f mg/dL".format(glucoseMgDl),
                isMet = glucoseMgDl >= 100f || onGlucoseMed,
                isRequired = false
            )
        )

        val waistDetail = StandardCriterionDetail(
            name = "Central Obesity (REQUIRED)",
            threshold = "> ${waistThreshold.toInt()} cm (${ethnicity.displayName})",
            userValue = "%.1f cm".format(waistCm),
            isMet = waistMet,
            isRequired = true
        )

        val allCriteria = listOf(waistDetail) + additionalCriteria
        val additionalMetCount = additionalCriteria.count { it.isMet }
        val totalMet = (if (waistMet) 1 else 0) + additionalMetCount

        val idfDiagnosis = waistMet && additionalMetCount >= 2

        return StandardResult(
            standardName = "IDF (International Diabetes Federation)",
            shortName = "IDF",
            isMet = idfDiagnosis,
            criteriaMetCount = totalMet,
            totalCriteria = 5,
            requiredForDiagnosis = "Central obesity (REQUIRED) + any 2 of 4 remaining criteria",
            notes = buildList {
                add("Central obesity is MANDATORY — you cannot have IDF-defined metabolic syndrome without it")
                add("Uses ethnicity-specific waist cutoffs for more accurate assessment")
                add("Current cutoff: ${ethnicity.displayName} — Male > ${ethnicity.maleWaistCm.toInt()} cm, Female > ${ethnicity.femaleWaistCm.toInt()} cm")
                add("Widely used internationally, especially in Europe and Asia")
                if (!waistMet) {
                    add("⚠️ Since central obesity is not met, IDF diagnosis cannot be made regardless of other criteria")
                }
            },
            criteriaDetails = allCriteria
        )
    }

    private fun evaluateWHO(
        waistCm: Float, isMale: Boolean,
        systolic: Float, diastolic: Float,
        glucoseMgDl: Float, trigMgDl: Float, hdlMgDl: Float,
        onWaistMed: Boolean, onBpMed: Boolean, onGlucoseMed: Boolean,
        onTrigMed: Boolean, onHdlMed: Boolean,
        bmi: Float?,
        hasConfirmedInsulinResistance: Boolean
    ): StandardResult {

        val hdlThreshold = if (isMale) 35f else 39f
        val whrThresholdMale = 0.90f
        val whrThresholdFemale = 0.85f

        // WHO uses insulin resistance as mandatory
        // We estimate it from glucose since we can't do a clamp test
        val estimatedIR = glucoseMgDl >= 110f || hasConfirmedInsulinResistance || onGlucoseMed

        val irDetail = StandardCriterionDetail(
            name = "Insulin Resistance (REQUIRED)",
            threshold = "Fasting glucose ≥ 110 mg/dL or confirmed IGT/diabetes",
            userValue = if (hasConfirmedInsulinResistance) "Confirmed" else "%.0f mg/dL (estimated)".format(glucoseMgDl),
            isMet = estimatedIR,
            isRequired = true
        )

        val waistOrBmiMet = (bmi != null && bmi > 30f) ||
                waistCm > (if (isMale) 102f else 88f) || onWaistMed

        val additionalCriteria = listOf(
            StandardCriterionDetail(
                name = "Obesity",
                threshold = "BMI > 30 or WHR > ${if (isMale) "0.90" else "0.85"}",
                userValue = buildString {
                    if (bmi != null) append("BMI: %.1f".format(bmi))
                    append(" | Waist: %.1f cm".format(waistCm))
                },
                isMet = waistOrBmiMet,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Dyslipidemia",
                threshold = "TG ≥ 150 mg/dL or HDL < ${hdlThreshold.toInt()} mg/dL",
                userValue = "TG: %.0f | HDL: %.0f mg/dL".format(trigMgDl, hdlMgDl),
                isMet = trigMgDl >= 150f || hdlMgDl < hdlThreshold || onTrigMed || onHdlMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Elevated Blood Pressure",
                threshold = "≥ 140/90 mmHg",
                userValue = "%.0f/%.0f mmHg".format(systolic, diastolic),
                isMet = systolic >= 140f || diastolic >= 90f || onBpMed,
                isRequired = false
            ),
            StandardCriterionDetail(
                name = "Microalbuminuria",
                threshold = "Urinary albumin ≥ 20 μg/min or ACR ≥ 30 mg/g",
                userValue = "Not tested",
                isMet = false, // We cannot assess this from available data
                isRequired = false
            )
        )

        val allCriteria = listOf(irDetail) + additionalCriteria
        val additionalMetCount = additionalCriteria.count { it.isMet }
        val totalMet = (if (estimatedIR) 1 else 0) + additionalMetCount

        val whoDiagnosis = estimatedIR && additionalMetCount >= 2

        return StandardResult(
            standardName = "WHO (World Health Organization)",
            shortName = "WHO",
            isMet = whoDiagnosis,
            criteriaMetCount = totalMet,
            totalCriteria = 5,
            requiredForDiagnosis = "Insulin resistance (REQUIRED) + any 2 of 4 additional factors",
            notes = buildList {
                add("Insulin resistance is MANDATORY — requires confirmed impaired glucose tolerance, diabetes, or fasting glucose ≥ 110 mg/dL")
                add("Uses higher BP threshold (140/90) compared to ATP III/IDF (130/85)")
                add("Includes microalbuminuria as a criterion (not assessable here)")
                add("Uses lower HDL threshold: Male < 35, Female < 39 mg/dL")
                add("Published in 1999 — one of the earliest formal definitions")
                if (!estimatedIR) {
                    add("⚠️ Insulin resistance criterion is estimated from fasting glucose. A formal glucose tolerance test may give different results.")
                }
                add("ℹ️ Microalbuminuria requires a urine test and cannot be assessed in this app")
            },
            criteriaDetails = allCriteria
        )
    }

    private fun buildDisagreementNote(
        atp: StandardResult,
        idf: StandardResult,
        who: StandardResult
    ): String {
        val parts = mutableListOf<String>()

        if (atp.isMet != idf.isMet) {
            parts.add(
                "ATP III and IDF differ because IDF requires central obesity as mandatory and uses " +
                "lower ethnicity-specific waist cutoffs, while ATP III counts any 3 of 5 criteria equally."
            )
        }

        if (atp.isMet != who.isMet || idf.isMet != who.isMet) {
            parts.add(
                "WHO criteria differ because they require confirmed insulin resistance " +
                "(fasting glucose ≥ 110 mg/dL or glucose tolerance test) and use higher BP threshold (140/90). " +
                "WHO also includes microalbuminuria which cannot be assessed here."
            )
        }

        return parts.joinToString("\n\n")
    }

    fun getEthnicityWaistCutoffs(): List<EthnicityWaistCutoff> {
        return listOf(
            EthnicityWaistCutoff("European / Caucasian", 94f, 80f, "Europe, Sub-Saharan Africa, Mediterranean, Middle East"),
            EthnicityWaistCutoff("South Asian", 90f, 80f, "India, Pakistan, Bangladesh, Sri Lanka"),
            EthnicityWaistCutoff("Chinese", 90f, 80f, "China, Hong Kong, Taiwan"),
            EthnicityWaistCutoff("Japanese", 85f, 90f, "Japan"),
            EthnicityWaistCutoff("South & Central American", 90f, 80f, "South America, Central America"),
            EthnicityWaistCutoff("US / ATP III", 102f, 88f, "United States (NCEP standard)")
        )
    }
}
