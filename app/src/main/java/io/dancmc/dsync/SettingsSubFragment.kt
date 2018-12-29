package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_settings.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


class SettingsSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): SettingsSubFragment {
            val myFragment = SettingsSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var realm: Realm


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_settings, container, false)


        val spinner = layout.subfragment_settings_address_spinner
        ArrayAdapter(context, android.R.layout.simple_spinner_item,
                arrayOf("Macbook",
                        "Raspberry Home",
                        "Raspberry Away",
                        "Scaleway")
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        val pos = when(Prefs.instance!!.readString(Prefs.API_URL, "https://dancmc.host")){
            "http://192.168.1.47:8080"->0
            "http://192.168.1.20"->1
            "https://dancmc.host"->2
            "https://dancmc.io"->3
            else ->0
        }
        spinner.setSelection(pos)
        spinner.onItemSelectedListener= object :AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val url = when(position){
                    0-> "http://192.168.1.47:8080"
                    1-> "http://192.168.1.20"
                    2-> "https://dancmc.host"
                    3-> "https://dancmc.io"
                    else->"https://dancmc.host"
                }
                Prefs.instance!!.writeString(Prefs.API_URL,url)
                MediaRetrofit.domain = url
                MediaRetrofit.rebuild()
            }


        }

        layout.subfragment_settings_logout.onClick { (activity as? MainActivity)?.logout() }

        return layout
    }



}