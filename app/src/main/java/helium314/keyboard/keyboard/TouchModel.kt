// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.latin.common.StringUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Learns where the user actually touches letter keys, as offset from the key center.
 * Samples are taken from committed words: for each letter of the committed word, the touch position
 * of the corresponding tap is compared to the center of the key of that letter. So taps that were
 * corrected (e.g. by autocorrect) are counted for the key the user intended to hit.
 * Samples of a word are only used once the next word is committed, so words that are immediately
 * reverted are not learned.
 * Offsets are stored separately for each display orientation, as fraction of key width / height.
 */
object TouchModel {
    // offsets are only used for keys with at least this many samples
    private const val MIN_SAMPLES = 15
    // running average over roughly this many recent samples, so the model follows changes in typing behavior
    private const val MAX_SAMPLES = 150
    // maximum offset of a key center, as fraction of key size
    private const val MAX_OFFSET = 0.35f
    // samples further away from the key center (as fraction of key size) are ignored as outliers
    private const val MAX_SAMPLE_DEVIATION = 0.6f
    private const val SAVE_INTERVAL = 20
    private const val PREF_DATA = "touch_model_data"

    private class KeyStats(var dx: Float, var dy: Float, var count: Int)
    private class Sample(val orientation: Int, val codePoint: Int, val dx: Float, val dy: Float)

    // key is orientation in upper 32 bits and lowercase code point in lower 32 bits
    private val stats = HashMap<Long, KeyStats>()
    private var pending: List<Sample> = emptyList()
    private var unsavedSamples = 0
    private var loaded = false
    @Volatile
    private var cachedOffsets: Map<Int, Map<Int, FloatArray>> = emptyMap()

    /** Learned offsets for [orientation], or null if no key has enough samples yet. */
    @JvmStatic
    fun offsets(orientation: Int): Map<Int, FloatArray>? = cachedOffsets[orientation]

    @JvmStatic
    @Synchronized
    fun load(prefs: SharedPreferences) {
        if (loaded) return
        loaded = true
        prefs.getString(PREF_DATA, null)?.split(";")?.forEach { entry ->
            val values = entry.split(",")
            if (values.size != 5) return@forEach
            val orientation = values[0].toIntOrNull() ?: return@forEach
            val codePoint = values[1].toIntOrNull() ?: return@forEach
            val dx = values[2].toFloatOrNull() ?: return@forEach
            val dy = values[3].toFloatOrNull() ?: return@forEach
            val count = values[4].toIntOrNull() ?: return@forEach
            stats[key(orientation, codePoint)] = KeyStats(dx, dy, min(count, MAX_SAMPLES))
        }
        updateCachedOffsets()
    }

    @JvmStatic
    @Synchronized
    fun save(prefs: SharedPreferences) {
        if (!loaded || unsavedSamples == 0) return
        unsavedSamples = 0
        val data = stats.entries.joinToString(";") { (key, s) ->
            "${key ushr 32},${key.toInt()},${s.dx},${s.dy},${s.count}"
        }
        prefs.edit { putString(PREF_DATA, data) }
    }

    @JvmStatic
    @Synchronized
    fun reset(prefs: SharedPreferences) {
        stats.clear()
        pending = emptyList()
        unsavedSamples = 0
        cachedOffsets = emptyMap()
        prefs.edit { remove(PREF_DATA) }
    }

    /**
     * Collect samples for a committed word. Previously collected samples are added to the model.
     *
     * @param committedWord the word that was committed (may differ from the typed word due to corrections)
     * @param typedWord the word as it was typed
     * @param xCoordinates touch x coordinates of the typed letters, in keyboard coordinates
     * @param yCoordinates touch y coordinates of the typed letters, in keyboard coordinates
     * @param pointerCount number of valid coordinates
     */
    @JvmStatic
    @Synchronized
    fun onWordCommitted(committedWord: String, typedWord: String, xCoordinates: IntArray, yCoordinates: IntArray,
                        pointerCount: Int, keyboard: Keyboard?, orientation: Int, prefs: SharedPreferences) {
        if (keyboard == null || !keyboard.mId.element.isAlphabet) {
            onWordCommitted(committedWord, typedWord, xCoordinates, yCoordinates, 0, orientation, prefs) { null }
            return
        }
        onWordCommitted(committedWord, typedWord, xCoordinates, yCoordinates, pointerCount, orientation, prefs) {
            keyboard.getKey(it) ?: keyboard.getKey(Character.toUpperCase(it))
        }
    }

    @VisibleForTesting
    @Synchronized
    internal fun onWordCommitted(committedWord: String, typedWord: String, xCoordinates: IntArray, yCoordinates: IntArray,
                        pointerCount: Int, orientation: Int, prefs: SharedPreferences, findKey: (Int) -> Key?) {
        if (!loaded) load(prefs)
        addPendingSamples(prefs)
        val committed = StringUtils.toCodePointArray(committedWord).map { Character.toLowerCase(it) }
        // only words where each tap belongs to one letter of the committed word
        if (committed.size != typedWord.codePointCount(0, typedWord.length) || committed.size > pointerCount) return
        val samples = ArrayList<Sample>(committed.size)
        for (i in committed.indices) {
            val codePoint = committed[i]
            val x = xCoordinates[i]
            val y = yCoordinates[i]
            if (!Character.isLetter(codePoint) || x == Constants.NOT_A_COORDINATE || y == Constants.NOT_A_COORDINATE) continue
            val key = findKey(codePoint) ?: continue
            if (key.width <= 0 || key.height <= 0) continue
            val dx = (x - (key.x + key.width / 2f)) / key.width
            val dy = (y - (key.y + key.height / 2f)) / key.height
            if (dx * dx + dy * dy > MAX_SAMPLE_DEVIATION * MAX_SAMPLE_DEVIATION) continue
            samples.add(Sample(orientation, codePoint, dx, dy))
        }
        pending = samples
    }

    /** Forget samples of the last committed word, e.g. because the commit was reverted. */
    @JvmStatic
    @Synchronized
    fun discardPendingSamples() {
        pending = emptyList()
    }

    private fun addPendingSamples(prefs: SharedPreferences) {
        if (pending.isEmpty()) return
        pending.forEach { sample ->
            val s = stats.getOrPut(key(sample.orientation, sample.codePoint)) { KeyStats(0f, 0f, 0) }
            s.count = min(s.count + 1, MAX_SAMPLES)
            s.dx += (sample.dx - s.dx) / s.count
            s.dy += (sample.dy - s.dy) / s.count
        }
        unsavedSamples += pending.size
        pending = emptyList()
        updateCachedOffsets()
        if (unsavedSamples >= SAVE_INTERVAL)
            save(prefs)
    }

    private fun updateCachedOffsets() {
        val offsets = HashMap<Int, HashMap<Int, FloatArray>>()
        stats.forEach { (key, s) ->
            if (s.count < MIN_SAMPLES) return@forEach
            val clampedDx = max(-MAX_OFFSET, min(MAX_OFFSET, s.dx))
            val clampedDy = max(-MAX_OFFSET, min(MAX_OFFSET, s.dy))
            offsets.getOrPut((key ushr 32).toInt()) { HashMap() }[key.toInt()] = floatArrayOf(clampedDx, clampedDy)
        }
        cachedOffsets = offsets
    }

    private fun key(orientation: Int, codePoint: Int) = (orientation.toLong() shl 32) or codePoint.toLong()

    @VisibleForTesting
    internal fun sampleCount(orientation: Int, codePoint: Int) = stats[key(orientation, codePoint)]?.count ?: 0
}
