package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {
    private var onClickListener: OnClickListener? = null


    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemHappyPlaceBinding.inflate(LayoutInflater.from(context), parent, false) // Update this line
        return MyViewHolder(binding) // Update this line
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]


        holder.binding.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.binding.tvTitle.text = model.title
            holder.binding.tvDescription.text = model.description

            holder.itemView.setOnClickListener{
                if (onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
    }

    fun removeAt(position: Int) {
            val dbHandler = DatabaseHandler(context)
            val isDeleted = dbHandler.deleteHappyPlace(list[position])
        if (isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestcode: Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestcode)
        notifyItemChanged(position)
    }


    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }



    interface  OnClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    class MyViewHolder(val binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root) // Update this line
}
