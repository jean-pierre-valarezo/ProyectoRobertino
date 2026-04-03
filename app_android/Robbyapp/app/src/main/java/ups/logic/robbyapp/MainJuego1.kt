package ups.logic.robbyapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import ups.logic.robbyapp.databinding.ActivityMainJuego1Binding
import android.view.View

class MainJuego1 : AppCompatActivity() {

    private var binding: ActivityMainJuego1Binding? = null

    private val fragmento
        get() = binding!!

    private lateinit var animacion: LottieAnimationView

    private lateinit var edad: Switch

    private lateinit var acuatico: ImageButton
    private lateinit var terrestre: ImageButton
    private lateinit var aereo: ImageButton
    private lateinit var tacuatico: ImageButton
    private lateinit var tterrestre: ImageButton
    private lateinit var taereo: ImageButton
    private lateinit var colores: ImageButton
    private lateinit var comidachatarra: ImageButton
    private lateinit var emocion: ImageButton
    private lateinit var fruta: ImageButton
    private lateinit var higiene: ImageButton
    private lateinit var instrumentomusical: ImageButton
    private lateinit var juguete: ImageButton
    private lateinit var numero: ImageButton
    private lateinit var persona: ImageButton
    private lateinit var prendadevestir: ImageButton
    private lateinit var verdura: ImageButton

    private lateinit var volver: ImageButton

    private lateinit var media: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainJuego1Binding.inflate(layoutInflater)
        setContentView(binding!!.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        acuatico = findViewById(R.id.acuatico)
        terrestre = findViewById(R.id.terrestre)
        aereo = findViewById(R.id.aereo)
        tacuatico = findViewById(R.id.tacuatico)
        tterrestre = findViewById(R.id.tterrestre)
        taereo = findViewById(R.id.taereo)
        colores = findViewById(R.id.color)
        comidachatarra = findViewById(R.id.comidachatarra)
        emocion = findViewById(R.id.emocion)
        fruta = findViewById(R.id.frutas)
        higiene = findViewById(R.id.higiene)
        instrumentomusical = findViewById(R.id.instrumentomusical)
        juguete = findViewById(R.id.juguete)
        numero = findViewById(R.id.numero)
        persona = findViewById(R.id.persona)
        prendadevestir = findViewById(R.id.prendadevestir)
        verdura = findViewById(R.id.verdura)

        edad = findViewById(R.id.edad)


        acuatico.setImageResource(R.mipmap.icono_aacuatico_foreground)
        terrestre.setImageResource(R.mipmap.icono_aterrestre_foreground)
        aereo.setImageResource(R.mipmap.icono_aaereo_foreground)
        tacuatico.setImageResource(R.mipmap.icono_tacuatico_foreground)
        tterrestre.setImageResource(R.mipmap.icono_tterrestre_foreground)
        taereo.setImageResource(R.mipmap.icono_taereo_foreground)
        colores.setImageResource(R.mipmap.icono_color_foreground)
        comidachatarra.setImageResource(R.mipmap.icono_comidachatarra_foreground)
        emocion.setImageResource(R.mipmap.icono_emocion_foreground)
        fruta.setImageResource(R.mipmap.icono_fruta_foreground)
        higiene.setImageResource(R.mipmap.icono_higiene_foreground)
        instrumentomusical.setImageResource(R.mipmap.icono_instrumentomusical_foreground)
        juguete.setImageResource(R.mipmap.icono_juguete_foreground)
        numero.setImageResource(R.mipmap.icono_numeros_foreground)
        persona.setImageResource(R.mipmap.icono_persona_foreground)
        prendadevestir.setImageResource(R.mipmap.icono_ropa_foreground)
        verdura.setImageResource(R.mipmap.icono_verdura_foreground)


        acuatico = findViewById(R.id.acuatico)
        terrestre = findViewById(R.id.terrestre)
        aereo = findViewById(R.id.aereo)
        tacuatico = findViewById(R.id.tacuatico)
        tterrestre = findViewById(R.id.tterrestre)
        taereo = findViewById(R.id.taereo)
        colores = findViewById(R.id.color)
        comidachatarra = findViewById(R.id.comidachatarra)
        emocion = findViewById(R.id.emocion)
        fruta = findViewById(R.id.frutas)
        higiene = findViewById(R.id.higiene)
        instrumentomusical = findViewById(R.id.instrumentomusical)
        juguete = findViewById(R.id.juguete)
        numero = findViewById(R.id.numero)
        persona = findViewById(R.id.persona)
        prendadevestir = findViewById(R.id.prendadevestir)
        verdura = findViewById(R.id.verdura)
        volver = findViewById(R.id.volver)

        efectoBoton(acuatico)
        efectoBoton(terrestre)
        efectoBoton(aereo)
        efectoBoton(tacuatico)
        efectoBoton(tterrestre)
        efectoBoton(taereo)
        efectoBoton(colores)
        efectoBoton(comidachatarra)
        efectoBoton(emocion)
        efectoBoton(fruta)
        efectoBoton(higiene)
        efectoBoton(instrumentomusical)
        efectoBoton(juguete)
        efectoBoton(numero)
        efectoBoton(persona)
        efectoBoton(prendadevestir)
        efectoBoton(verdura)
        efectoBoton(volver)

//        when(Memoria.numeroJuego){
//            1 -> media = MediaPlayer.create(this, R.raw.escogerbotones).apply { start() }
//            2 -> media = MediaPlayer.create(this, R.raw.saludojuego2).apply { start() }
//            3 -> media = MediaPlayer.create(this, R.raw.saludojuego3).apply { start() }
//            4 -> media = MediaPlayer.create(this, R.raw.saludojuego4).apply { start() }
//        }

        acuatico.isEnabled = false
        acuatico.alpha = 0.5f

        tacuatico.isEnabled = false
        tacuatico.alpha = 0.5f

        tterrestre.isEnabled = false
        tterrestre.alpha = 0.5f

        terrestre.isEnabled = false
        terrestre.alpha = 0.5f

        aereo.isEnabled = false
        aereo.alpha = 0.5f

        taereo.isEnabled = false
        taereo.alpha = 0.5f

        persona.isEnabled = false
        persona.alpha = 0.5f

        numero.isEnabled = false
        numero.alpha = 0.5f

        verdura.isEnabled = false
        verdura.alpha = 0.5f

        juguete.isEnabled = false
        juguete.alpha = 0.5f

        prendadevestir.isEnabled = false
        prendadevestir.alpha = 0.5f

        comidachatarra.isEnabled = false
        comidachatarra.alpha = 0.5f

        emocion.isEnabled = false
        emocion.alpha = 0.5f

        instrumentomusical.isEnabled = false
        instrumentomusical.alpha = 0.5f

        higiene.isEnabled = false
        higiene.alpha = 0.5f

        fruta.isEnabled = false
        fruta.alpha = 0.5f

        colores.isEnabled = false
        colores.alpha = 0.5f

        acuatico.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.animalesacuaticos)
            media.start()

            media.setOnCompletionListener {

                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }

                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 0

            }
        }

        aereo.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.animalesaereos).apply {
                start()
            }
            media.setOnCompletionListener {

                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 1
            }
        }

        terrestre.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.animalesterrestres)

            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 2
            }
        }


        tacuatico.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.transporteacuatico)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 3
            }
        }

        taereo.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.transporteaereo)
            media.start()

            media.setOnCompletionListener {

                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 4
            }
        }

        tterrestre.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.transporteterrestre)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 5
            }
        }


        colores.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.colores)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 6
            }
        }
        comidachatarra.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.comidachatarra)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 7
            }
        }

        emocion.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.emociones)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 8
            }
        }

        fruta.setOnClickListener {
          //  finish()
            media = MediaPlayer.create(this, R.raw.frutas)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 9
            }
        }

        higiene.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.higiene)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 10
            }
        }
        instrumentomusical.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.intrumentosmusicales)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 11
            }
        }

        juguete.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.juguetes)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 12
            }
        }
        numero.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.numeros)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 13
            }
        }

        persona.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.personas)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 14
            }
        }

        prendadevestir.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.prendasdevestir)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 15
            }
        }

        verdura.setOnClickListener {
            //finish()
            media = MediaPlayer.create(this, R.raw.verduras)
            media.start()

            media.setOnCompletionListener {
                var intento: Intent? = null
                when(Memoria.numeroJuego){
                    1 -> intento = Intent(this, Juego1::class.java)
                    2 -> intento = Intent(this, Juego2::class.java)
                    3 -> intento = Intent(this, Juego3::class.java)
                    4 -> intento = Intent(this, Juego4::class.java)
                    5 -> intento = Intent(this, Juego5::class.java)
                }
                startActivity(intento!!)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Memoria.numeroCategoria = 16
            }
        }

        volver = findViewById(R.id.volver)
        volver.setOnClickListener {
            finish()
        }

        //media.setOnCompletionListener {
            acuatico.isEnabled = true
            acuatico.alpha = 1.0f

            tacuatico.isEnabled = true
            tacuatico.alpha = 1.0f

            tterrestre.isEnabled = true
            tterrestre.alpha = 1.0f

            terrestre.isEnabled = true
            terrestre.alpha = 1.0f

            aereo.isEnabled = true
            aereo.alpha = 1.0f

            taereo.isEnabled = true
            taereo.alpha = 1.0f

            persona.isEnabled = true
            persona.alpha = 1.0f

            //numero.isEnabled = true
            //numero.alpha = 1.0f

            verdura.isEnabled = true
            verdura.alpha = 1.0f

            juguete.isEnabled = true
            juguete.alpha = 1.0f

            prendadevestir.isEnabled = true
            prendadevestir.alpha = 1.0f

            comidachatarra.isEnabled = true
            comidachatarra.alpha = 1.0f

            emocion.isEnabled = true
            emocion.alpha = 1.0f

            instrumentomusical.isEnabled = true
            instrumentomusical.alpha = 1.0f

            higiene.isEnabled = true
            higiene.alpha = 1.0f

            fruta.isEnabled = true
            fruta.alpha = 1.0f

            colores.isEnabled = true
            colores.alpha = 1.0f
      //  }

        var texto = findViewById<TextView>(R.id.textoedad)

        if (Memoria.numeroJuego == 2  || Memoria.numeroJuego == 3 || Memoria.numeroJuego ==4){
            edad.isVisible = false
            texto.isVisible = false
        }

        edad.setOnCheckedChangeListener { _, isChecked ->
            Memoria.edad = isChecked
            val edadNumerica = if (Memoria.edad) 6 else 5
            texto.text = "Edad: $edadNumerica"
        }

        hideSystemUI()
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


}



