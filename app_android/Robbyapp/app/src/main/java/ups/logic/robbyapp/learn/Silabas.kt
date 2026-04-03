package ups.logic.robbyapp.learn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ups.logic.robbyapp.R
import ups.logic.robbyapp.configuraciones.TabsAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Silabas.newInstance] factory method to
 * create an instance of this fragment.
 */
class Silabas : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var letra: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        letra = arguments?.getString("letra")



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_silabas, container, false)

        // Usa la letra para generar las sílabas, ejemplo:
        val texto = view.findViewById<TextView>(R.id.textoSilabas)
        texto.text = generarSilabas(letra)
        return view
    }
    private fun generarSilabas(letra: String?): String {
        if (letra == null) return ""
        var vocales: List<String>? = null
        if(letra == "C") {
            vocales = listOf("a", "o", "u")
        }
        else if(letra == "G" ){
            vocales = listOf("a", "o", "u", "ue", "ui")
        }
        else if(letra == "Q"){
            vocales = listOf("ue", "ui")
        }else {
            vocales = listOf("a", "e", "i", "o", "u")
        }
        return vocales.joinToString("      ") { letra + it }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Silabas.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                Silabas().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}