package com.argon.agent
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class ArgonService : Service(), TextToSpeech.OnInitListener {
    private lateinit var sr: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        val channel = NotificationChannel("argon", "Argon Background Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notif = Notification.Builder(this, "argon").setContentTitle("Argon AI Active").setContentText("Listening for 'Argon'...").setSmallIcon(android.R.drawable.ic_btn_speak_now).build()
        startForeground(1, notif)
        startListening()
    }

    private fun startListening() {
        Handler(Looper.getMainLooper()).post {
            sr = SpeechRecognizer.createSpeechRecognizer(this)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)?.lowercase() ?: ""
                    if (text.contains("argon")) handleCommand(text.replace("argon", "").trim())
                    restartListening()
                }
                override fun onError(error: Int) { restartListening() }
                override fun onReadyForSpeech(p0: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(p0: Float) {}
                override fun onBufferReceived(p0: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(p0: Bundle?) {}
                override fun onEvent(p0: Int, p1: Bundle?) {}
            })
            sr.startListening(intent)
        }
    }

    private fun restartListening() {
        Handler(Looper.getMainLooper()).postDelayed({ sr.destroy(); startListening() }, 1000)
    }

    private fun handleCommand(cmd: String) {
        when {
            cmd.contains("youtube") || cmd.contains("song") || cmd.contains("play") -> {
                val query = cmd.replace("play", "").replace("youtube", "").replace("song", "").trim()
                speak("Playing $query on YouTube")
                val i = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(i)
            }
            cmd.contains("search") || cmd.contains("google") -> {
                val query = cmd.replace("search", "").replace("google", "").trim()
                speak("Searching Google for $query")
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(i)
            }
            cmd.contains("email") || cmd.contains("mail") -> {
                speak("Opening email composer")
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Message from Argon AI")
                    putExtra(Intent.EXTRA_TEXT, cmd)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(i)
            }
            else -> speak("You said $cmd")
        }
    }

    private fun speak(text: String) { tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1") }
    override fun onInit(status: Int) { tts.language = Locale.US }
    override fun onBind(intent: Intent?): IBinder? = null
}
