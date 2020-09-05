package com.mohitsrivastava1311.bharatcamscan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class ScanActivity extends AppCompatActivity {

    private static final int CROP_REQUEST_CODE = 2000;
    private ImageView imageView, imageView2, btnupload, Save, Cancel;
    private Uri selectedFileUri;
    private String path;
    private File file;
    private File pdfFile;

    public String image_name ="";

    BitmapDrawable bitmapDrawable;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        imageView = (ImageView) findViewById(R.id.image_view);
        imageView2 = (ImageView) findViewById(R.id.image_view2);
        btnupload = (ImageView) findViewById(R.id.button_upload);

        Save = (ImageView) findViewById(R.id.save);
        Cancel = (ImageView) findViewById(R.id.cancel);

        Uri image_uri = getIntent().getData();
        imageView.setImageURI(image_uri);
        selectedFileUri = image_uri;

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ScanActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmapDrawable = (BitmapDrawable)imageView2.getDrawable();
                bitmap = bitmapDrawable.getBitmap();

                FileOutputStream outputStream = null;

                File sdCard = Environment.getExternalStorageDirectory();
                File directory = new File(sdCard.getAbsolutePath() + "/BharatCam");
                directory.mkdir();

                image_name = "IMG-" + System.currentTimeMillis();
                String filename = image_name + ".jpg";
                File outFile = new File(directory, filename);

                try {
                    outputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/BharatCamScan");
                if(!docsFolder.exists()) {
                    docsFolder.mkdir();
                }

                String pdfName = image_name + ".pdf";
                pdfFile = new File(docsFolder.getAbsolutePath(), pdfName);
                OutputStream output = null;
                try {
                    output = new FileOutputStream(pdfFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Document document = new Document();

                try {
                    PdfWriter.getInstance(document, output);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                document.open();
                Image image = null;

                try {
                    image = Image.getInstance(directory +"/" + image_name+".jpg");
                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0)/image.getWidth())*100;

                image.scalePercent(scaler);
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                try {
                    document.add(image);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                document.close();
                Toast.makeText(ScanActivity.this, "PDF Saved", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(ScanActivity.this, HomeActivity.class);
                startActivity(i);

            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // uploadFile();
               // CropImage.startPickImageActivity(ScanActivity.this);
                launchImageCrop(selectedFileUri);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                if(result.getUri()!=null){
                    setImage(result.getUri());
                }
            }
        }
    }



    private void setImage(Uri uri) {
        imageView.setVisibility(View.INVISIBLE);
        Glide.with(this)
                .load(uri)
                .into(imageView2);

        btnupload.setVisibility(View.INVISIBLE);
        Save.setVisibility(View.VISIBLE);
        Cancel.setVisibility(View.VISIBLE);

    }

    private void launchImageCrop(Uri uri) {
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAutoZoomEnabled(true)
                .setAllowRotation(true)
                .setAllowCounterRotation(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this);

    }


}




