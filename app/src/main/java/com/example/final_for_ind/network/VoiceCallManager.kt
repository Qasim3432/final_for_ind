package com.example.final_for_ind.network

import android.content.Context
import android.util.Log

import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ==========================================================
// VOICE CALL STATE
// ==========================================================

sealed class VoiceState {

    object Idle : VoiceState()

    object Joining : VoiceState()

    data class Connected(
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = true,
        val remoteUserCount: Int = 0,
    ) : VoiceState()

    data class Failed(val reason: String) : VoiceState()
}


// ==========================================================
// VOICE CALL MANAGER — Agora RTC Wrapper
// ==========================================================

class VoiceCallManager(
    private val context: Context?,
    private val onStateChange: (VoiceState) -> Unit
) {

    private var rtcEngine: RtcEngine? = null

    private var currentChannel: String? = null
    private var currentUid: Int = 0

    @Volatile
    private var joined = false

    private var localMuted = false
    private var speakerOn = true
    private var joinJob: Job? = null


    private val rtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d("VOICE", "JOINED | channel=$channel | uid=$uid")
            joined = true

            // Call connect hote hi speaker force ON karein
            rtcEngine?.setEnableSpeakerphone(speakerOn)

            onStateChange(VoiceState.Connected(localMuted, speakerOn, 0))
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("VOICE", "REMOTE USER JOINED | uid=$uid")

            // Jab dusra user join ho toh wapis speaker force ON karein
            rtcEngine?.setEnableSpeakerphone(speakerOn)

            onStateChange(VoiceState.Connected(localMuted, speakerOn, 1))
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("VOICE", "REMOTE USER LEFT | uid=$uid | reason=$reason")
            onStateChange(VoiceState.Connected(localMuted, speakerOn, 0))
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            Log.d("VOICE", "LEFT CHANNEL | duration=${stats?.totalDuration}s")
            joined = false
        }

        override fun onError(err: Int) {
            Log.e("VOICE", "AGORA ERROR | code=$err")
            onStateChange(VoiceState.Failed("Agora error: $err"))
        }

        // ✅ NEW FIX: Jab Headphone lagayein ya nikalain toh state update karein
        fun onAudioRoutingChanged(routing: Int) {
            Log.d("VOICE", "AUDIO ROUTING CHANGED | routing=$routing")

            // routing values: 1 = Earpiece, 2 = Speaker, 3 = Wired Headset, 4 = Bluetooth
            val isSpeaker = (routing == 2)
            speakerOn = isSpeaker

            onStateChange(VoiceState.Connected(localMuted, speakerOn, if (joined) 1 else 0))
        }
    }

    fun initialize(appId: String) {
        if (rtcEngine != null) {
            Log.d("VOICE", "Engine already initialized")
            return
        }

        val appContext = context?.applicationContext
        if (appContext == null) {
            Log.e("VOICE", "Init failed: Context is null")
            onStateChange(VoiceState.Failed("Context is null"))
            return
        }

        try {
            val config = RtcEngineConfig().apply {
                mContext = appContext
                mAppId = appId
                mEventHandler = rtcEventHandler
            }

            rtcEngine = RtcEngine.create(config)
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine?.disableVideo()
            rtcEngine?.enableAudio()

            // ✅ Default audio route speaker pe set karein
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            rtcEngine?.setEnableSpeakerphone(true)

            rtcEngine?.enableAudioVolumeIndication(500, 3, true)

            Log.d("VOICE", "Agora engine initialized")

        } catch (e: Exception) {
            Log.e("VOICE", "Engine init failed", e)
            onStateChange(VoiceState.Failed("Init failed: ${e.message}"))
        }
    }

    fun joinChannel(sessionManager: GameSessionManager, gameId: String) {
        val engine = rtcEngine
        if (engine == null) {
            onStateChange(VoiceState.Failed("Engine not initialized"))
            return
        }

        if (joined) {
            Log.d("VOICE", "Already joined")
            return
        }

        onStateChange(VoiceState.Joining)

        joinJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val tokenJson = sessionManager.fetchAgoraToken(gameId, 0)

                if (tokenJson == null || tokenJson.optString("status") != "success") {
                    onStateChange(VoiceState.Failed(tokenJson?.optString("message") ?: "Token fetch failed"))
                    return@launch
                }

                val channel = tokenJson.optString("channel_name", "")
                val uid = tokenJson.optInt("uid", 0)
                val token = tokenJson.optString("token", "")

                if (token.isBlank() || channel.isBlank()) {
                    onStateChange(VoiceState.Failed("Invalid token response"))
                    return@launch
                }

                currentChannel = channel
                currentUid = uid

                val options = ChannelMediaOptions().apply {
                    autoSubscribeAudio = true
                    autoSubscribeVideo = false
                    publishMicrophoneTrack = true
                    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                }

                val result = engine.joinChannel(token, channel, uid, options)
                Log.d("VOICE", "joinChannel returned $result for channel=$channel uid=$uid")

                if (result != 0) {
                    onStateChange(VoiceState.Failed("Join failed: code=$result"))
                }
            } catch (e: Exception) {
                Log.e("VOICE", "joinChannel exception", e)
                onStateChange(VoiceState.Failed("Join error: ${e.message}"))
            }
        }
    }

    fun leaveChannel() {
        try {
            joinJob?.cancel()
            rtcEngine?.leaveChannel()
            joined = false
            currentChannel = null
            currentUid = 0
            localMuted = false
            speakerOn = true
            onStateChange(VoiceState.Idle)
            Log.d("VOICE", "Left channel")
        } catch (e: Exception) {
            Log.e("VOICE", "leaveChannel failed", e)
        }
    }

    fun toggleMute(): Boolean {
        localMuted = !localMuted
        rtcEngine?.muteLocalAudioStream(localMuted)
        Log.d("VOICE", "Muted=$localMuted")
        return localMuted
    }

    fun toggleSpeaker(): Boolean {
        speakerOn = !speakerOn
        rtcEngine?.setEnableSpeakerphone(speakerOn)
        Log.d("VOICE", "Speaker=$speakerOn")
        return speakerOn
    }

    fun release() {
        try {
            joinJob?.cancel()
            leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            Log.d("VOICE", "Released")
        } catch (e: Exception) {
            Log.e("VOICE", "release failed", e)
        }
    }

    fun isJoined(): Boolean = joined
}