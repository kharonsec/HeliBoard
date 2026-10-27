/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import helium314.keyboard.event.HapticEvent;
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode;
import helium314.keyboard.latin.common.Constants;
import helium314.keyboard.latin.settings.SettingsValues;

/**
 * This class gathers audio feedback and haptic feedback functions.
 * <p>
 * It offers a consistent and simple interface that allows LatinIME to forget about the
 * complexity of settings and the like.
 */
public final class AudioAndHapticFeedbackManager {
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    // crisp vibration effects, null if not supported by the device
    private VibrationEffect mCrispKeyEffect;
    private VibrationEffect mCrispSpecialKeyEffect;
    private VibrationEffect mCrispRepeatEffect;

    private SettingsValues mSettingsValues;
    private boolean mSoundOn;
    private boolean mDoNotDisturb;

    private static final AudioAndHapticFeedbackManager sInstance =
            new AudioAndHapticFeedbackManager();

    public static AudioAndHapticFeedbackManager getInstance() {
        return sInstance;
    }

    private AudioAndHapticFeedbackManager() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final Context context) {
        sInstance.initInternal(context);
    }

    private void initInternal(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        initCrispEffects();
    }

    private void initCrispEffects() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !hasVibrator()) {
            return;
        }
        if (mVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK,
                VibrationEffect.Composition.PRIMITIVE_TICK)) {
            mCrispKeyEffect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f).compose();
            mCrispSpecialKeyEffect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f).compose();
            mCrispRepeatEffect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f).compose();
        } else if (mVibrator.areAllEffectsSupported(VibrationEffect.EFFECT_CLICK, VibrationEffect.EFFECT_HEAVY_CLICK,
                VibrationEffect.EFFECT_TICK) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES) {
            mCrispKeyEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            mCrispSpecialKeyEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
            mCrispRepeatEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
        }
    }

    public boolean supportsCrispHaptics() {
        return mCrispKeyEffect != null;
    }

    public void performHapticAndAudioFeedback(
        final int code,
        final View viewToPerformHapticFeedbackOn,
        final HapticEvent hapticEvent
    ) {
        performHapticFeedback(viewToPerformHapticFeedbackOn, hapticEvent, code);
        performAudioFeedback(code, hapticEvent);
    }

    public boolean hasVibrator() {
        return mVibrator != null && mVibrator.hasVibrator();
    }

    public void vibrate(final long milliseconds) {
        if (mVibrator == null || milliseconds <= 0) {
            return;
        }
        mVibrator.vibrate(milliseconds);
    }

    private boolean reevaluateIfSoundIsOn() {
        if (mSettingsValues == null || !mSettingsValues.mSoundOn || mAudioManager == null || mDoNotDisturb) {
            return false;
        }
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    public void performAudioFeedback(final int code, final HapticEvent hapticEvent) {
        // if mAudioManager is null, we can't play a sound anyway, so return
        if (mAudioManager == null) {
            return;
        }
        if (!mSoundOn) {
            return;
        }
        if (hapticEvent != HapticEvent.KEY_PRESS) {
            return;
        }
        final int sound = switch (code) {
            case KeyCode.DELETE -> AudioManager.FX_KEYPRESS_DELETE;
            case Constants.CODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN;
            case Constants.CODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR;
            default -> AudioManager.FX_KEYPRESS_STANDARD;
        };
        mAudioManager.playSoundEffect(sound, mSettingsValues.mKeypressSoundVolume);
    }

    public void performHapticFeedback(final View viewToPerformHapticFeedbackOn, final HapticEvent hapticEvent) {
        performHapticFeedback(viewToPerformHapticFeedbackOn, hapticEvent, KeyCode.NOT_SPECIFIED);
    }

    public void performHapticFeedback(final View viewToPerformHapticFeedbackOn, final HapticEvent hapticEvent, final int code) {
        if (!mSettingsValues.mVibrateOn || (mDoNotDisturb && !mSettingsValues.mVibrateInDndMode)) {
            return;
        }
        if (hapticEvent == HapticEvent.NO_HAPTICS) {
            // Avoid surprises with the handling of HapticFeedbackConstants.NO_HAPTICS
            return;
        }
        if (hapticEvent.allowCustomDuration && mSettingsValues.mKeypressVibrationDuration >= 0) {
            vibrate(mSettingsValues.mKeypressVibrationDuration);
            return;
        }
        if (mSettingsValues.mCrispHaptics && performCrispHapticFeedback(hapticEvent, code)) {
            return;
        }
        // Go ahead with the system default
        if (viewToPerformHapticFeedbackOn != null) {
            viewToPerformHapticFeedbackOn.performHapticFeedback(
                    hapticEvent.feedbackConstant,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private boolean performCrispHapticFeedback(final HapticEvent hapticEvent, final int code) {
        if (mCrispKeyEffect == null) {
            return false;
        }
        final VibrationEffect effect;
        if (hapticEvent == HapticEvent.KEY_PRESS) {
            final boolean isSpecialKey = code == Constants.CODE_SPACE || code == Constants.CODE_ENTER
                    || code == KeyCode.DELETE;
            effect = isSpecialKey ? mCrispSpecialKeyEffect : mCrispKeyEffect;
        } else if (hapticEvent == HapticEvent.KEY_REPEAT) {
            effect = mCrispRepeatEffect;
        } else {
            return false;
        }
        mVibrator.vibrate(effect);
        return true;
    }

    public void onSettingsChanged(final SettingsValues settingsValues) {
        mSettingsValues = settingsValues;
        mSoundOn = reevaluateIfSoundIsOn();
    }

    public void onRingerModeChanged(boolean doNotDisturb) {
        mDoNotDisturb = doNotDisturb;
        mSoundOn = reevaluateIfSoundIsOn();
    }
}
