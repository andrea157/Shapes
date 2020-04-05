package it.andrea.shapes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_target_0.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                holeView.animateHole(viewToShow = btn_target_0)
            } else {
                holeView.removeAllHole()
            }
        }

        btn_target_1.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                holeView.animateHole(viewToShow = btn_target_1)
            } else {
                holeView.removeAllHole()
            }
        }

        btn_target_2.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                holeView.animateHole(viewToShow = btn_target_2)
            } else {
                holeView.removeAllHole()
            }
        }

        btn_target_3.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                holeView.animateHole(viewToShow = btn_target_3)
            } else {
                holeView.removeAllHole()
            }
        }
    }
}
