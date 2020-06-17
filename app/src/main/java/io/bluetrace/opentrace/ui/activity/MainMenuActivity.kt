package io.bluetrace.opentrace.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.ui.fragment.HomeMenuFragment
import io.bluetrace.opentrace.ui.fragment.UploadDataFragment
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var homeFragment : Fragment
    private lateinit var profileFragment : Fragment
    private lateinit var activeFragment : Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        createMenuFragments()
        addFragments()
        configBottomNavigationView()
    }

    private fun configBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    private fun addFragments() {
        activeFragment = homeFragment
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer_mainMenu, profileFragment, profileFragment.tag).hide(profileFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer_mainMenu, homeFragment, homeFragment.tag).commit()
    }

    private fun createMenuFragments() {
        homeFragment = HomeMenuFragment()
        profileFragment = UploadDataFragment()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit()
        activeFragment = fragment
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home -> showFragment(homeFragment)
            R.id.action_upload_data -> showFragment(profileFragment)
        }
        return true
    }
}