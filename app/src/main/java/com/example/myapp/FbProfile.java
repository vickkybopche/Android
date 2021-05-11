package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.squareup.picasso.Picasso;

public class FbProfile extends AppCompatActivity {

    ImageView imageView;
    TextView name,email;
    Button signOut;
    String Pfb_id, Pfb_name, Pfb_profileUrl, Pfb_email;
    SharedPreferences sharedPreferences;
    public static final String SHARED_PREFS = "sahredprefs";
    public static final String FbprofileUrl = "PfbprofileUrl", Fbname = "Pfb_name", Fbemail = "Pfb_email", Fbid = "Pfb_id";

    public static final String FB_LOGIN = "fb_login";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_profile);

        imageView = findViewById(R.id.fb_profile_pic);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        signOut = findViewById(R.id.logout);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Pfb_name = sharedPreferences.getString(Fbname, "");
        Pfb_email = sharedPreferences.getString(Fbemail, "");
        Pfb_profileUrl = sharedPreferences.getString(FbprofileUrl, "");
        Pfb_id = sharedPreferences.getString(Fbid, "");

        Picasso.get().load(Pfb_profileUrl).placeholder(R.drawable.profile).into(imageView);
        name.setText(Pfb_name);
        email.setText(Pfb_email);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder_exitbutton = new AlertDialog.Builder(FbProfile.this);
                builder_exitbutton.setTitle("Really Logout?")
                        .setMessage("Are you sure?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences settings = getSharedPreferences(FB_LOGIN, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.remove("fb_logged");
                                editor.clear();
                                editor.commit();

                                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                                editor1.clear();
                                editor1.commit();

                                Toast.makeText(FbProfile.this, "Facebook Logged out successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(FbProfile.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("No", null);
                AlertDialog alertexit = builder_exitbutton.create();
                alertexit.show();
            }
        });
    }
    AccessTokenTracker accessTokenTracker =  new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null) {
                imageView.setImageResource(0);
                name.setText(" ");
                email.setText(" ");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}