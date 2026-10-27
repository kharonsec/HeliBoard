// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class TouchModelTest {
    private val prefs = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("touch_model_test", Context.MODE_PRIVATE)
    // a single row "a s d", keys are 100 px wide and 150 px high
    private val keyA = letterKey('a', 0)
    private val keyS = letterKey('s', 100)
    private val keyD = letterKey('d', 200)
    private val keys = mapOf('a'.code to keyA, 's'.code to keyS, 'd'.code to keyD)

    @AfterTest
    fun tearDown() = TouchModel.reset(prefs)

    @Test fun offsetsAreLearnedAfterEnoughSamples() {
        // both taps 20 px right of the key center
        repeat(20) { commit("as", listOf(70, 170)) }
        flush()
        val offsets = assertNotNull(TouchModel.offsets(0))
        assertEquals(0.2f, offsets.getValue('a'.code)[0], 0.001f)
        assertEquals(0f, offsets.getValue('a'.code)[1], 0.001f)
        assertEquals(0.2f, offsets.getValue('s'.code)[0], 0.001f)
        assertNull(TouchModel.offsets(1)) // other orientation
    }

    @Test fun fewSamplesAreNotUsed() {
        repeat(5) { commit("as", listOf(70, 170)) }
        flush()
        assertEquals(5, TouchModel.sampleCount(0, 'a'.code))
        assertNull(TouchModel.offsets(0))
    }

    @Test fun revertedWordIsNotLearned() {
        commit("as", listOf(70, 170))
        TouchModel.discardPendingSamples()
        flush()
        assertEquals(0, TouchModel.sampleCount(0, 'a'.code))
    }

    @Test fun correctedTapCountsForIntendedKey() {
        // typed "ss", but the first tap was meant to be "a"
        commit("as", listOf(105, 150), typedWord = "ss")
        flush()
        assertEquals(1, TouchModel.sampleCount(0, 'a'.code))
        assertEquals(1, TouchModel.sampleCount(0, 's'.code))
    }

    @Test fun outliersAreIgnored() {
        commit("as", listOf(150, 150))
        flush()
        assertEquals(0, TouchModel.sampleCount(0, 'a'.code))
        assertEquals(1, TouchModel.sampleCount(0, 's'.code))
    }

    @Test fun wordsWithDifferentLengthAreIgnored() {
        commit("ass", listOf(50, 150), typedWord = "as")
        flush()
        assertEquals(0, TouchModel.sampleCount(0, 'a'.code))
    }

    @Test fun learnedOffsetsShiftDetection() {
        val candidates = listOf(keyA, keyS, keyD)
        // without learned offsets and predictions, the touch stays on the touched key
        assertSame(keyA, AdaptiveHitboxes.HitKeyBias(emptyMap()).adjust(keyA, 90, 75, candidates))
        // user tends to hit s 30 px left of its center
        val bias = AdaptiveHitboxes.HitKeyBias(emptyMap(), mapOf('s'.code to floatArrayOf(-0.3f, 0f)))
        assertSame(keyS, bias.adjust(keyA, 90, 75, candidates))
        assertSame(keyA, bias.adjust(keyA, 60, 75, candidates))
    }

    private fun commit(word: String, xs: List<Int>, typedWord: String = word) =
        TouchModel.onWordCommitted(word, typedWord, xs.toIntArray(), IntArray(xs.size) { 75 }, xs.size, 0, prefs) { keys[it] }

    // samples are only added when the next word is committed
    private fun flush() = commit("", emptyList())

    private fun letterKey(letter: Char, x: Int) =
        Key(letter.toString(), null, letter.code, null, null, 0, Key.BACKGROUND_TYPE_NORMAL, x, 0, 100, 150, 0, 0)
}
