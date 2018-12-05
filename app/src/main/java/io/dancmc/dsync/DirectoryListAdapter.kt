package io.dancmc.dsync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_directorylist.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


// Adapter for the list of bluetooth devices in BluetoothSubFragment
class DirectoryListAdapter(private val context: Context?, var dataset: ArrayList<ImageDirectory>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener :DirectoryListAdapter.Listener?=null


    inner class DirectoryHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image = v.adapter_directorylist_image
        val name = v.adapter_directorylist_name
        val number = v.adapter_directorylist_number
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DirectoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_directorylist, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = dataset[position]
        holder as DirectoryHolder

        holder.itemView.onClick {
            listener?.onClick(item)
        }

        Glide.with(context!!).load(item.displayPhoto).into(holder.image)
        holder.name.text = item.albumName
        holder.number.text = item.numPhotos.toString()

    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    interface Listener{
        fun onClick(directory:ImageDirectory)
    }

}