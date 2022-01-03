package com.example.toonist;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // Define the pic id
    private static final int pic_id = 123;

    // One Button
    ImageButton BSelectImage;

    // Define the button variable for camera
    ImageButton camera_open_id;

    // One Preview Image
    ImageView IVPreviewImage;

    // Save image button
    ImageButton saveimage;

    // Convert Button for toonist action
    Button convertImage;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register the UI widgets with their appropriate IDs
        BSelectImage = findViewById(R.id.BSelectImage);
        camera_open_id = findViewById(R.id.camera_button);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        saveimage = findViewById(R.id.savegallery);
        convertImage = findViewById(R.id.convert_button);
        convertImage.setVisibility(View.GONE);

        // handle the Choose Image button to trigger
        // the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v1)
            {
                imageCapture();
            }
        });

        saveimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                saveToGallery();
            }
        });

        // handle the Choose Image button to trigger
        // the image chooser function
        convertImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v3) {
                convert();
            }
        });

    }

    // this function is triggered when
    // the camera button is clicked
    void imageCapture() {

        // Create the camera_intent ACTION_IMAGE_CAPTURE
        // it will open the camera for capture the image
        Intent camera_intent
                = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);

        // Start the activity with camera_intent,
        // and request pic id
        startActivityForResult(camera_intent, pic_id);
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }
            }

            // Match the request 'pic id with requestCode
            if (requestCode == pic_id) {

                // BitMap is data structure of image file
                // which stores the image in memory
                Bitmap photo = (Bitmap)data.getExtras()
                        .get("data");

                // Set the image in imageview for display
                IVPreviewImage.setImageBitmap(photo);
            }

            convertImage.setVisibility(View.VISIBLE);
        }
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void saveToGallery(){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) IVPreviewImage.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/DCIM/Toonist");
        deleteRecursive(dir);
        dir.mkdirs();

        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(dir,filename);
        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // method to convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    void convert() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) IVPreviewImage.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        // get the base 64 string from byte array from input image
        String inputImgString = Base64.encodeToString(getBytesFromBitmap(bitmap),
                Base64.NO_WRAP);

        // (api code ?) input base 64 string to return output converted base 64 string
        // String result = null;
        // try {
        //     DownloadTask downloadTask = new DownloadTask();
        //     result = downloadTask.execute("https://api.openweathermap.org/data/2.5/weather?q=" + inputImgString + "&appid=07c153a8414dc274c75e3c33451c85c5").get();
        //     JSONObject jsonObject = new JSONObject(result);
        //     JSONArray weatherArr = jsonObject.getJSONArray("weather");
        //     JSONObject weatherInfo = weatherArr.getJSONObject(0);
        //     String main = weatherInfo.getString("main");
        //     String description = weatherInfo.getString("description");
        //     String outputImgString = main + " : " + description + "\n";
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // } catch (ExecutionException e) {
        //     e.printStackTrace();
        // } catch (JSONException e) {
        //     e.printStackTrace();
        // }

        // get the byte array from response
        byte[] outputByteArray = Base64.decode(outputImgString, Base64.DEFAULT);
        // get the final output image from byte array
        Bitmap outputImage = BitmapFactory.decodeByteArray(outputByteArray, 0, outputByteArray.length);

        // Set the image in imageview for display
        IVPreviewImage.setImageBitmap(outputImage);

        convertImage.setVisibility(View.GONE);
    }

}