package io.bluetrace.opentrace.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.bluetrace.opentrace.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(),View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupUI()

    }

    private fun setupUI(){
        buttonEnter.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.buttonEnter->{
                startActivity(
                    Intent(this,
                    VerificationActivity::class.java)
                )
            }
        }
    }
}