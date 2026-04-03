package ups.logic.robbyapp

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Intent
import android.icu.text.ListFormatter.Width
import android.media.Image
import android.os.Bundle
//import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ups.logic.robbyapp.configuraciones.TabsAdapter
import ups.logic.robbyapp.databinding.ActivityJuego4Binding
import ups.logic.robbyapp.databinding.ActivityJuego4MainBinding
import android.view.View
import android.view.MotionEvent

class MainJuego4 : AppCompatActivity() {

    private var binding: ActivityJuego4MainBinding? = null

    private val fragmento
        get() = binding!!
    private lateinit var numeros: ImageButton
    private lateinit var letras: ImageButton
    private lateinit var vocales: ImageButton
    private lateinit var back: ImageButton
    private lateinit var learning: ImageButton
    private lateinit var silabas: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJuego4MainBinding.inflate(layoutInflater)
        setContentView(fragmento.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()


        numeros = findViewById(R.id.numerosbtn)
        letras = findViewById(R.id.letrasbtn)
        silabas = findViewById(R.id.silabas)
        vocales = findViewById(R.id.vocalesbtn)
        back = findViewById(R.id.volver)
        learning = findViewById(R.id.learn)


        efectoBoton(numeros)
        efectoBoton(letras)
        efectoBoton(vocales)
        efectoBoton(silabas)
        efectoBoton(learning)
        efectoBoton(back)

        numeros.setImageResource(R.mipmap.numerosicono)
        vocales.setImageResource(R.mipmap.iconovocales)
        letras.setImageResource(R.mipmap.iconoletras_foreground)
        silabas.setImageResource(R.mipmap.iconosilabas_foreground)

        back.setOnClickListener {
            finish()
        }

        numeros.setOnClickListener {
            Memoria.esc = 1
            var intento: Intent = Intent(this, Juego4::class.java)
            startActivity(intento)
        }

        silabas.setOnClickListener {
            Memoria.silbOLett = 1
            var intento = Intent(this, ActivitySyllables::class.java)
            startActivity(intento)
        }

        letras.setOnClickListener {
            Memoria.silbOLett = 2
            var intento = Intent(this, ActivitySyllables::class.java)
            startActivity(intento)
        }

        vocales.setOnClickListener {
            Memoria.esc = 3
            var intento: Intent = Intent(this, Juego4::class.java)
            startActivity(intento)
        }


//        val scaleX = AnimatorInflater.loadAnimator(this, R.animator.scale_up)
//        scaleX.setTarget(learning)
//
//        val scaleY = AnimatorInflater.loadAnimator(this, R.animator.scaley)
//        scaleY.setTarget(learning)


/* 
        learning.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }

        vocales.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }

        numeros.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }

        letras.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }

        back.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }

        silabas.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                }
            }
            true
        }
            */

        learning.setOnClickListener {
//            scaleX.start()
//            scaleY.start()

            var intento: Intent = Intent(this, LearningPage::class.java)
            startActivity(intento)
        }


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


