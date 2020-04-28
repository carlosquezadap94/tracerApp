package io.bluetrace.opentrace.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.api.ApiService
import io.bluetrace.opentrace.models.*
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecord
import io.bluetrace.opentrace.streetpass.view.RecordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_upload.view.*


/**
 * A simple [Fragment] subclass.
 */
class UploadFragment : Fragment() {
    private lateinit var viewModel: RecordViewModel

    @SuppressLint("HardwareIds")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view_ = inflater.inflate(R.layout.fragment_upload, container, false)
        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        val button_uplad = view_.button_upload

        val name = "User name"
        val lastName = "User Last name"
        val identityDocumentType = "CC"
        val identityDocumentValue = "XXX"
        val phoneNumber = "PhoneNumber"
        val deviceIdForVendor =
            Settings.Secure.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)
        val macAddress = "xxx"
        val deviceName = android.os.Build.MODEL
        val deviceOs = "Andriod v${android.os.Build.ID}"
        val deviceModel = android.os.Build.DEVICE

        button_uplad.setOnClickListener({

            viewModel.allRecords.observe(activity!!, Observer { records ->
                val rec = records
                val deviceIdentifiers = DeviceIdentifiers(deviceIdForVendor, macAddress)
                val other = OtherData(deviceModel, deviceName, deviceOs)
                val deviceData = DeviceData(deviceIdentifiers, other)
                val eventos: List<Event> = createRecord(rec)
                val userData = UserData(
                    identityDocumentType,
                    identityDocumentValue,
                    lastName,
                    name,
                    phoneNumber
                )

                val base = Data(deviceData = deviceData, events = eventos, userData = userData)

                val observable = ApiService.apiCall().sendData(base)
                observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ loginResponse ->
                        var response = loginResponse
                    }, { error ->
                        var t = error
                    }
                    )
            })
        })

        return view_
    }

    fun createRecord(record: List<StreetPassRecord>): List<Event> {
        var list = ArrayList<Event>()
        for (recor in record) {
            val eventDate = "2020-04-24T01:15:31.263Z"
            val eventType = "DEVICE-CONTACT"
            val eventduration = 20
            val detectionMethod = "BLUETOOTH"
            val location = EventLocation("6.3805309", "-75.4465323")
            val deviceContact = DeviceInContact(recor.msg, "macAdress", recor.rssi, 0)
            val eventData = EventData(detectionMethod, deviceContact, eventduration, location)
            val evento = Event(eventData, eventDate, eventType)
            list.add(evento)
        }

        return list

    }


}
