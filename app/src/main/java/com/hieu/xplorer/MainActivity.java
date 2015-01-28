package com.hieu.xplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements OnClickListener{
    private final static int CAMERA_PIC_REQUEST = 1;
    private final String message[] = {"Take left picture", "Take right picture", "Save"};

    private String imagePath[];
    private ImageView  image[];
    private Bitmap    bitmap[];

    private Button photoButton;
    private int imageNum = 0;   // Choose which side to work on
    private boolean done = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = new ImageView[]{(ImageView) findViewById(R.id.image1),
                                (ImageView) findViewById(R.id.image2)};
        photoButton = (Button)findViewById(R.id.magicbutton);

        reset();
        photoButton.setOnClickListener(this);
    }

    private void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        // Creates temporary image file
        File imageFile = null;
        try {
            imageFile = createImageFile(".jpg");

            // Save a file: path for use with ACTION_VIEW intents
            imagePath[imageNum] = "file:" + imageFile.getAbsolutePath();

            // Saves image if creation was successful
            if (imageFile != null)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, CAMERA_PIC_REQUEST);

        } catch (Exception e) {}
    }

    public void onClick(View view) {
        if(done) {
            try {
                saveCrossviewImage();
            } catch(Exception e){}
        }
        else
            takePhoto();
    }

    // Delete files in directory
    private void deleteImages() {
        if(imagePath != null) {
            for(int i=0; i < 2; i++) {
                if(imagePath[i] != null) {
                    File file = new File(imagePath[i].substring(5));
                    file.delete();
                }
            }
        }
    }
    private void reset() {
        deleteImages();

        imagePath = new String[2];
        bitmap    = new Bitmap[2];
        image[0].setImageDrawable(getResources().getDrawable(R.drawable.image3));
        image[1].setImageDrawable(getResources().getDrawable(R.drawable.image3));

        imageNum = 0;
        done = false;
        photoButton.setText(message[0]);
        photoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_action_camera, 0, 0, 0);

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        if(!done) {
            //menu.findItem(R.id.action_discard).setVisible(false);
            menu.findItem(R.id.action_swap)   .setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_discard:
                if(imagePath[0] == null)
                    Toast.makeText(this, "Nothing to delete.", Toast.LENGTH_SHORT).show();
                reset();
                return true;
            case R.id.action_swap:
                swapImages();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private File createImageFile(String extension) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "Xplorer_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFilename, extension, storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            try {
                // Discard the "file:" part of the path
                bitmap[imageNum] = BitmapFactory.decodeFile(imagePath[imageNum].substring(5));
                image[imageNum].setImageBitmap(bitmap[imageNum]);

                if(imageNum == 1) {
                    done = true;
                    photoButton.setText(message[2]);
                    photoButton.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_action_save, 0, 0, 0);
                    invalidateOptionsMenu();
                }

                imageNum = 1 - imageNum;
                if(!done)
                    photoButton.setText(message[imageNum]);
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            }
            catch(Exception e) {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                System.out.println(e);
            }
        }
    }

    private void swapImages() {
        String tempPath = imagePath[1];
        imagePath[1] = imagePath[0];
        imagePath[0] = tempPath;

        Bitmap tempMap = bitmap[1];
        bitmap[1] = bitmap[0];
        bitmap[0] = tempMap;

        image[0].setImageBitmap(bitmap[0]);
        image[1].setImageBitmap(bitmap[1]);
    }

    private void saveCrossviewImage() throws IOException {
        // Get settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int scaledWidth = Integer.parseInt(sharedPref.getString("pref_key_resolution", ""));
        int divider = sharedPref.getInt("pref_key_divider", 0);
        int border  = sharedPref.getInt("pref_key_border", 0);

        // Scale bitmaps
        Bitmap left = bitmap[0];  // Native resolution
        Bitmap right = bitmap[1];
        if(scaledWidth > 0) {
            left  = Bitmap.createScaledBitmap(bitmap[0], scaledWidth, scaledWidth * 16 / 9, false);
            right = Bitmap.createScaledBitmap(bitmap[1], scaledWidth, scaledWidth * 16 / 9, false);
        }

        int width  = left.getWidth() + right.getWidth() + divider + (border * 2);
        int height = (left.getHeight() + right.getHeight()) / 2 + (border * 2);

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE);
        Canvas crossview = new Canvas(bmp);

        crossview.drawBitmap(left, border, border, null);
        crossview.drawBitmap(right, border + left.getWidth() + divider, border, null);

        FileOutputStream out;
        File image = createImageFile(".jpg");
        try {
            out = new FileOutputStream(image);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {}

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
    }

}
