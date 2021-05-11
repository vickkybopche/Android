package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity{

    String name, id,  profilePicUrl, email;
    SignInButton gSignIn ;
    LoginButton fSignIn;
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    int RC_SIGN_IN = 0;

    SharedPreferences sharedPreferences;
    public static final String SHARED_PREFS = "sahredprefs";
    public static final String FbprofileUrl = "PfbprofileUrl", Fbname = "Pfb_name", Fbemail = "Pfb_email", Fbid = "Pfb_id";
    public static final String FB_LOGIN = "fb_login";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //for facebook

        SharedPreferences fb_settings = getSharedPreferences(FB_LOGIN, 0);
        if (fb_settings.getString("fb_logged", "").toString().equals("fb_logged")) {
            startActivity(new Intent(MainActivity.this, FbProfile.class));
            this.finish();
        }
        gSignIn = findViewById(R.id.sign_in_button);
        gSignIn.setSize(SignInButton.SIZE_STANDARD);
        fSignIn = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();

        fSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,Arrays.asList("email"));

                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fSignIn.setVisibility(View.INVISIBLE);
                        gSignIn.setVisibility(View.INVISIBLE);
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("Loading data...");
                        progressDialog.show();

                        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d("Demo", object.toString());

                                try {
                                    name = object.getString("name");
                                    id = object.getString("id");
                                    if(object.getJSONObject("picture").getJSONObject("data").getString("url") != null) {
                                        profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    }else{
                                        profilePicUrl = "null";
                                    }

                                    if(object.has("email")) {
                                        email = object.getString("email");
                                    }else{
                                        email = " ";
                                    }

                                    SharedPreferences settings = getSharedPreferences(FB_LOGIN, 0);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("fb_logged", "fb_logged");
                                    editor.commit();
                                    sendfbData();
                                    progressDialog.dismiss();

                                    Toast.makeText(MainActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, FbProfile.class));
                                    finish();


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        Bundle bundle = new Bundle();
                        bundle.putString("fields","picture.type(large),gender, name, id, birthday, friends, email");
                        graphRequest.setParameters(bundle);
                        graphRequest.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        fSignIn.setVisibility(View.VISIBLE);
                        gSignIn.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Login cancelled.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        fSignIn.setVisibility(View.VISIBLE);
                        gSignIn.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Login error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        //for google

        gSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }
            }
        });


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    private void sendfbData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Fbname,name);
        editor.putString(Fbemail,email);
        editor.putString(FbprofileUrl,profilePicUrl);
        editor.putString(Fbid,id);
        editor.apply();
    }

    //for google
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for facebook and google.
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }




    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(MainActivity.this,GmailProfile.class);
            startActivity(intent);
        } catch (ApiException e) {
            fSignIn.setVisibility(View.VISIBLE);
            gSignIn.setVisibility(View.VISIBLE);
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("error", "signInResult:failed code=" + e.getStatusCode());

        }
    }


}