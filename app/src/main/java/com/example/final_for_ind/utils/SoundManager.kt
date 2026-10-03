package com.example.final_for_ind.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

import com.example.final_for_ind.R


object SoundManager {

    private var soundPool: SoundPool? = null

    private var initialized = false

    private var diceRollId = 0
    private var tokenMoveId = 0
    private var tokenKickId = 0
    private var winId = 0
    private var loseId = 0
    private var buttonClickId = 0
    private var spinTickId = 0
    private var coinRewardId = 0

    private var appContext: Context? = null


    // =========================================================
    // INIT
    // =========================================================

    fun init(context: Context) {

        if (initialized) return

        appContext = context.applicationContext

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        val ctx = appContext ?: return

        try {

            diceRollId = soundPool?.load(ctx, R.raw.dice_roll, 1) ?: 0
            tokenMoveId = soundPool?.load(ctx, R.raw.token_move, 1) ?: 0
            tokenKickId = soundPool?.load(ctx, R.raw.token_kick, 1) ?: 0
            winId = soundPool?.load(ctx, R.raw.win, 1) ?: 0
            loseId = soundPool?.load(ctx, R.raw.lose, 1) ?: 0
            buttonClickId = soundPool?.load(ctx, R.raw.button_click, 1) ?: 0
            spinTickId = soundPool?.load(ctx, R.raw.spin_tick, 1) ?: 0
            coinRewardId = soundPool?.load(ctx, R.raw.coin_reward, 1) ?: 0

            Log.d("SOUND", "SoundManager initialized")

        } catch (e: Exception) {

            Log.e("SOUND", "Failed to load sounds", e)
        }

        initialized = true
    }


    // =========================================================
    // TOGGLE CHECK
    // =========================================================

    private fun isEnabled(): Boolean {

        val ctx = appContext ?: return false

        return ctx.getSharedPreferences(
            "ludo_session_prefs",
            Context.MODE_PRIVATE
        ).getBoolean("sound_enabled", true)
    }


    // =========================================================
    // PLAY
    // =========================================================

    private fun play(soundId: Int, volume: Float = 1f) {

        if (!initialized) return
        if (soundId == 0) return
        if (!isEnabled()) return

        try {

            soundPool?.play(
                soundId,
                volume,
                volume,
                1,
                0,
                1f
            )

        } catch (e: Exception) {

            Log.e("SOUND", "Play failed", e)
        }
    }


    // =========================================================
    // PUBLIC API
    // =========================================================

    fun playDiceRoll() = play(diceRollId, 1f)

    fun playTokenMove() = play(tokenMoveId, 0.8f)

    fun playTokenKick() = play(tokenKickId, 1f)

    fun playWin() = play(winId, 1f)

    fun playLose() = play(loseId, 0.8f)

    fun playButtonClick() = play(buttonClickId, 0.5f)

    fun playSpinTick() = play(spinTickId, 0.7f)

    fun playCoinReward() = play(coinRewardId, 1f)


    // =========================================================
    // RELEASE
    // =========================================================

    fun release() {

        try {

            soundPool?.release()
            soundPool = null
            initialized = false

        } catch (e: Exception) {

            Log.e("SOUND", "Release failed", e)
        }
    }
}