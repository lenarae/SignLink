// ============================================================
// File: viewmodel/CalibrationViewModel.kt
// Purpose: ViewModel that drives the calibration wizard UI.
//
// Exposes:
//   - sessionState: full calibration session state machine
//   - signalStrength: 0–1 float for the signal bar animation
//   - channelQualities: 8 electrode quality readings
//   - currentStep: computed convenience property
//   - overallProgress: 0–1 across ALL steps for the top bar
// ============================================================

package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.bluetooth.CalibrationRepository
import com.signlink.app.data.bluetooth.CalibrationSessionState
import com.signlink.app.data.bluetooth.CALIBRATION_STEPS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository
) : ViewModel() {

    // ── Direct repository flows ───────────────────────────────
    val sessionState: StateFlow<CalibrationSessionState> =
        calibrationRepository.sessionState

    val signalStrength: StateFlow<Float> =
        calibrationRepository.signalStrength

    val channelQualities: StateFlow<List<Float>> =
        calibrationRepository.channelQualities

    // ── Computed: current step number (1–4, or 0 if not started) ─
    val currentStepNumber: StateFlow<Int> = sessionState
        .map { state ->
            when (state) {
                is CalibrationSessionState.InProgress -> state.step
                is CalibrationSessionState.Complete   -> CALIBRATION_STEPS.size
                else -> 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /**
     * Overall progress 0.0–1.0 across all steps.
     * Used by the top progress bar.
     * e.g. completing step 2 of 4 = 0.5
     */
    val overallProgress: StateFlow<Float> = sessionState
        .map { state ->
            when (state) {
                is CalibrationSessionState.NotStarted -> 0f
                is CalibrationSessionState.Complete   -> 1f
                is CalibrationSessionState.Failed     -> 0f
                is CalibrationSessionState.InProgress -> {
                    val completedSteps = (state.step - 1).toFloat()
                    val stepFraction   = when (val ss = state.stepState) {
                        is com.signlink.app.data.bluetooth.CalibrationStepState.Running -> ss.progress
                        is com.signlink.app.data.bluetooth.CalibrationStepState.Done    -> 1f
                        else -> 0f
                    }
                    (completedSteps + stepFraction) / CALIBRATION_STEPS.size
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    // ══════════════════════════════════════════════════════════
    // USER ACTIONS
    // ══════════════════════════════════════════════════════════

    fun startCalibration()   = calibrationRepository.startCalibration()
    fun resetCalibration()   = calibrationRepository.reset()
    fun retryCalibration()   = calibrationRepository.retry()
}