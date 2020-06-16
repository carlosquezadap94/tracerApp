package io.bluetrace.opentrace.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.domain.servicios.ServicioIntroItems
import io.bluetrace.opentrace.ui.adapter.IntroItemAdapter
import kotlinx.android.synthetic.main.activity_intro.*
import javax.inject.Inject

class IntroActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var introItemAdapter: IntroItemAdapter

    @Inject
    lateinit var servicioIntroItems:ServicioIntroItems


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        configureUI()
        updateRecyclerView()
    }

    private fun configureUI(){
        buttonStart.setOnClickListener(this)
        introItemAdapter = IntroItemAdapter(ArrayList(),this )
        recyclerView_introItems.adapter = introItemAdapter
    }

    private fun updateRecyclerView(){
        introItemAdapter.items.clear()
        introItemAdapter.items = servicioIntroItems.getIntroItems()
        introItemAdapter.notifyDataSetChanged();
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.buttonStart->{
                startActivity(Intent(this,
                    VerificationActivity::class.java))
            }
        }
    }
}