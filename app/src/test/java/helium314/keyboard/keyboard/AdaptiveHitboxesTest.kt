// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import helium314.keyboard.latin.SuggestedWords
import helium314.keyboard.latin.SuggestedWords.SuggestedWordInfo
import helium314.keyboard.latin.dictionary.Dictionary
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class AdaptiveHitboxesTest {
    // a single row "a s d", keys are 100 px wide and 150 px high
    private val keyA = letterKey('a', 0)
    private val keyS = letterKey('s', 100)
    private val keyD = letterKey('d', 200)
    private val keys = listOf(keyA, keyS, keyD)

    @AfterTest
    fun tearDown() = AdaptiveHitboxes.clear()

    @Test fun likelyLetterTakesTouchesNearItsEdge() {
        val bias = biasFor("wa", "was", "wash", "wasn't")
        assertSame(keyS, bias.adjust(keyA, 95, 75, keys))
        assertSame(keyS, bias.adjust(keyA, 85, 75, keys))
    }

    @Test fun touchCloseToCenterIsNotChanged() {
        val bias = biasFor("wa", "was", "wash", "wasn't")
        assertSame(keyA, bias.adjust(keyA, 50, 75, keys))
    }

    @Test fun growthIsLimited() {
        val bias = biasFor("wa", "was", "wash", "wasn't")
        // 30% of a key width away from the hitbox of s
        assertSame(keyA, bias.adjust(keyA, 70, 75, keys))
    }

    @Test fun lessLikelyLetterDoesNotTakeTouches() {
        val bias = biasFor("wa", "was", "wash", "wasn't")
        // s is more likely than d, so d must not take touches from s
        assertSame(keyS, bias.adjust(keyS, 195, 75, keys))
        assertSame(keyS, bias.adjust(keyS, 105, 75, keys))
    }

    @Test fun nextWordPredictionsBiasFirstLetter() {
        AdaptiveHitboxes.onSuggestionsUpdated("", suggestions("", listOf("day", "do", "does"), SuggestedWords.INPUT_STYLE_PREDICTION))
        val bias = assertNotNull(AdaptiveHitboxes.currentBias())
        assertSame(keyD, bias.adjust(keyS, 190, 75, keys))
    }

    @Test fun outdatedSuggestionsAreIgnored() {
        AdaptiveHitboxes.onSuggestionsUpdated("was", suggestions("wa", listOf("was", "wash")))
        assertNull(AdaptiveHitboxes.currentBias())
    }

    @Test fun suggestionsWithoutMatchingPrefixAreIgnored() {
        AdaptiveHitboxes.onSuggestionsUpdated("xq", suggestions("xq", listOf("as", "so")))
        assertNull(AdaptiveHitboxes.currentBias())
    }

    @Test fun capitalizationIsIgnored() {
        val bias = biasFor("Wa", "was", "Wash")
        assertSame(keyS, bias.adjust(letterKey('A', 0), 95, 75, keys))
    }

    private fun biasFor(typedWord: String, vararg words: String): AdaptiveHitboxes.HitKeyBias {
        AdaptiveHitboxes.onSuggestionsUpdated(typedWord, suggestions(typedWord, words.toList()))
        return assertNotNull(AdaptiveHitboxes.currentBias())
    }

    private fun suggestions(typedWord: String, words: List<String>, inputStyle: Int = SuggestedWords.INPUT_STYLE_TYPING): SuggestedWords {
        val typedWordInfo = info(typedWord, SuggestedWordInfo.KIND_TYPED)
        val infos = ArrayList<SuggestedWordInfo>()
        if (inputStyle != SuggestedWords.INPUT_STYLE_PREDICTION)
            infos.add(typedWordInfo)
        words.forEach { infos.add(info(it, SuggestedWordInfo.KIND_CORRECTION)) }
        return SuggestedWords(infos, null, typedWordInfo, false, false, false, inputStyle, SuggestedWords.NOT_A_SEQUENCE_NUMBER)
    }

    private fun info(word: String, kind: Int) = SuggestedWordInfo(word, "", 100, kind, Dictionary.DICTIONARY_USER_TYPED,
        SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)

    private fun letterKey(letter: Char, x: Int) =
        Key(letter.toString(), null, letter.code, null, null, 0, Key.BACKGROUND_TYPE_NORMAL, x, 0, 100, 150, 0, 0)
}
