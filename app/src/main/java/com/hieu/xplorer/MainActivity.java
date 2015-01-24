package com.hieu.xplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {
    private final static int CAMERA_PIC_REQUEST = 1;
    private final String message[] = {"Take left picture", "Take right picture"};

    private String imagePath[];
    private ImageView  image[];
    private Bitmap    bitmap[];

    private Button magicButton;
    private int imageNum = 0;   // Choose which side to work on
    private boolean done = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePath = new String[2];
        image = new ImageView[2];
        image[0] = (ImageView) findViewById(R.id.image1);
        image[1] = (ImageView) findViewById(R.id.image2);
        bitmap = new Bitmap[2];

        magicButton = (Button)findViewById(R.id.magicbutton);
        magicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

                // Creates temporary image file
                File photoFile = null;
                try {
                    photoFile = saveImageFile();
                }
                    catch(Exception e) {
                }

                // Saves image if creation was successful
                if (photoFile != null)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                startActivityForResult(intent, CAMERA_PIC_REQUEST);
            }
        });

        reset();
    }

    private void reset() {
        image[0].setImageDrawable(getResources().getDrawable(R.drawable.image3));
        image[1].setImageDrawable(getResources().getDrawable(R.drawable.image3));

        imageNum = 0;
        done = false;
        magicButton.setText(message[0]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if(!done) {
            menu.findItem(R.id.action_discard).setVisible(false);
            menu.findItem(R.id.action_swap).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_discard:
                reset();
                return true;
            case R.id.action_swap:
                swapImages();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private File createImageFile(String suffix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "Xplorer_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFilename, suffix, storageDir);
    }

    private File saveImageFile() throws IOException {
        File image = createImageFile(".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        imagePath[imageNum] = "file:" + image.getAbsolutePath();
        return image;
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
                    invalidateOptionsMenu();
                    //saveCrossviewImage();
                }

                imageNum = 1 - imageNum;
                magicButton.setText(message[imageNum]);
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
        // Scale bitmaps
        Bitmap left  = Bitmap.createScaledBitmap(bitmap[0], 1080, 1920, false);
        Bitmap right = Bitmap.createScaledBitmap(bitmap[1], 1080, 1920, false);

        int width  = left.getWidth() + right.getWidth();
        int height = (left.getHeight() + right.getHeight()) / 2;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas crossview = new Canvas(bmp);

        crossview.drawBitmap(left, 0f, 0f, null);
        crossview.drawBitmap(right, left.getWidth(), 0f, null);

        FileOutputStream out = null;
        File image = createImageFile(".png");
        try {
            out = new FileOutputStream(image);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
