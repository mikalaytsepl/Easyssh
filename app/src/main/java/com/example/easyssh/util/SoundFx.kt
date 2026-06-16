package com.example.easyssh.util

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Syntezowane efekty dźwiękowe — realizacja wymogu "Audio" z opisu projektu BEZ plików w zasobach.
 * Dźwięki generuje [ToneGenerator] sprzętowo, więc nie potrzebujemy żadnego .mp3/.wav.
 *
 *  - [playError] : klasyczny terminalowy "beep" przy błędzie połączenia (np. Timeout).
 *  - [playCopy]  : cichy dźwięk sukcesu przy skopiowaniu komendy / klucza do schowka.
 *
 * Głośność ustawiana jest przy tworzeniu generatora, dlatego trzymamy dwa osobne:
 * głośniejszy dla błędu i cichszy dla potwierdzenia kopiowania.
 */
object SoundFx {

    @Volatile private var errorTone: ToneGenerator? = null
    @Volatile private var copyTone: ToneGenerator? = null

    /** Terminalowy beep błędu (np. nieudane połączenie SSH / timeout). */
    fun playError() {
        runCatching {
            val tg = errorTone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 90).also { errorTone = it }
            tg.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
        }
    }

    /** Cichy ton potwierdzenia — skopiowano do schowka. */
    fun playCopy() {
        runCatching {
            val tg = copyTone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 35).also { copyTone = it }
            tg.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        }
    }
}
