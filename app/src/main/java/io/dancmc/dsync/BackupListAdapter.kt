package io.dancmc.dsync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.adapter_backup_list.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File


// Adapter for the list of bluetooth devices in BluetoothSubFragment
class BackupListAdapter(var context: Context, var type:String, var dataset: List<RealmDifference>,
                        var checkListener:(RealmDifference,Boolean)->Unit, var deleteCheckListener:(RealmDifference,Boolean)->Unit={_,_->}) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val image = v.adapter_backup_list_image
        val folder = v.adapter_backup_list_folder
        val filepath = v.adapter_backup_list_filepath
        val checkbox = v.adapter_backup_list_checkbox
        val deleteCheckbox = v.adapter_backup_list_checkbox_delete
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_backup_list, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = dataset[position]
        holder as Holder

        holder.itemView.onClick {

        }

        holder.folder.text = item.folders.joinToString()
        holder.checkbox.isChecked = !item.ignored
        holder.checkbox.onClick {
            checkListener.invoke(item, !holder.checkbox.isChecked)
        }

        if(type==BackupSummarySubFragment.ON_SERVER){
            val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
            Glide.with(context).load(GlideHeader.getUrlWithHeaders(MediaRetrofit.getPhotoThumbUrl(item.uuid))).apply(requestOptions).into(holder.image)
            holder.filepath.text = item.filepaths.joinToString()
            holder.deleteCheckbox.isChecked = item.deleteOffServer
            holder.deleteCheckbox.onClick {
                deleteCheckListener.invoke(item, holder.deleteCheckbox.isChecked)
            }
        } else {
            Glide.with(context).load(item.filepaths[0]).into(holder.image)
            holder.filepath.text = item.filepaths.map { File(it).name }.joinToString()
            holder.deleteCheckbox.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}