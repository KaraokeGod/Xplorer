package com.hieu.xplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;


public class OutputActivity extends Activity implements OnClickListener{
    private ImageView crossview;
    private Button shareButton;
    private String crossPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        Intent intent = getIntent();
        crossPath = intent.getStringExtra(Intent.EXTRA_TEXT);

        crossview = (ImageView) findViewById(R.id.crossview);
        crossview.setImageBitmap(BitmapFactory.decodeFile(crossPath));

        shareButton = (Button) findViewById(R.id.sharebutton);
        shareButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Opens sharing menu
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(crossPath)));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    @Override
    public void onBackPressed() {
        // Sets transition animation back to MainActivity
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,
                                  android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
