package ups.logic.robbyapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ups.logic.robbyapp.Memoria.inicializarUrl
import ups.logic.robbyapp.configuraciones.Config
import ups.logic.robbyapp.configuraciones.SecureStorage.getSecurePrefs
import ups.logic.robbyapp.databinding.ActivityMainBinding
import android.view.View
import android.view.MotionEvent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Matrix
import android.widget.TextView
import com.airbnb.lottie.LottieDrawable
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.TypefaceSpan
import androidx.core.content.res.ResourcesCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var valor: LottieAnimationView
    private lateinit var media: MediaPlayer
    private lateinit var musica: MediaPlayer

    private lateinit var juego1: ImageButton
    private lateinit var juego2: ImageButton
    private lateinit var juego3: ImageButton
    private lateinit var juego4: ImageButton
    private lateinit var juego5: ImageButton
    private lateinit var configurar: ImageButton

    private lateinit var contexto: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //decorView.systemUiVisibility = uiOptions
        inicializarUrl(this)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE),
            0
        )

        //binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        //val titulo = findViewById<TextView>(R.id.tituloApp)
        //aplicarFuenteMixta(titulo)
        //animarTitulo(titulo)

        val tituloParte1 = findViewById<TextView>(R.id.tituloParte1)
        val tituloEnie = findViewById<TextView>(R.id.tituloEnie)
        val tituloParte2 = findViewById<TextView>(R.id.tituloParte2)

        animarTitulo(tituloParte1)
        animarTitulo(tituloEnie)
        animarTitulo(tituloParte2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Example of a call to a native method
        //binding.sampleText.text = stringFromJNI()


        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        valor = findViewById(R.id.animacion)

        valor.repeatCount = LottieDrawable.INFINITE
        LottieDrawable.INFINITE
        valor.repeatMode = LottieDrawable.RESTART
        valor.playAnimation()
        valor.speed = 1.30f
        //valor.loop(true)


        val lista = listOf(R.raw.saludoinicialuno, R.raw.saludoinicialdos)

        val saludo = lista.random()
        //musica = MediaPlayer.create(this,saludo)


        //musica.start()



        juego1 = findViewById(R.id.juego1)

        juego2 = findViewById(R.id.juego2)

        juego3 = findViewById(R.id.juego3)

        juego4 = findViewById(R.id.juego4)


        //efectoBoton(juego1)
        //efectoBoton(juego2)
        //efectoBoton(juego3)
        //efectoBoton(juego4)
        //efectoBoton(configurar)

        

        //juego5 = findViewById(R.id.juego5)

        juego1.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                v.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .start()
            }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
            }
        }
        false
        }
        juego2.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }
            false
        }
        juego3.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }
            false
        }
        juego4.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }
            false
        }

        
        


        configurar = findViewById<ImageButton>(R.id.config)


        configurar.setOnTouchListener { v, event ->
            when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            v.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .start()
            }
        android.view.MotionEvent.ACTION_UP,
        android.view.MotionEvent.ACTION_CANCEL -> {
            v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
            }
        }
        false
        }

        juego1.setOnClickListener {
        if (!hayInternet()) {
            Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
            return@setOnClickListener
        }

    inicializarUrl(this)

        if (Memoria.url.isNotEmpty()) {
            val intent = Intent(this, MainJuego1::class.java)
               Memoria.numeroJuego = 1
        startActivity(intent)
        } else {
            Toast.makeText(this, "Configura la dirección de la maquina", Toast.LENGTH_SHORT).show()
        }
    }
    
        juego2.setOnClickListener {
        if (!hayInternet()) {
            Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
            return@setOnClickListener
        }

    inicializarUrl(this)

        if (Memoria.url.isNotEmpty()) {
            val intent = Intent(this, MainJuego1::class.java)
            Memoria.numeroJuego = 2
            startActivity(intent)
        } else {
            Toast.makeText(this, "Configura la dirección de la maquina", Toast.LENGTH_SHORT).show()
        }
    }

        juego3.setOnClickListener {
    if (!hayInternet()) {
        Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        return@setOnClickListener
    }

    inicializarUrl(this)

    if (Memoria.url.isNotEmpty()) {
        val intent = Intent(this, MainJuego1::class.java)
        Memoria.numeroJuego = 3
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    } else {
        Toast.makeText(this, "Configura la dirección de la maquina", Toast.LENGTH_SHORT).show()
        }
    }

       juego4.setOnClickListener {
    if (!hayInternet()) {
        Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        return@setOnClickListener
    }

    inicializarUrl(this)

    if (Memoria.url.isNotEmpty()) {
        val intent = Intent(this, MainJuego4::class.java)
        Memoria.numeroJuego = 4
        startActivity(intent)
    } else {
        Toast.makeText(this, "Configura la dirección de la maquina", Toast.LENGTH_SHORT).show()
        }
    }

//        juego5.setOnClickListener {
//            var intent: Intent = Intent(this, Juego5::class.java)
//            startActivity(intent)
//        }


        configurar.setOnClickListener {
            var intent: Intent = Intent(this, Config::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }



        hideSystemUI()
        contexto = this
        CoroutineScope(Dispatchers.IO).launch {
            try{
                media = MediaPlayer.create(contexto, R.raw.menu)
                media.setVolume(0.05f,0.05f)
                //media.isLooping = true
                //media.start()
                withContext(Dispatchers.Main){
                    //media.start()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun hayInternet(): Boolean {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val network = connectivityManager.activeNetworkInfo
    return network != null && network.isConnected
    }

    override fun onResume() {
    super.onResume()
    inicializarUrl(this)
    }

    override fun onStart() {
        super.onStart()
        //valor.loop(true)
        valor.playAnimation()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                media = MediaPlayer.create(contexto, R.raw.menu)
//                media.setVolume(0.15f, 0.15f)
//                media.isLooping = true
//                //media.start()
//                withContext(Dispatchers.Main) {
//                    media.start()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
       // valor.loop(false)
        valor.pauseAnimation()
    }

    fun isKeyStored(context: Context, key: String): Boolean {
        val prefs = getSecurePrefs(context)
        val value = prefs.getString(key, "")?.trim() ?: ""
        return value.isNotEmpty()
    }



    @RequiresApi(value = 24)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * A native method that is implemented by the 'robbyapp' native library,
     * which is packaged with this application.
     */
//    external fun stringFromJNI(): String
//
//    companion object {
//        // Used to load the 'robbyapp' library on application startup.
//        init {
//            System.loadLibrary("robbyapp")
//        }
//    }



}

fun efectoBoton(vista: View) {
    vista.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .start()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
        }
        false
    }
}

fun aplicarFuenteMixta(textView: TextView) {

    val texto = "ROBERTIÑO"
    val spannable = SpannableString(texto)

    val alphaEcho = ResourcesCompat.getFont(textView.context, R.font.alpha_echo)

    if (alphaEcho != null) {
        spannable.setSpan(
            CustomTypefaceSpan(alphaEcho),
            7,
            8,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    textView.text = spannable
}


fun animarTitulo(textView: TextView) {

    val colores = intArrayOf(
        0xFF0000FF.toInt(),
        0xFFFFFFFF.toInt(),
        0xFFFF0000.toInt(),
        0xFF000000.toInt(),
        0xFF0000FF.toInt()
    )

   val shader = LinearGradient(
        0f, 0f, 600f, 0f,
        colores,
        null,
        Shader.TileMode.MIRROR
    )

    textView.paint.shader = shader

    val matrix = Matrix()
    val handler = Handler(Looper.getMainLooper())

    var desplazamiento = 0f

    val runnable = object : Runnable {
        override fun run() {

            desplazamiento += 20f

            matrix.setTranslate(desplazamiento, 0f)

            shader.setLocalMatrix(matrix)

            textView.invalidate()

            handler.postDelayed(this, 40)
        }
    }

    handler.post(runnable)

    // efecto brillo tipo "shine"
    textView.animate()
        .alpha(1f)
        .setDuration(1000)
        .start()
}
class CustomTypefaceSpan(
    private val typeface: Typeface
) : TypefaceSpan("") {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, typeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, typeface)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0
        val fakeStyle = oldStyle and tf.style.inv()

        if (fakeStyle and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fakeStyle and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}