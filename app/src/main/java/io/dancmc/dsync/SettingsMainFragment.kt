package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager


class SettingsMainFragment : BaseMainFragment() {

    companion object {

        fun newInstance(): SettingsMainFragment {
            val myFragment = SettingsMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var manager :FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.mainfragment_settings, container, false)

        manager = childFragmentManager
        val tx = manager.beginTransaction()
        val settingsFrag = SettingsSubFragment.newInstance()
        tx.add(R.id.fragment_overall_container, settingsFrag, null)
        tx.commit()


        return layout
    }



}