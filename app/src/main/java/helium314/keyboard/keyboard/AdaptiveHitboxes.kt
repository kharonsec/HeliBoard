// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import androidx.annotation.VisibleForTesting
import helium314.keyboard.latin.SuggestedWords
import helium314.keyboard.latin.SuggestedWords.SuggestedWordInfo
import helium314.keyboard.latin.settings.Settings
import kotlin.math.ln
import kotlin.math.max

/**
 * Adaptive ("invisible") hit boxes: letters that are likely to be typed next get a slightly larger
 * touch area, while the visible keys stay unchanged.
 *
 * Likelihoods are taken from the current suggestions (or next word predictions when no word is being
 * composed). They are valid for a single key press only: they are taken by the next letter key down
 * event, and cleared when any text input happens.
 */
object AdaptiveHitboxes {
    // probability used for letters that don't appear in any suggestion
    private const val BASE_PROBABILITY = 0.02f
    // weight for "the user types something that is not in the suggestions", so a single suggestion can't dominate
    private const val PRIOR_WEIGHT = 1f
    // how much a log-probability difference can shift the key boundary, in (squared) key sizes
    private const val BIAS_STRENGTH = 0.12f
    // maximum distance a key can grow into a neighboring key, as fraction of key width / height
    private const val MAX_GROWTH = 0.25f
    private val BASE_LOG_PROBABILITY = ln(BASE_PROBABILITY)

    @Volatile
    private var current: HitKeyBias? = null

    /** Compute next letter likelihoods from [suggestedWords], if they belong to [currentTypedWord]. */
    @JvmStatic
    fun onSuggestionsUpdated(currentTypedWord: String, suggestedWords: SuggestedWords) {
        current = null
        if (suggestedWords.isEmpty || suggestedWords.isPunctuationSuggestions) return
        if (suggestedWords.mInputStyle == SuggestedWords.INPUT_STYLE_UPDATE_BATCH
            || suggestedWords.mInputStyle == SuggestedWords.INPUT_STYLE_TAIL_BATCH) return
        // suggestions are computed asynchronously, make sure they are not outdated
        val suggestionsTypedWord = if (suggestedWords.isPrediction) "" else suggestedWords.mTypedWordInfo?.mWord ?: return
        if (suggestionsTypedWord != currentTypedWord) return

        val typed = lowercase(currentTypedWord)
        val weights = HashMap<Int, Float>()
        var totalWeight = PRIOR_WEIGHT
        var rank = 0
        for (i in 0 until suggestedWords.size()) {
            val info = suggestedWords.getInfo(i)
            if (info.isKindOf(SuggestedWordInfo.KIND_TYPED)) continue
            val word = lowercase(info.mWord)
            if (word.length <= typed.length || !word.startsWith(typed)) continue
            // suggestions are sorted, so rank is a robust measure (scores from different dictionaries are not comparable)
            val weight = 1f / ++rank
            totalWeight += weight
            val codePoint = word.codePointAt(typed.length)
            if (Character.isLetter(codePoint))
                weights[codePoint] = (weights[codePoint] ?: 0f) + weight
        }
        if (weights.isEmpty()) return
        current = HitKeyBias(weights.mapValues { ln(max(it.value / totalWeight, BASE_PROBABILITY)) })
    }

    @JvmStatic
    fun clear() {
        current = null
    }

    /** The bias for the next key press, or null if disabled or no predictions are available. */
    @JvmStatic
    fun peek(): HitKeyBias? = if (Settings.getValues().mAdaptiveHitboxes) current else null

    @VisibleForTesting
    internal fun currentBias() = current

    @JvmStatic
    fun isLetterKey(key: Key) = key.code > 0 && Character.isLetter(key.code)

    private fun lowercase(word: String) = buildString {
        word.codePoints().forEach { appendCodePoint(Character.toLowerCase(it)) }
    }

    class HitKeyBias(private val logProbabilities: Map<Int, Float>) {
        private fun logProbability(key: Key) =
            logProbabilities[Character.toLowerCase(key.code)] ?: BASE_LOG_PROBABILITY

        /**
         * Returns the key to use for a touch at [x], [y], given the geometrically detected [primaryKey].
         * Only a letter key that is more likely than [primaryKey] can take over the touch, and only
         * if the touch is close to its hit box.
         */
        fun adjust(primaryKey: Key, x: Int, y: Int, candidates: List<Key>): Key {
            if (!isLetterKey(primaryKey)) return primaryKey
            val width = primaryKey.width.toFloat()
            val height = primaryKey.height.toFloat()
            if (width <= 0 || height <= 0) return primaryKey

            fun squaredDistanceToCenter(key: Key): Float {
                val dx = (x - (key.x + key.width / 2f)) / width
                val dy = (y - (key.y + key.height / 2f)) / height
                return dx * dx + dy * dy
            }
            fun isWithinGrowth(key: Key): Boolean {
                val hitBox = key.hitBox
                val dx = max(0, max(hitBox.left - x, x - hitBox.right)) / width
                val dy = max(0, max(hitBox.top - y, y - hitBox.bottom)) / height
                return dx * dx + dy * dy <= MAX_GROWTH * MAX_GROWTH
            }

            val primaryLogProbability = logProbability(primaryKey)
            var bestKey = primaryKey
            var bestScore = BIAS_STRENGTH * primaryLogProbability - squaredDistanceToCenter(primaryKey)
            for (key in candidates) {
                if (key === primaryKey || !isLetterKey(key)) continue
                val logProbability = logProbability(key)
                if (logProbability <= primaryLogProbability || !isWithinGrowth(key)) continue
                val score = BIAS_STRENGTH * logProbability - squaredDistanceToCenter(key)
                if (score > bestScore) {
                    bestScore = score
                    bestKey = key
                }
            }
            return bestKey
        }
    }
}
