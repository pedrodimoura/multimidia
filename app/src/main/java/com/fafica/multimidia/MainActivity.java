package com.fafica.multimidia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private File mFile;
    private ImageView mImageViewPicture;
    LoadImageTask loadImageTask;
    int imageWidth;
    int imageHeight;

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    private void init() {

        String picturePath = ImageUtils.getLastMedia(MainActivity.this, ImageUtils.MEDIA_PICTURE);

        if (picturePath != null)  mFile = new File(picturePath);

        this.mImageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);
        this.imageWidth = this.mImageViewPicture.getWidth();
        this.imageHeight = this.mImageViewPicture.getHeight();
        Log.d("TAG", "Image Width -> " + this.mImageViewPicture.getWidth());
        Log.d("TAG", "Image Height -> " + this.mImageViewPicture.getHeight());
        this.mButton = (Button) findViewById(R.id.buttonTakePicture);
        this.mButton.setOnClickListener(this);

        loadImage();
    }

    private void loadImage() {
        if (this.mFile != null && mFile.exists()) {
            if (loadImageTask == null || loadImageTask.getStatus() != AsyncTask.Status.RUNNING) {
                loadImageTask = new LoadImageTask();
                loadImageTask.execute();
            }
        }
    }

    private void openCamera() {
        mFile = ImageUtils.newMedia(ImageUtils.MEDIA_PICTURE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
        startActivityForResult(intent, ImageUtils.REQUEST_CODE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ImageUtils.REQUEST_CODE_PICTURE:
                switch (resultCode) {
                    case AppCompatActivity.RESULT_OK:
                        loadImage();
                        break;
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonTakePicture:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openCamera();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    }
                } else {
                    openCamera();
                }
                break;
        }
    }

    class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            return ImageUtils.loadImage(mFile, imageWidth, imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                Log.d("TAGM", "Bitmap != null");
                mImageViewPicture.setImageBitmap(bitmap);
                ImageUtils.saveLastMedia(MainActivity.this, ImageUtils.MEDIA_PICTURE, mFile.getAbsolutePath());
            } else {
                Log.d("TAGM", "Bitmap == null");
            }
        }
    }

}
