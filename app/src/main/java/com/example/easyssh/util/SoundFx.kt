package com.example.easyssh.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.example.easyssh.R

/**
 * Efekty dźwiękowe aplikacji — realizacja wymogu "Audio".
 * Dźwięki odtwarzane są z prawdziwych plików w `res/raw` przez [SoundPool] (małe opóźnienie,
 * idealne do krótkich SFX):
 *
 *  - [playError] : dźwięk błędu (np. nieudane połączenie SSH / timeout) — `error_attention.mp3`.
 *  - [playCopy]  : dźwięk potwierdzenia (skopiowano do schowka) — `success_chime.mp3`.
 *
 * [init] należy wywołać raz przy starcie aplikacji (EasySshApplication). Gdyby SoundPool nie
 * zdążył jeszcze załadować próbki (krótkie okno tuż po starcie) lub init nie został wywołany,
 * stosujemy fallback na sprzętowy [ToneGenerator], by dźwięk zawsze był słyszalny.
 */
object SoundFx {

    @Volatile private var pool: SoundPool? = null
    private var copyId = 0
    private var errorId = 0

    // ID próbek, które zakończyły ładowanie — tylko takie można odtworzyć przez SoundPool.
    private val loaded = HashSet<Int>()

    // Fallback na wypadek niegotowego SoundPoola.
    @Volatile private var errorTone: ToneGenerator? = null
    @Volatile private var copyTone: ToneGenerator? = null

    /** Inicjalizacja odtwarzacza i załadowanie próbek z `res/raw`. Idempotentne. */
    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
        sp.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(loaded) { loaded.add(sampleId) }
        }
        val appCtx = context.applicationContext
        copyId = sp.load(appCtx, R.raw.success_chime, 1)
        errorId = sp.load(appCtx, R.raw.error_attention, 1)
        pool = sp
    }

    /** Dźwięk błędu (np. nieudane połączenie SSH / timeout). */
    fun playError() {
        if (!playSample(errorId, 1f)) {
            // Fallback: terminalowy beep błędu
            runCatching {
                val tg = errorTone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 90).also { errorTone = it }
                tg.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
            }
        }
    }

    /** Dźwięk potwierdzenia — skopiowano do schowka. */
    fun playCopy() {
        if (!playSample(copyId, 0.85f)) {
            // Fallback: krótki ton potwierdzenia
            runCatching {
                val tg = copyTone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 80).also { copyTone = it }
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
            }
        }
    }

    /** Próbuje odtworzyć próbkę przez SoundPool. Zwraca false, jeśli nie jest jeszcze gotowa. */
    private fun playSample(sampleId: Int, volume: Float): Boolean {
        val sp = pool ?: return false
        val ready = synchronized(loaded) { loaded.contains(sampleId) }
        if (!ready) return false
        return sp.play(sampleId, volume, volume, 1, 0, 1f) != 0
    }
}
