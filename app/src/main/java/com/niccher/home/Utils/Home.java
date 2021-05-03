package com.niccher.home.Utils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;


public class Home extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder bul=new Picasso.Builder(this);
        bul.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso buil=bul.build();
        buil.setIndicatorsEnabled(false);
        buil.setLoggingEnabled(true);
        Picasso.setSingletonInstance(buil);
    }
}
