package io.bluetrace.opentrace.principal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.fragment.HomeFragment

class StartActivity : AppCompatActivity() {

    var LAYOUT_MAIN_ID = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        Utils.startBluetoothMonitoringService(this)
        LAYOUT_MAIN_ID = R.id.startFragment

        openFragment(
            LAYOUT_MAIN_ID, HomeFragment(),
            HomeFragment::class.java.name, 0
        )
    }


    fun openFragment(
        containerViewId: Int,
        fragment: Fragment,
        tag: String,
        title: Int
    ) {
        try { // pop all fragments
            supportFragmentManager.popBackStackImmediate(
                LAYOUT_MAIN_ID,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            val transaction =
                supportFragmentManager.beginTransaction()
            transaction.replace(containerViewId, fragment, tag)
            transaction.commit()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}