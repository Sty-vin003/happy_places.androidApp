package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetailModel : HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel =
                intent.getSerializableExtra(
                    MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null){
            var toolbar_happy_place_detail = findViewById<Toolbar>(R.id.toolbar_happy_place_detail)
            setSupportActionBar(toolbar_happy_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            toolbar_happy_place_detail.setNavigationOnClickListener{
                onBackPressed()
            }
            var tv_location = findViewById<TextView>(R.id.tv_location)
            var tv_description = findViewById<TextView>(R.id.tv_description)
            var iv_place_image = findViewById<AppCompatImageView>(R.id.iv_place_image)
            iv_place_image.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            tv_description.text = happyPlaceDetailModel.description
            tv_location.text = happyPlaceDetailModel.location

            var btn_view_on_map = findViewById<Button>(R.id.btn_view_on_map)
            btn_view_on_map.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }

        }
    }
}