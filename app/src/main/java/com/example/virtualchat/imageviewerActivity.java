package com.example.virtualchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class imageviewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);


        imageView=(ImageView) findViewById(R.id.image_viewer);
        imageurl=getIntent().getStringExtra("url");


        Picasso.get().load(imageurl).fit().centerCrop().into(imageView);
    }
}
