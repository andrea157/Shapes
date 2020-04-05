package it.andrea.shapes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_target.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                holeView.animateHole(viewToShow = btn_target)
            }else{
                holeView.removeAllHole()
            }
        }
    }
}
