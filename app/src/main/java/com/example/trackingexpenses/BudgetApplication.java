package com.example.trackingexpenses;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class BudgetApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set default locale to Hebrew
        setLocale(this);
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(setLocale(base));
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLocale(this);
    }
    
    /**
     * Sets the application locale to Hebrew
     */
    private static Context setLocale(Context context) {
        Locale locale = new Locale("iw"); // Hebrew locale code
        Locale.setDefault(locale);
        
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
}
