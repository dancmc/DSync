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
        Utils.createServerSpinnerAdapter(context, spinner)

        layout.subfragment_settings_logout.onClick { (activity as? MainActivity)?.logout() }

        return layout
    }



}