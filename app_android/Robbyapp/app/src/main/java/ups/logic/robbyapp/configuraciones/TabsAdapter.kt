package ups.logic.robbyapp.configuraciones

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ups.logic.robbyapp.learn.Silabas

class TabsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val letras = listOf(
        "B", "C", "D", "F", "G", "H", "J", "K", "L", "M",
        "N", "Ñ", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z"
    )

    override fun getItemCount(): Int = letras.size

    override fun createFragment(position: Int): Fragment {
        val fragment = Silabas()
        fragment.arguments = Bundle().apply {
            putString("letra", letras[position])
        }
        return fragment
    }
}
