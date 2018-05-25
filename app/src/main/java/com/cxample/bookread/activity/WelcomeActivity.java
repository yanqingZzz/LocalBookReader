package com.cxample.bookread.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cxample.bookread.MainActivity;
import com.cxample.bookread.R;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        if(checkSettingPermission()) {
//            gotoMainActivity();
//        }
        gotoMainActivity();
    }

    private void gotoMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 2000);

    }

//    private static final int REQUEST_CODE_ASK_WRITE_SETTINGS = 100;

//    private boolean checkSettingPermission() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
//                    Uri.parse("package:" + getPackageName()));
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
//            return false;
//        }
//        return true;
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_CODE_ASK_WRITE_SETTINGS) {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
//                gotoMainActivity();
//            } else {
//                finish();
//            }
//        }
//    }
}
