package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeCalculator
import com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeResult
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.MetabolicSyndromeTrackingRepository
import com.health.calculator.bmi.tracker.data.model.AssessmentComparison
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import com.health.calculator.bmi.tracker.data.model.MetabolicSyndromeRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MetabolicSyndromeUiState(
    // Inputs
    val waist: String = "",
    val waistUnitCm: Boolean = true,
    val isMale: Boolean = true,
    val systolic: String = "",
    val diastolic: String = "",
    val fastingGlucose: String = "",
    val glucoseUnitMgDl: Boolean = true,
    val triglycerides: String = "",
    val triglyceridesUnitMgDl: Boolean = true,
    val hdl: String = "",
    val hdlUnitMgDl: Boolean = true,

    // Medication toggles
    val onWaistMedication: Boolean = false,
    val onBpMedication: Boolean = false,
    val onGlucoseMedication: Boolean = false,
    val onTriglyceridesMedication: Boolean = false,
    val onHdlMedication: Boolean = false,

    // Result
    val result: MetabolicSyndromeResult? = null,
    val showResult: Boolean = false,
    val isSaved: Boolean = false,

    // Validation errors
    val waistError: String? = null,
    val systolicError: String? = null,
    val diastolicError: String? = null,
    val glucoseError: String? = null,
    val triglyceridesError: String? = null,
    val hdlError: String? = null,

    // Tracking
    val showTracking: Boolean = false,
    val trackingRecords: List<MetabolicSyndromeRecord> = emptyList(),
    val comparison: AssessmentComparison? = null,
    val isLabReminderEnabled: Boolean = false,
    val reminderMonths: Int = 3,

    // Multi-Standard Comparison
    val selectedEthnicity: com.health.calculator.bmi.tracker.calculator.Ethnicity =
        com.health.calculator.bmi.tracker.calculator.Ethnicity.US_ATP,
    val standardsComparison: com.health.calculator.bmi.tracker.calculator.MultiStandardComparison? = null,
    val userBmi: Float? = null,
    val hasConfirmedInsulinResistance: Boolean = false,
    val partialResult: com.health.calculator.bmi.tracker.calculator.PartialAssessmentResult? = null,
    val genderSelected: Boolean = false
)

class MetabolicSyndromeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MetabolicSyndromeUiState())
    val uiState: StateFlow<MetabolicSyndromeUiState> = _uiState.asStateFlow()

    private val historyRepository = HistoryRepository(com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(application).historyDao())
    private val trackingRepository = MetabolicSyndromeTrackingRepository(application)

    init {
        loadTrackingData()
    }

    private fun loadTrackingData() {
        val records = trackingRepository.getRecordsSorted()
        val comparison = trackingRepository.getComparison()
        val reminderEnabled = trackingRepository.isLabReminderEnabled()
        val reminderMonths = trackingRepository.getReminderMonths()

        _uiState.value = _uiState.value.copy(
            trackingRecords = records,
            comparison = comparison,
            isLabReminderEnabled = reminderEnabled,
            reminderMonths = reminderMonths
        )
    }

    fun updateWaist(value: String) {
        _uiState.value = _uiState.value.copy(waist = value, waistError = null, showResult = false)
        updatePartialAssessment()
    }

    fun toggleWaistUnit() {
        val current = _uiState.value
        val currentVal = current.waist.toFloatOrNull()
        val converted = if (currentVal != null) {
            if (current.waistUnitCm) {
                MetabolicSyndromeCalculator.cmToInches(currentVal)
            } else {
                MetabolicSyndromeCalculator.inchesToCm(currentVal)
            }
        } else null

        _uiState.value = current.copy(
            waistUnitCm = !current.waistUnitCm,
            waist = converted?.let { "%.1f".format(it) } ?: current.waist
        )
        updatePartialAssessment()
    }

    fun updateGender(isMale: Boolean) {
        _uiState.value = _uiState.value.copy(isMale = isMale, showResult = false, genderSelected = true)
        updatePartialAssessment()
    }

    fun updateSystolic(value: String) {
        _uiState.value = _uiState.value.copy(systolic = value, systolicError = null, showResult = false)
        updatePartialAssessment()
    }

    fun updateDiastolic(value: String) {
        _uiState.value = _uiState.value.copy(diastolic = value, diastolicError = null, showResult = false)
        updatePartialAssessment()
    }

    fun updateFastingGlucose(value: String) {
        _uiState.value = _uiState.value.copy(fastingGlucose = value, glucoseError = null, showResult = false)
        updatePartialAssessment()
    }

    fun toggleGlucoseUnit() {
        val current = _uiState.value
        val currentVal = current.fastingGlucose.toFloatOrNull()
        val converted = if (currentVal != null) {
            if (current.glucoseUnitMgDl) {
                MetabolicSyndromeCalculator.mgDlToMmolL_glucose(currentVal)
            } else {
                MetabolicSyndromeCalculator.mmolLToMgDl_glucose(currentVal)
            }
        } else null

        _uiState.value = current.copy(
            glucoseUnitMgDl = !current.glucoseUnitMgDl,
            fastingGlucose = converted?.let { "%.1f".format(it) } ?: current.fastingGlucose
        )
        updatePartialAssessment()
    }

    fun updateTriglycerides(value: String) {
        _uiState.value = _uiState.value.copy(triglycerides = value, triglyceridesError = null, showResult = false)
        updatePartialAssessment()
    }

    fun toggleTriglyceridesUnit() {
        val current = _uiState.value
        val currentVal = current.triglycerides.toFloatOrNull()
        val converted = if (currentVal != null) {
            if (current.triglyceridesUnitMgDl) {
                MetabolicSyndromeCalculator.mgDlToMmolL_triglycerides(currentVal)
            } else {
                MetabolicSyndromeCalculator.mmolLToMgDl_triglycerides(currentVal)
            }
        } else null

        _uiState.value = current.copy(
            triglyceridesUnitMgDl = !current.triglyceridesUnitMgDl,
            triglycerides = converted?.let { "%.1f".format(it) } ?: current.triglycerides
        )
        updatePartialAssessment()
    }

    fun updateHdl(value: String) {
        _uiState.value = _uiState.value.copy(hdl = value, hdlError = null, showResult = false)
        updatePartialAssessment()
    }

    fun toggleHdlUnit() {
        val current = _uiState.value
        val currentVal = current.hdl.toFloatOrNull()
        val converted = if (currentVal != null) {
            if (current.hdlUnitMgDl) {
                MetabolicSyndromeCalculator.mgDlToMmolL_hdl(currentVal)
            } else {
                MetabolicSyndromeCalculator.mmolLToMgDl_hdl(currentVal)
            }
        } else null

        _uiState.value = current.copy(
            hdlUnitMgDl = !current.hdlUnitMgDl,
            hdl = converted?.let { "%.1f".format(it) } ?: current.hdl
        )
        updatePartialAssessment()
    }

    fun updateWaistMedication(value: Boolean) {
        _uiState.value = _uiState.value.copy(onWaistMedication = value, showResult = false)
        updatePartialAssessment()
    }

    fun updateBpMedication(value: Boolean) {
        _uiState.value = _uiState.value.copy(onBpMedication = value, showResult = false)
        updatePartialAssessment()
    }

    fun updateGlucoseMedication(value: Boolean) {
        _uiState.value = _uiState.value.copy(onGlucoseMedication = value, showResult = false)
        updatePartialAssessment()
    }

    fun updateTriglyceridesMedication(value: Boolean) {
        _uiState.value = _uiState.value.copy(onTriglyceridesMedication = value, showResult = false)
        updatePartialAssessment()
    }

    fun updateHdlMedication(value: Boolean) {
        _uiState.value = _uiState.value.copy(onHdlMedication = value, showResult = false)
        updatePartialAssessment()
    }

    fun updateEthnicity(ethnicity: com.health.calculator.bmi.tracker.calculator.Ethnicity) {
        _uiState.value = _uiState.value.copy(selectedEthnicity = ethnicity)
        // Recalculate standards comparison if result exists
        recalculateStandardsComparison()
    }

    fun updateInsulinResistanceConfirmation(confirmed: Boolean) {
        _uiState.value = _uiState.value.copy(hasConfirmedInsulinResistance = confirmed)
        recalculateStandardsComparison()
    }

    private fun recalculateStandardsComparison() {
        val state = _uiState.value
        val result = state.result ?: return

        val waistCm = state.waist.toFloatOrNull()?.let {
            if (state.waistUnitCm) it else MetabolicSyndromeCalculator.inchesToCm(it)
        } ?: return

        val systolic = state.systolic.toFloatOrNull() ?: return
        val diastolic = state.diastolic.toFloatOrNull() ?: return

        val glucoseMgDl = state.fastingGlucose.toFloatOrNull()?.let {
            if (state.glucoseUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_glucose(it)
        } ?: return

        val trigMgDl = state.triglycerides.toFloatOrNull()?.let {
            if (state.triglyceridesUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_triglycerides(it)
        } ?: return

        val hdlMgDl = state.hdl.toFloatOrNull()?.let {
            if (state.hdlUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_hdl(it)
        } ?: return

        val comparison = com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeStandards.evaluateAllStandards(
            waistCm = waistCm,
            isMale = state.isMale,
            systolic = systolic,
            diastolic = diastolic,
            fastingGlucoseMgDl = glucoseMgDl,
            triglyceridesMgDl = trigMgDl,
            hdlMgDl = hdlMgDl,
            onWaistMedication = state.onWaistMedication,
            onBpMedication = state.onBpMedication,
            onGlucoseMedication = state.onGlucoseMedication,
            onTriglyceridesMedication = state.onTriglyceridesMedication,
            onHdlMedication = state.onHdlMedication,
            ethnicity = state.selectedEthnicity,
            bmi = state.userBmi,
            hasConfirmedInsulinResistance = state.hasConfirmedInsulinResistance
        )

        _uiState.value = _uiState.value.copy(standardsComparison = comparison)
    }

    private fun updatePartialAssessment() {
        val state = _uiState.value

        val waistCm = state.waist.toFloatOrNull()?.let {
            if (state.waistUnitCm) it else MetabolicSyndromeCalculator.inchesToCm(it)
        }
        val systolic = state.systolic.toFloatOrNull()
        val diastolic = state.diastolic.toFloatOrNull()
        val glucoseMgDl = state.fastingGlucose.toFloatOrNull()?.let {
            if (state.glucoseUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_glucose(it)
        }
        val trigMgDl = state.triglycerides.toFloatOrNull()?.let {
            if (state.triglyceridesUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_triglycerides(it)
        }
        val hdlMgDl = state.hdl.toFloatOrNull()?.let {
            if (state.hdlUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_hdl(it)
        }

        val partial = com.health.calculator.bmi.tracker.calculator.MetabolicSyndromePartialAssessment.evaluatePartial(
            waistCm = waistCm,
            isMale = state.isMale,
            systolic = systolic,
            diastolic = diastolic,
            glucoseMgDl = glucoseMgDl,
            trigMgDl = trigMgDl,
            hdlMgDl = hdlMgDl,
            onWaistMed = state.onWaistMedication,
            onBpMed = state.onBpMedication,
            onGlucoseMed = state.onGlucoseMedication,
            onTrigMed = state.onTriglyceridesMedication,
            onHdlMed = state.onHdlMedication
        )

        _uiState.value = _uiState.value.copy(partialResult = partial)
    }

    fun getLastResultSummary(): Pair<String, String>? {
        val records = trackingRepository.getRecordsSorted()
        val latest = records.firstOrNull() ?: return null
        return Pair(
            "${latest.criteriaMet}/5 criteria",
            latest.riskLevel
        )
    }

    fun toggleTracking() {
        _uiState.value = _uiState.value.copy(showTracking = !_uiState.value.showTracking)
    }

    fun calculate() {
        val state = _uiState.value
        var hasError = false

        val waistCm = state.waist.toFloatOrNull()?.let {
            if (state.waistUnitCm) it else MetabolicSyndromeCalculator.inchesToCm(it)
        }
        if (waistCm == null || waistCm < 40f || waistCm > 200f) {
            _uiState.value = state.copy(waistError = "Enter valid waist (40-200 cm)")
            hasError = true
        }

        val systolic = state.systolic.toFloatOrNull()
        if (systolic == null || systolic < 60f || systolic > 300f) {
            _uiState.value = _uiState.value.copy(systolicError = "Enter valid systolic (60-300)")
            hasError = true
        }

        val diastolic = state.diastolic.toFloatOrNull()
        if (diastolic == null || diastolic < 30f || diastolic > 200f) {
            _uiState.value = _uiState.value.copy(diastolicError = "Enter valid diastolic (30-200)")
            hasError = true
        }

        val glucoseMgDl = state.fastingGlucose.toFloatOrNull()?.let {
            if (state.glucoseUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_glucose(it)
        }
        if (glucoseMgDl == null || glucoseMgDl < 30f || glucoseMgDl > 600f) {
            _uiState.value = _uiState.value.copy(glucoseError = "Enter valid glucose value")
            hasError = true
        }

        val trigMgDl = state.triglycerides.toFloatOrNull()?.let {
            if (state.triglyceridesUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_triglycerides(it)
        }
        if (trigMgDl == null || trigMgDl < 20f || trigMgDl > 2000f) {
            _uiState.value = _uiState.value.copy(triglyceridesError = "Enter valid triglycerides value")
            hasError = true
        }

        val hdlMgDl = state.hdl.toFloatOrNull()?.let {
            if (state.hdlUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_hdl(it)
        }
        if (hdlMgDl == null || hdlMgDl < 5f || hdlMgDl > 150f) {
            _uiState.value = _uiState.value.copy(hdlError = "Enter valid HDL value")
            hasError = true
        }

        if (hasError) return

        val result = MetabolicSyndromeCalculator.evaluate(
            waistCm = waistCm!!,
            isMale = state.isMale,
            systolic = systolic!!,
            diastolic = diastolic!!,
            fastingGlucoseMgDl = glucoseMgDl!!,
            triglyceridesMgDl = trigMgDl!!,
            hdlMgDl = hdlMgDl!!,
            onWaistMedication = state.onWaistMedication,
            onBpMedication = state.onBpMedication,
            onGlucoseMedication = state.onGlucoseMedication,
            onTriglyceridesMedication = state.onTriglyceridesMedication,
            onHdlMedication = state.onHdlMedication
        )

        val comparison = com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeStandards.evaluateAllStandards(
            waistCm = waistCm!!,
            isMale = state.isMale,
            systolic = systolic!!,
            diastolic = diastolic!!,
            fastingGlucoseMgDl = glucoseMgDl!!,
            triglyceridesMgDl = trigMgDl!!,
            hdlMgDl = hdlMgDl!!,
            onWaistMedication = state.onWaistMedication,
            onBpMedication = state.onBpMedication,
            onGlucoseMedication = state.onGlucoseMedication,
            onTriglyceridesMedication = state.onTriglyceridesMedication,
            onHdlMedication = state.onHdlMedication,
            ethnicity = state.selectedEthnicity,
            bmi = state.userBmi,
            hasConfirmedInsulinResistance = state.hasConfirmedInsulinResistance
        )

        _uiState.value = _uiState.value.copy(
            result = result,
            showResult = true,
            isSaved = false,
            standardsComparison = comparison
        )
    }

    fun saveToHistory() {
        val state = _uiState.value
        val result = state.result ?: return

        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateTime = dateFormat.format(Date())

            // Convert values to base units for storage
            val waistCm = state.waist.toFloatOrNull()?.let {
                if (state.waistUnitCm) it else MetabolicSyndromeCalculator.inchesToCm(it)
            } ?: 0f

            val glucoseMgDl = state.fastingGlucose.toFloatOrNull()?.let {
                if (state.glucoseUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_glucose(it)
            } ?: 0f

            val trigMgDl = state.triglycerides.toFloatOrNull()?.let {
                if (state.triglyceridesUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_triglycerides(it)
            } ?: 0f

            val hdlMgDl = state.hdl.toFloatOrNull()?.let {
                if (state.hdlUnitMgDl) it else MetabolicSyndromeCalculator.mmolLToMgDl_hdl(it)
            } ?: 0f

            val systolic = state.systolic.toFloatOrNull() ?: 0f
            val diastolic = state.diastolic.toFloatOrNull() ?: 0f

            // Save to tracking repository
            val record = MetabolicSyndromeRecord(
                timestamp = System.currentTimeMillis(),
                dateTime = dateTime,
                criteriaMet = result.criteriaMet,
                isSyndromePresent = result.isSyndromePresent,
                riskLevel = result.riskLevel.label,
                waistCm = waistCm,
                waistMet = result.criteria[0].isMet,
                waistOnMed = state.onWaistMedication,
                systolic = systolic,
                diastolic = diastolic,
                bpMet = result.criteria[3].isMet,
                bpOnMed = state.onBpMedication,
                glucoseMgDl = glucoseMgDl,
                glucoseMet = result.criteria[4].isMet,
                glucoseOnMed = state.onGlucoseMedication,
                triglyceridesMgDl = trigMgDl,
                triglyceridesMet = result.criteria[1].isMet,
                triglyceridesOnMed = state.onTriglyceridesMedication,
                hdlMgDl = hdlMgDl,
                hdlMet = result.criteria[2].isMet,
                hdlOnMed = state.onHdlMedication,
                isMale = state.isMale
            )
            trackingRepository.saveRecord(record)

            // Save to general history
            val entry = HistoryEntry(
                calculatorKey = com.health.calculator.bmi.tracker.data.model.CalculatorType.METABOLIC_SYNDROME.key,
                resultValue = result.criteriaMet.toString(),
                resultLabel = "Criteria Met",
                category = result.riskLevel.label,
                detailsJson = com.google.gson.Gson().toJson(buildMap {
                    put("criteria_met", result.criteriaMet.toString())
                    put("syndrome_present", result.isSyndromePresent.toString())
                    put("risk_level", result.riskLevel.label)
                    put("waist_cm", "%.1f".format(waistCm))
                    put("systolic", "%.0f".format(systolic))
                    put("diastolic", "%.0f".format(diastolic))
                    put("glucose_mgdl", "%.0f".format(glucoseMgDl))
                    put("triglycerides_mgdl", "%.0f".format(trigMgDl))
                    put("hdl_mgdl", "%.0f".format(hdlMgDl))
                    put("gender", if (state.isMale) "Male" else "Female")
                    put("idf_diagnosis", result.idfDiagnosis.toString())
                    result.criteria.forEachIndexed { i, c ->
                        put("criterion_${i}_name", c.name)
                        put("criterion_${i}_met", c.isMet.toString())
                        put("criterion_${i}_medication", c.isOnMedication.toString())
                    }
                }),
                timestamp = System.currentTimeMillis()
            )
            historyRepository.addEntry(entry)

            // Reload tracking data
            loadTrackingData()

            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun setLabReminder(enabled: Boolean, months: Int) {
        trackingRepository.setLabReminder(enabled, months)
        _uiState.value = _uiState.value.copy(
            isLabReminderEnabled = enabled,
            reminderMonths = months
        )
    }

    fun resetResult() {
        _uiState.value = _uiState.value.copy(showResult = false, result = null, isSaved = false)
    }

    fun clearAll() {
        _uiState.value = MetabolicSyndromeUiState(
            trackingRecords = _uiState.value.trackingRecords,
            comparison = _uiState.value.comparison,
            isLabReminderEnabled = _uiState.value.isLabReminderEnabled,
            reminderMonths = _uiState.value.reminderMonths
        )
    }

    fun getShareText(): String {
        val state = _uiState.value
        val result = state.result ?: return ""
        val diagnosis = if (result.isSyndromePresent) "Present" else "Not Present"

        val criteriaDetails = result.criteria.joinToString("\n") { criterion ->
            val status = if (criterion.isMet) "⚠️ Abnormal" else "✅ Normal"
            val medNote = if (criterion.isOnMedication) " (Medicated)" else ""
            "  ${criterion.name}: ${criterion.userValue} → $status$medNote"
        }

        val cvRisk = com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeRecommendations
            .getCardiovascularRiskSummary(result.criteriaMet)

        val standardsText = state.standardsComparison?.let { comp ->
            """
            |
            |Standards Comparison:
            |  ATP III: ${if (comp.atpResult.isMet) "Present" else "Not Present"} (${comp.atpResult.criteriaMetCount}/5)
            |  IDF (${comp.selectedEthnicity.displayName}): ${if (comp.idfResult.isMet) "Present" else "Not Present"} (${comp.idfResult.criteriaMetCount}/5)
            |  WHO: ${if (comp.whoResult.isMet) "Present" else "Not Present"} (${comp.whoResult.criteriaMetCount}/5)
            """.trimMargin()
        } ?: ""

        return """
            |Metabolic Syndrome Assessment
            |━━━━━━━━━━━━━━━━━━━━━━━━━━
            |Criteria Met: ${result.criteriaMet}/5
            |Diagnosis: $diagnosis (ATP III)
            |Risk Level: ${result.riskLevel.label}
            |Cardiovascular Risk: ${cvRisk.riskLevel} (${cvRisk.riskScore}/100)
            |
            |Criteria Breakdown:
            |$criteriaDetails
            |$standardsText
            |${if (result.diagnosisDiffers) "\n⚠️ IDF criteria result differs: ${if (result.idfDiagnosis) "Present" else "Not Present"}\n" else ""}
            |⚕️ For educational purposes only — not medical advice.
            |
            |Assessed using Health Calculator: BMI Tracker
        """.trimMargin()
    }
}
