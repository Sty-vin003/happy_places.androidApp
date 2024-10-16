package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.happyplaces.utils.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fabAddHappyPlace = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fabHappyPlace
        )
        fabAddHappyPlace.setOnClickListener(){
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
          var rv_happy_places_list = findViewById<RecyclerView>(R.id.rv_happy_places_list)
            rv_happy_places_list.layoutManager =  LinearLayoutManager(this)
            rv_happy_places_list.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        rv_happy_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,
                HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler =  object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

        // DELETE A HAPPY PLACE
        val deleteSwipeHandler =  object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

    }



    private fun getHappyPlacesListFromLocalDB(){
        val dbaHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbaHandler.getHappyPlaceList()

        if (getHappyPlaceList.size > 0){
            var rv_happy_places_list = findViewById<RecyclerView>(R.id.rv_happy_places_list)
            var tv_no_records_availble = findViewById<TextView>(R.id.tv_no_records_available)
            rv_happy_places_list.visibility = View.VISIBLE
            tv_no_records_availble.visibility = View.GONE

            setupHappyPlacesRecyclerView(getHappyPlaceList)
        }else{
            var rv_happy_places_list = findViewById<RecyclerView>(R.id.rv_happy_places_list)
            var tv_no_records_availble = findViewById<TextView>(R.id.tv_no_records_available)
            rv_happy_places_list.visibility = View.GONE
            tv_no_records_availble.visibility = View.VISIBLE
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }

    companion object {
       var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
       var EXTRA_PLACE_DETAILS = "extra_place_details"
    }

}