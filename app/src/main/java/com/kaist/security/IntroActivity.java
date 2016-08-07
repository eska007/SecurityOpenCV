package com.kaist.security;

/**
 * Created by user on 2016-08-01.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class IntroActivity extends AppCompatActivity {
    static String TAG = "[SM]";
    private Context mContext;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        TextView titleView = (TextView) findViewById(R.id.intro_subtitle);
        Button startBt = (Button) findViewById(R.id.start_main);
        Button confirmBt = (Button) findViewById(R.id.confirm_bt);
        Button changeKeyBt = (Button) findViewById(R.id.change_key);

        SharedPreferences prefs = mContext.getSharedPreferences("SM", MODE_PRIVATE);
        String key = prefs.getString("regist_key", "");
        if (key == null || key.equals("")) {
            titleView.setText(R.string.intro_subtitle_enter);
            registPrivateKey(confirmBt);
        } else {
            titleView.setText(R.string.intro_subtitle_select);
            startBt.setVisibility(View.VISIBLE);
            changeKeyBt.setVisibility(View.VISIBLE);
            startBt.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startMainActivity(0);
                }
            });
            changeKey(titleView, changeKeyBt);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void changeKey(final TextView titleView, final Button changeKeyBt) {

        changeKeyBt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleView.setText(R.string.intro_subtitle_enter);
                registPrivateKey(changeKeyBt);
            }
        });
    }

    private void startMainActivity(int timer) {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, timer);
    }

    private void registPrivateKey (Button bt) {
        Log.d(TAG, "registPrivateKey()");

        if (bt == null) {
            throw new AssertionError();
        }
        bt.setVisibility(View.VISIBLE);

        final EditText key = (EditText) findViewById(R.id.edit_regist_key);
        if (key == null) {
            throw new AssertionError();
        }
        key.setVisibility(View.VISIBLE);

        bt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO : click event

                SharedPreferences prefs = mContext.getSharedPreferences("SM", MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString("regist_key", String.valueOf(key.getText()));
                prefEditor.apply();

                startMainActivity(0);
            }
        });
    }
}
