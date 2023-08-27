package com.thoughtworks.ivassistant.abilities.tts.google

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.AudioConfig
import com.google.cloud.texttospeech.v1.AudioEncoding
import com.google.cloud.texttospeech.v1.SsmlVoiceGender
import com.google.cloud.texttospeech.v1.SynthesisInput
import com.google.cloud.texttospeech.v1.TextToSpeechClient
import com.google.cloud.texttospeech.v1.TextToSpeechSettings
import com.google.cloud.texttospeech.v1.VoiceSelectionParams
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsCallback
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GoogleTts(
    private val context: Context,
    private val params: Map<String, Any>
) : Tts {
    private var textToSpeechClient: TextToSpeechClient? = null
    private var mediaPlayer = MediaPlayer()

    override fun initialize() {
        val credentials = GoogleCredentials.fromStream(params["credentials"] as InputStream)
        val settings =
            TextToSpeechSettings.newBuilder().setCredentialsProvider { credentials }.build()
        textToSpeechClient = TextToSpeechClient.create(settings)
    }

    private fun extractGender(): SsmlVoiceGender {
        params["gender"]?.let {
            return when (it) {
                "unspecified" -> SsmlVoiceGender.SSML_VOICE_GENDER_UNSPECIFIED
                "male" -> SsmlVoiceGender.MALE
                "female" -> SsmlVoiceGender.FEMALE
                "neutral" -> SsmlVoiceGender.NEUTRAL
                else -> SsmlVoiceGender.UNRECOGNIZED
            }
        }

        return SsmlVoiceGender.SSML_VOICE_GENDER_UNSPECIFIED
    }

    private fun createAudioData(text: String): ByteArray? {
        // Set the text input to be synthesized
        val input = SynthesisInput.newBuilder()
            .setText(text)
            .build()

        val gender = extractGender()

        // Build the voice request
        val voice = VoiceSelectionParams.newBuilder()
            .setLanguageCode(params["language_code"]?.toString() ?: "en-US")  // Language code
            .setSsmlGender(gender)  // Gender
            .setName(params["name"]?.toString() ?: "en-US-Wavenet-F")  // Specific voice model
            .build()

        // Select the type of audio file you want returned
        val audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.MP3)  // Audio format
            .setSpeakingRate(
                params["speaking_rate"]?.toString()?.toDouble() ?: 1.0
            )  // Speech speed. Default is 1.0. Range is 0.25 to 4.0.
            .setPitch(0.0)  // Speech pitch. Default is 0.0. Range is -20.0 to 20.0.
            .setVolumeGainDb(params["volume_gain_db"]?.toString()?.toDouble() ?: 0.0)
            .build()

        return try {
            // Perform the Text-to-Speech request
            val response = textToSpeechClient?.synthesizeSpeech(input, voice, audioConfig)
            // The response's audioContent is a ByteString containing the audio
            val audioContents = response?.audioContent
            audioContents?.toByteArray()
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            null
        }
    }

    private fun createAudioFile(text: String, fileName: String): File? {
        stopPlay()

        val audioData = createAudioData(text)

        return try {
            val savePath = File(context.cacheDir, DEFAULT_FILE_SAVE_DIR)
            if (!savePath.exists()) {
                savePath.mkdirs()
            }
            val saveFile = File(savePath, fileName)
            val fos = FileOutputStream(saveFile)
            fos.write(audioData)
            fos.close()

            saveFile
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            null
        }
    }

    override fun play(text: String, ttsCallback: TtsCallback?) {
        stopPlay()

        val audioData = createAudioData(text)

        try {
            val tempFile = File.createTempFile("audio", "mp3")
            tempFile.deleteOnExit()
            val fos = FileOutputStream(tempFile)
            fos.write(audioData)
            fos.close()

            ttsCallback?.onTTSFileSaved(tempFile.absolutePath)

            // play audio
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

//            val mutex = Mutex(locked = true)
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "mediaPlayer onCompletion")
//                mutex.unlock()
                ttsCallback?.onPlayEnd()
            }

            mediaPlayer.setOnErrorListener { _, _, _ ->
                Log.e(TAG, "mediaPlayer onError")
//                mutex.unlock()
                ttsCallback?.onPlayEnd()
                return@setOnErrorListener true
            }

//            mutex.lock()
            Log.d(TAG, "Tts ends")
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            Log.e(TAG, "play audio end error")
        }
    }

    override fun stopPlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun release() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()

        textToSpeechClient?.close()
        textToSpeechClient = null
    }

    companion object {
        private const val TAG = "IV.GoogleTts"
        private const val DEFAULT_FILE_SAVE_DIR = "google_tts"
    }
}