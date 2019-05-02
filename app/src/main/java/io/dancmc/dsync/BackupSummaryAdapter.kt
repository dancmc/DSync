package io.dancmc.dsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_backup_summary.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


// Adapter for the list of bluetooth devices in BluetoothSubFragment
class BackupSummaryAdapter(var dataset: ArrayList<Pair<String, Int>>, var clickListener:(String)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val name = v.adapter_backup_summary_label
        val number = v.adapter_backup_summary_number
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_backup_summary, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = dataset[position]
        holder as Holder

        holder.itemView.onClick {
            clickListener.invoke(item.first)
        }

        holder.name.text = item.first
        holder.number.text = if(item.second>0)item.second.toString() else ""

    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}