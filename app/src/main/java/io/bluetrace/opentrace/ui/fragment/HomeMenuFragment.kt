package io.bluetrace.opentrace.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.ui.adapter.HomeRequirementsAdapter
import kotlinx.android.synthetic.main.home_menu_fragment.view.*

class HomeMenuFragment : Fragment() {

    private lateinit var content: View
    private lateinit var homeRequirementsAdapter: HomeRequirementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        content = inflater.inflate(R.layout.home_menu_fragment, container, false)
        configureUI()
        return content
    }


    private fun configureUI() {
        homeRequirementsAdapter = HomeRequirementsAdapter(provideData(), activity!!)
        content.recyclerView_requirements.adapter = homeRequirementsAdapter
    }


    private fun provideData(): ArrayList<String> {
        var arrayList = ArrayList<String>()
        arrayList.add(context!!.getString(R.string.bluetooth))
        arrayList.add(context!!.getString(R.string.gps))
        arrayList.add(context!!.getString(R.string.notifications))
        arrayList.add(context!!.getString(R.string.question_survey))
        return arrayList
    }
}
