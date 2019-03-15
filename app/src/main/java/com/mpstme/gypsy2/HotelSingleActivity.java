package com.mpstme.gypsy2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;

import java.security.Key;



public class HotelSingleActivity extends AppCompatActivity {

    String hotel_name;
    String hotel_address;
    String hotel_img_url;
    String hotel_booking;
    String hotel_rating_cost;

    TextView hotelName;
    TextView hotelAddress;
    TextView hotelRating;

    ImageView hotelImage;


    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_single);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(Keys.PREFERENCES, Context.MODE_PRIVATE);
        String user_email = sharedPreferences.getString("email", "null");

        Intent came_here = getIntent();
        Bundle b = came_here.getExtras();

        if (b != null){
            hotel_name = (String) b.get("hotel_name");
            hotel_address = (String) b.get("hotel_address");
            hotel_img_url = (String) b.get("hotel_image_url");
            hotel_booking = (String) b.get("hotel_booking");
            hotel_rating_cost = (String) b.get("hotel_rating");
        }

        hotelName = (TextView) findViewById(R.id.hotel_name_single);
        hotelAddress = (TextView) findViewById(R.id.hotel_address_single);
        hotelRating = (TextView) findViewById(R.id.hotel_cost_single);

        hotelName.setText(hotel_name);
        hotelAddress.setText(hotel_address);
        hotelRating.setText(hotel_rating_cost);

        hotelImage = (ImageView) findViewById(R.id.hotelSingleImage);

        Glide.with(this)
                .load(hotel_img_url)
                .centerCrop()
                .into(hotelImage);


        final String process_url = Keys.SERVER + "/get_booking/" + user_email + "&" + hotel_booking;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(HotelSingleActivity.this);

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                        new HttpRequestTask(
                                new HttpRequest(process_url, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            Snackbar.make(view, "Booking confirmed", Snackbar.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        } else {
                                            Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    }
                                }).execute();

                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();



            }
        });


    }

}
