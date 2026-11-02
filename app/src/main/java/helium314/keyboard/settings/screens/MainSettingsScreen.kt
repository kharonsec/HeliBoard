// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.JniUtils
import helium314.keyboard.latin.utils.NextScreenIcon
import helium314.keyboard.latin.utils.SubtypeLocaleUtils.displayName
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.UncachedInputMethodManagerUtils
import helium314.keyboard.latin.utils.previewDark
import helium314.keyboard.settings.GroupDivider
import helium314.keyboard.settings.IconTile
import helium314.keyboard.settings.SearchSettingsScreen
import helium314.keyboard.settings.SettingsGroup
import helium314.keyboard.settings.initPreview
import helium314.keyboard.settings.preferences.Preference
import helium314.keyboard.settings.screens.gesturedata.END_DATE_EPOCH_MILLIS
import helium314.keyboard.settings.screens.gesturedata.TWO_WEEKS_IN_MILLIS
import kotlinx.coroutines.delay

// icon tile colors of the main settings sections
private val blue = Color(0xFF0A84FF)
private val green = Color(0xFF34C759)
private val purple = Color(0xFFAF52DE)
private val indigo = Color(0xFF5856D6)
private val pink = Color(0xFFFF2D55)
private val orange = Color(0xFFFF9500)
private val teal = Color(0xFF30B0C7)
private val brown = Color(0xFFA2845E)
private val yellow = Color(0xFFF2B600)
private val gray = Color(0xFF8E8E93)

// divider inset: row padding + icon tile + spacing, so dividers start at the text
private const val ROW_DIVIDER_INSET = 16 + 30 + 14

@Composable
fun MainSettingsScreen(
    onClickAbout: () -> Unit,
    onClickTextCorrection: () -> Unit,
    onClickPreferences: () -> Unit,
    onClickToolbar: () -> Unit,
    onClickGestureTyping: () -> Unit,
    onClickDataGathering: () -> Unit,
    onClickAdvanced: () -> Unit,
    onClickAppearance: () -> Unit,
    onClickLanguage: () -> Unit,
    onClickLayouts: () -> Unit,
    onClickDictionaries: () -> Unit,
    onClickBack: () -> Unit,
) {
    SearchSettingsScreen(
        onClickBack = onClickBack,
        title = stringResource(R.string.ime_settings),
        settings = emptyList(),
        isMainScreen = true,
    ) {
        val enabledSubtypes = SubtypeSettings.getEnabledSubtypes(true)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
        ) {
            SetupCard()

            // typing
            SettingsGroup {
                Section(
                    stringResource(R.string.language_and_layouts_title),
                    enabledSubtypes.joinToString(", ") { it.displayName() },
                    R.drawable.ic_settings_languages, blue, onClickLanguage
                )
                GroupDivider(ROW_DIVIDER_INSET)
                Section(
                    stringResource(R.string.settings_screen_correction),
                    stringResource(R.string.main_summary_correction),
                    R.drawable.ic_settings_correction, green, onClickTextCorrection
                )
                if (JniUtils.sHaveGestureLib) {
                    GroupDivider(ROW_DIVIDER_INSET)
                    Section(
                        stringResource(R.string.settings_screen_gesture),
                        stringResource(R.string.main_summary_gesture),
                        R.drawable.ic_settings_gesture, purple, onClickGestureTyping
                    )
                }
                // we don't even show the menu if data gathering phase ended more than 2 weeks ago
                if (JniUtils.sHaveGestureLib && System.currentTimeMillis() < END_DATE_EPOCH_MILLIS + TWO_WEEKS_IN_MILLIS) {
                    GroupDivider(ROW_DIVIDER_INSET)
                    Section(
                        stringResource(R.string.gesture_data_screen),
                        stringResource(R.string.main_summary_gesture_data),
                        R.drawable.ic_settings_gesture, indigo, onClickDataGathering
                    )
                }
            }

            // look and feel
            SettingsGroup {
                Section(
                    stringResource(R.string.settings_screen_appearance),
                    stringResource(R.string.main_summary_appearance),
                    R.drawable.ic_settings_appearance, pink, onClickAppearance
                )
                GroupDivider(ROW_DIVIDER_INSET)
                Section(
                    stringResource(R.string.settings_screen_preferences),
                    stringResource(R.string.main_summary_preferences),
                    R.drawable.ic_settings_preferences, orange, onClickPreferences
                )
                GroupDivider(ROW_DIVIDER_INSET)
                Section(
                    stringResource(R.string.settings_screen_toolbar),
                    stringResource(R.string.main_summary_toolbar),
                    R.drawable.ic_settings_toolbar, teal, onClickToolbar
                )
            }

            // more
            SettingsGroup {
                Section(
                    stringResource(R.string.dictionary_settings_category),
                    stringResource(R.string.main_summary_dictionaries),
                    R.drawable.ic_dictionary, brown, onClickDictionaries
                )
                GroupDivider(ROW_DIVIDER_INSET)
                Section(
                    stringResource(R.string.settings_screen_secondary_layouts),
                    stringResource(R.string.main_summary_secondary_layouts),
                    R.drawable.ic_settings_layout, yellow, onClickLayouts
                )
                GroupDivider(ROW_DIVIDER_INSET)
                Section(
                    stringResource(R.string.settings_screen_advanced),
                    stringResource(R.string.main_summary_advanced),
                    R.drawable.ic_settings_advanced, gray, onClickAdvanced
                )
            }

            SettingsGroup {
                Section(
                    stringResource(R.string.settings_screen_about),
                    null,
                    R.drawable.ic_settings_about, gray, onClickAbout
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Section(name: String, description: String?, icon: Int, color: Color, onClick: () -> Unit) {
    Preference(
        name = name,
        description = description,
        onClick = onClick,
        icon = icon,
        iconColor = color
    ) { NextScreenIcon() }
}

/** Shows how to enable and select the keyboard, as long as that's not done. */
@Composable
private fun SetupCard() {
    val ctx = LocalContext.current
    val imm = remember { ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    fun isEnabled() = UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm)
    fun isCurrent() = UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm)
    var enabled by remember { mutableStateOf(isEnabled()) }
    var current by remember { mutableStateOf(isCurrent()) }
    if (enabled && current) return
    // the user changes this in system settings or the input method picker, so check regularly while shown
    LaunchedEffect(Unit) {
        while (!enabled || !current) {
            delay(1000)
            enabled = isEnabled()
            current = isCurrent()
        }
    }
    val appName = stringResource(R.string.english_ime_name)
    val (title, instruction, action) = if (!enabled) Triple(
        stringResource(R.string.setup_step1_title, appName),
        stringResource(R.string.setup_step1_instruction, appName),
        stringResource(R.string.setup_step1_action)
    ) else Triple(
        stringResource(R.string.setup_step2_title, appName),
        stringResource(R.string.setup_step2_instruction, appName),
        stringResource(R.string.setup_step2_action)
    )
    SettingsGroup {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTile(if (enabled) R.drawable.ic_setup_select else R.drawable.ic_setup_key, MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                instruction,
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    if (!enabled) {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(intent)
                    } else {
                        imm.showInputMethodPicker()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(48.dp)
            ) { Text(action, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Preview
@Composable
private fun PreviewScreen() {
    initPreview(LocalContext.current)
    Theme(previewDark) {
        Surface {
            MainSettingsScreen({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        }
    }
}
