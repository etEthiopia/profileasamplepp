package com.example.profilesampleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RegisterActivity extends AppCompatActivity {
    //Initialization
    EditText firstName, countryCode, phoneNumber;
    Button profilePicBtn, coverPicsBtn;
    Boolean profilePic, coverPictures;
    AwesomeValidation awesomeValidation;
    private File file;
    Set<String> galleryContents = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        getSupportActionBar().hide();

        //Assigning Variables
        profilePic = false;
        coverPictures = false;
        firstName = findViewById(R.id.editTextFirstName);
        countryCode = findViewById(R.id.editTextCountryCode);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        profilePicBtn = findViewById(R.id.captureProPicBtn);
        coverPicsBtn = findViewById(R.id.uploadGalleryBtn);

        //Initialize Validation
        awesomeValidation = new AwesomeValidation((ValidationStyle.BASIC));

        //Add Validation for First Name
        awesomeValidation.addValidation(firstName, RegexTemplate.NOT_EMPTY, "Please Enter Your First Name");

        //Add Validation for Country Code
        awesomeValidation.addValidation(countryCode, "^(\\+?\\d{1,3}|\\d{1,4})$", "Country Code is Invalid");

        //Add Validation for Phone Number
        awesomeValidation.addValidation(phoneNumber, "^\\d{9}$$", "Please Enter a Valid Phone Number");

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 98);
        }

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 99);
        }

    }

    //Next Button Function
    public void nextBtnClicked(View view) {
        boolean passed = true;
        Log.d("status", profilePic+" : "+coverPictures);
        //Validate the Text fields
        if(!awesomeValidation.validate()){
            passed = false;
        }
        //Check if Profile Picture is captured
        if(!profilePic){
            passed = false;
            Toast.makeText(getApplicationContext(), "Please: Take a Picture", Toast.LENGTH_SHORT).show();
        }
        //Check if Cover Pictures are uploaded
        if(!coverPictures){
            passed = false;
            Toast.makeText(getApplicationContext(), "Please: Upload Cover Pictures", Toast.LENGTH_SHORT).show();
        }
        if(passed){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("first_name", String.valueOf(firstName.getText()));
            String fullphone  = String.valueOf(countryCode.getText()) + String.valueOf(phoneNumber.getText());
            if(fullphone.charAt(0) != '+'){
                fullphone = "+" +String.valueOf(countryCode.getText()) + String.valueOf(phoneNumber.getText());
            }
            editor.putString("phone_number", fullphone);
            editor.putStringSet("pictures", galleryContents);
            editor.apply();
            Intent myIntent = new Intent(RegisterActivity.this, ProfileActivity.class);
            RegisterActivity.this.startActivity(myIntent );

        }

    }

    // Capture Profile Pic Button Function
    public void profileBtnClicked(View view) {
        // Validate the Text fields
        if(awesomeValidation.validate()){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 100);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("request_code", String.valueOf(requestCode));
        //If it was from the Camera Intent
        if(requestCode == 100 && resultCode == RESULT_OK){
            //Get Captured Image
            try {
                Bitmap captureImage = (Bitmap) data.getExtras().get("data");
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File file = new File(directory, "profile_pic.jpg");
                file.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(file);
                captureImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                profilePicBtn.setText("Picture Captured");
                profilePic = true;
            }
            catch (Exception e){
                profilePic = false;
                Toast.makeText(getApplicationContext(), "Failed to Capture: Try Again", Toast.LENGTH_SHORT).show();
            }
        }
        //If it was from the Gallery Intent
        else if(requestCode == 101 && resultCode == RESULT_OK){
            //Get Selected Images
            try {
                List<Bitmap> gallery = new ArrayList<>();
                ClipData clipData = data.getClipData();
                if(clipData != null){
                    Log.d("clip_data", "Multiple Files");
                    for(int i = 0; i < clipData.getItemCount(); i++){
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        gallery.add(bitmap);
                    }
                }
                else{
                    Log.d("clip_data", "Single File");
                    Uri imageUri = data.getData();
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    gallery.add(bitmap);
                }
                galleryContents = new HashSet<>();
                for (int i = 0; i < gallery.size(); i++) {
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    Random rand = new Random();
                    int n = rand.nextInt(100);
                    String fileName = String.valueOf(firstName.getText())+n+".jpg";
                    File file = new File(directory, fileName) ;
                    if(file.exists()){
                        file.delete();
                        Log.d("file exists", "cover"+i);
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    gallery.get(i).compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    galleryContents.add(fileName);
                    Log.d("Gallery Save", String.valueOf(gallery.size()));

                    fos.flush();
                    fos.close();
                }
                Log.d("Gallery Size", String.valueOf(gallery.size()));
                if(gallery.size()>0){
                    coverPicsBtn.setText(gallery.size()+ " Pictures Uploaded");
                    coverPictures = true;
                }else{
                    coverPictures = false;
                }

            }
            catch (Exception e){
                coverPictures = false;
                Toast.makeText(getApplicationContext(), "Failed to Retrieve : Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void galleryBtnClicked(View view) {
        //Validate the Text fields
        if(awesomeValidation.validate()){
            //Open Gallery
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, 101);
        }
        }
    }
