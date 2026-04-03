package ups.logic.robbyapp

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBindings
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ups.logic.robbyapp.configuraciones.ApiService
import ups.logic.robbyapp.databinding.ActivityJuego3Binding
import ups.logic.robbyapp.databinding.ActivityJuego4Binding
import java.util.TreeMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import android.widget.ProgressBar
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

public class Juego4 : AppCompatActivity() {

    private var cameraProvider: ProcessCameraProvider? = null

    private var binding: ActivityJuego4Binding? = null

    private lateinit var loading: ProgressBar

    private lateinit var repetirAudio: ImageButton

    private lateinit var mensajeResultado: TextView

    private val fragmento
        get() = binding!!


    private val processingThread = HandlerThread("ProcessingThread").apply { start() }
    private val processingHandler = android.os.Handler(processingThread.looper)

    val ok = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(Memoria.url) //Siempre debe terminar en / la URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(ok)
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    private lateinit var volver: ImageButton
    public lateinit var palabra: TextView

    private lateinit var media: MediaPlayer

    private lateinit var animacion: LottieAnimationView
    private lateinit var fireworks: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJuego4Binding.inflate(layoutInflater)
        setContentView(fragmento.root)
        fireworks = findViewById(R.id.fireworks)
        loading = findViewById(R.id.loading)
        repetirAudio = findViewById(R.id.repetirAudio)
        mensajeResultado = findViewById(R.id.mensajeResultado)


        repetirAudio.setOnClickListener {
    if (Memoria.audioJuego4 != 0) {
        val media = MediaPlayer.create(this, Memoria.audioJuego4)
        media.start()
        }
    }

       

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()
        animacion = findViewById(R.id.cara)
        animacion.playAnimation()
        animacion.loop(true)


        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val saludos = listOf(R.raw.saludojuego444)
        media = MediaPlayer.create(this, saludos.random())
        media.start()

        palabra = findViewById(R.id.palabra)

        cargar()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                val cara = findViewById<LottieAnimationView>(R.id.cara)
                val dibujo = findViewById<AreaDibujo>(R.id.dibujo)
                var palabra = findViewById<TextView>(R.id.palabra)

                val parent = cara.parent as ConstraintLayout
                val screenWidth = parent.width // Get total screen width

                val startWidthCara = cara.width
                val endWidthCara = screenWidth / 2

                val startWidthDibujo = dibujo.width
                val endWidthDibujo = screenWidth / 2

                val startWidthPalabra = palabra.width
                val endWidthPalabra = screenWidth / 2

                // Animate width change
                val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 1000 // 1 second animation
                    addUpdateListener { animation ->
                        val progress = animation.animatedValue as Float

                        // Update cara width
                        val paramsCara = cara.layoutParams as ConstraintLayout.LayoutParams
                        paramsCara.width = (startWidthCara + (endWidthCara - startWidthCara) * progress).toInt()
                        cara.layoutParams = paramsCara

                        // Update dibujo width
                        val paramsDibujo = dibujo.layoutParams as ConstraintLayout.LayoutParams
                        paramsDibujo.width = (startWidthDibujo + (endWidthDibujo - startWidthDibujo) * progress).toInt()
                        dibujo.layoutParams = paramsDibujo

                        val paramsPalabra = palabra.layoutParams as FrameLayout.LayoutParams
                        paramsPalabra.width = (startWidthPalabra + (endWidthPalabra - startWidthPalabra) * progress).toInt()
                        palabra.layoutParams = paramsPalabra
                    }
                }
                animator.start()




            },
            5000
        )


        hideSystemUI()
        volver = findViewById(R.id.volver)
        volver.setOnClickListener {
            finish()
        }
        AreaDibujo.actividadJuego4 = this


    }

     fun mostrarLoading() {
        runOnUiThread {
            loading.visibility = View.VISIBLE
        }
    }

        fun ocultarLoading() {
        runOnUiThread {
            loading.visibility = View.GONE
        }
    }




   fun mostrarFuegos() {
    runOnUiThread {
        fireworks.cancelAnimation()
        fireworks.visibility = View.VISIBLE
        fireworks.alpha = 1f
        fireworks.progress = 0f
        fireworks.repeatCount = 0
        fireworks.bringToFront()
        fireworks.translationZ = 100f
        fireworks.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            fireworks.cancelAnimation()
            fireworks.visibility = View.GONE
        }, 1500)
    }
}

fun mostrarCorrecto() {
    runOnUiThread {
        mensajeResultado.text = "¡Muy bien!"
        mensajeResultado.setTextColor(android.graphics.Color.parseColor("#00C853"))
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}

fun mostrarIncorrecto() {
    runOnUiThread {
        mensajeResultado.text = "Intenta otra vez"
        mensajeResultado.setTextColor(android.graphics.Color.RED)
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}


    fun vibrarError() {
    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}



    public fun actualizarTexto(nuevoTexto: String) {
        palabra.text = nuevoTexto
    }

    fun cargar(){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                var solicitud = apiService.words()
                val response=  solicitud.execute()
                if(response.isSuccessful){
                    val gson = Gson()

                    // Map<String, Map<String, String>> para que lea el nivel anidado
                    val type = object : TypeToken<Map<String, Map<String, String>>>() {}.type
                    val parsedMap: Map<String, Map<String, String>> = gson.fromJson(response.body()?.string(), type)
                    Memoria.palabras = TreeMap(parsedMap["diccionario"])

                    println(Memoria.palabras)
                }

            }catch (e :Exception){
                println(e.message)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Memoria.inicioJuego4 = false
        media.stop()
    }



    @RequiresApi(value = 24)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
class Helper(private val textView: TextView) {

    fun actualizarTexto(nuevoTexto: String) {
        textView.text = nuevoTexto
    }
}
