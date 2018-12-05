package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager


class BackupMainFragment : BaseMainFragment() {

    companion object {

        fun newInstance(): BackupMainFragment {
            val myFragment = BackupMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var manager :FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.mainfragment_backup, container, false)

        manager = childFragmentManager
        val tx = manager.beginTransaction()
//        val feedFrag = FeedSubFragment.newInstance()
//        feedFrag.clickListeners = this.clickListeners
//        tx.add(R.id.fragment_overall_container, feedFrag, null)
        tx.commit()


        return layout
    }



}