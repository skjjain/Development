package com.samyak.crackit;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class GetStarted extends FragmentActivity  implements
        ConnectionCallbacks, OnConnectionFailedListener, OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean inIntentProgress;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    private boolean isSigningClicked;
    private SignInButton mSignInButton;
    private Button mSignOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        initializeGoogleAPI();
        mSignInButton = (SignInButton)findViewById(R.id.sign_in_button);
        mSignOutButton = (Button)findViewById(R.id.sign_out_button);
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
    }

    void initializeGoogleAPI() {
        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_started, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInButton.setEnabled(false);
        mSignOutButton.setEnabled(true);
        isSigningClicked = false;
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        String prefix = "Mrs. ";
        if(currentUser.getGender() == Person.Gender.MALE){
            prefix = "Mr. ";
        }
        Toast.makeText(this, prefix + currentUser.getDisplayName() + " age "+currentUser.getBirthday()+" is connected!" , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!inIntentProgress){
            if(isSigningClicked && connectionResult.hasResolution()) {
                try {
                    inIntentProgress = true;
                    startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    inIntentProgress = true;
                    mGoogleApiClient.connect();
                }
            }
        }

    }

    public void onClick(final View view) {
        if(!mGoogleApiClient.isConnecting()) {
            switch (view.getId()) {
                case R.id.sign_in_button:{
                    isSigningClicked = true;
                    mGoogleApiClient.connect();
                }break;
                case R.id.sign_out_button:{
                    if(mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                        // Prior to disconnecting, run clearDefaultAccount().
                        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        Toast.makeText(view.getContext(), "Disconnected!", Toast.LENGTH_LONG).show();
                                    }
                                });
                        mGoogleApiClient.disconnect();
                        mSignInButton.setEnabled(true);
                        mSignOutButton.setEnabled(false);
                    }
                }break;

            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                isSigningClicked = false;
            }

            inIntentProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.reconnect();
            }
        }
    }


}
