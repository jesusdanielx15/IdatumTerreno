package cl.idatum.idatumterreno.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.utils.Config;

public class SplashScreen extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        Thread myTread = new Thread() {
            @Override
            public void run() {
                if (prefs.getBoolean(Config.FIRST_TIME, false)) {


                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(Config.FIRST_TIME, true);
                        editor.commit();
                    }
                    try {
                        sleep(3000);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                super.run();
            }
        };

        myTread.start();

    }


}
