// ============================================================
// File: ui/theme/Color.kt
// Purpose: Define ALL colors used in SignLink.
//
// Design philosophy:
//   - Deep navy/teal for trust and accessibility
//   - Vibrant cyan accent for interactive elements
//   - High contrast for users with visual impairments
//   - Separate light and dark palette
// ============================================================

package com.signlink.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand Primary Colors ──────────────────────────────────────
// Deep teal - main brand color, conveys trust and calm
val SignLinkTeal900 = Color(0xFF003D4D)
val SignLinkTeal800 = Color(0xFF005262)
val SignLinkTeal700 = Color(0xFF006D82)
val SignLinkTeal600 = Color(0xFF008A9E)
val SignLinkTeal500 = Color(0xFF00A3BB)   // ← Primary brand color
val SignLinkTeal400 = Color(0xFF33B8CC)
val SignLinkTeal300 = Color(0xFF66CDDD)
val SignLinkTeal200 = Color(0xFF99E0EB)
val SignLinkTeal100 = Color(0xFFCCF0F5)
val SignLinkTeal50  = Color(0xFFE5F7FA)

// ── Accent Colors ─────────────────────────────────────────────
// Bright cyan - used for active states, CTAs, highlights
val SignLinkCyan     = Color(0xFF00D4E8)
val SignLinkCyanDark = Color(0xFF00A8C0)

// ── Status Colors ─────────────────────────────────────────────
// These follow universal UX conventions (green=good, red=error)
val SignLinkConnected    = Color(0xFF2DD882)  // ✅ BLE connected
val SignLinkDisconnected = Color(0xFFFF5252)  // ❌ BLE disconnected
val SignLinkConnecting   = Color(0xFFFFB300)  // ⏳ BLE connecting
val SignLinkListening    = Color(0xFF00D4E8)  // 🎙️ Mic active
val SignLinkProcessing   = Color(0xFFAB82FF)  // ⚙️ AI processing

// ── Neutral Colors ────────────────────────────────────────────
val SignLinkNeutral950 = Color(0xFF0A0E14)  // Near black
val SignLinkNeutral900 = Color(0xFF111827)
val SignLinkNeutral800 = Color(0xFF1F2937)
val SignLinkNeutral700 = Color(0xFF374151)
val SignLinkNeutral600 = Color(0xFF4B5563)
val SignLinkNeutral500 = Color(0xFF6B7280)
val SignLinkNeutral400 = Color(0xFF9CA3AF)
val SignLinkNeutral300 = Color(0xFFD1D5DB)
val SignLinkNeutral200 = Color(0xFFE5E7EB)
val SignLinkNeutral100 = Color(0xFFF3F4F6)
val SignLinkNeutral50  = Color(0xFFF9FAFB)
val SignLinkWhite      = Color(0xFFFFFFFF)

// ── High Contrast Mode Colors ─────────────────────────────────
// Pure black/white for maximum accessibility
val HighContrastBackground = Color(0xFF000000)
val HighContrastSurface    = Color(0xFF1A1A1A)
val HighContrastText       = Color(0xFFFFFFFF)
val HighContrastAccent      = Color(0xFFFFFF00)  // Yellow for max contrast

// ── Material 3 Seed Color ─────────────────────────────────────
// Material 3 generates an entire color scheme from this one seed
val SignLinkSeed = SignLinkTeal500