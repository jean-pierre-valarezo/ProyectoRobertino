package ups.logic.robbyapp

import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ups.logic.robbyapp.configuraciones.TabsAdapter
import ups.logic.robbyapp.databinding.ActivityJuego4MainBinding
import ups.logic.robbyapp.databinding.ActivityLearningPageBinding

class LearningPage : AppCompatActivity() {


    private var binding: ActivityLearningPageBinding? = null

    private val fragmento
        get() = binding!!

    private lateinit var volver: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLearningPageBinding.inflate(layoutInflater)
        setContentView(fragmento.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        val tabLayout = findViewById<TabLayout>(R.id.barra)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager2)

        val adapter = TabsAdapter(this)
        viewPager.adapter = adapter

        val letras = listOf(
            "B", "C", "D", "F", "G", "H", "J", "K", "L", "M",
            "N", "Ñ", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z"
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = letras[position]
        }.attach()




        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        volver = findViewById(R.id.volver)

        volver.setOnClickListener {
            finish()
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