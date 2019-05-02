package io.dancmc.dsync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_gallery_item_grid.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


class GalleryAdapter(private val context: Context?, var dataset: List<MediaObj>, var listener:(MediaObj)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val image = v.gallery_item_grid_image
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_gallery_item_grid, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = dataset[position]
        holder as Holder

        holder.itemView.onClick {
            listener.invoke(item)
        }

        Glide.with(context!!).load(item.filepath).into(holder.image)

    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}