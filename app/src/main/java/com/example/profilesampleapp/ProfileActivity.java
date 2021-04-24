package com.example.profilesampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;


import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    //Initialization
    TextView firstName, phoneNumber;
    Set<String> gallery = new HashSet<>();
    ViewFlipper coverPictures;
    ImageSlider coverSlider;
    de.hdodenhof.circleimageview.CircleImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().hide();

        //Assigning Variables
        firstName = findViewById(R.id.firstNameText);
        phoneNumber = findViewById(R.id.phoneText);
        profilePicture = findViewById(R.id.profilePictureView);
        coverSlider = findViewById(R.id.coverSlider);

        deleteCache();


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory, "profile_pic.jpg");
        if(file.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profilePicture.setImageBitmap(myBitmap);
        }
        Log.d("profile", "Profile Picture is Set");

        firstName.setText(sharedPref.getString("first_name", "First Name"));
        Log.d("profile", "First Name is Set");

        phoneNumber.setText(sharedPref.getString("phone_number", "Phone Number"));
        Log.d("profile", "Phone Number is Set");

        gallery = sharedPref.getStringSet("pictures", new HashSet<>());


        List<SlideModel> slides = new ArrayList<>();

        int coverCounter = 0;
        for (String coverImage:
             gallery) {
            file = new File(directory, coverImage);
            try{ if(file.exists()){

                slides.add(new SlideModel(String.valueOf(Uri.fromFile(file)), "picture : "+(coverCounter+1)));
                Log.d("profile", "picture : "+coverCounter+" added");
                coverCounter++;
            }
            else{
                break;
            }}
            catch (Exception e){
                break;
            }
        }


        coverSlider.setImageList(slides, true);
        Log.d("profile", "Cover Images are Set");
    }
    //DeleteCache
    public void deleteCache() {
        try {
            File dir = getApplicationContext().getCacheDir();
            dir.delete();
        } catch (Exception e) { e.printStackTrace();}
    }

    //Button Go Back clicked
    public void goBackClicked(View view) {
        Log.d("profile", "Back to Register");
        finish();
    }

}