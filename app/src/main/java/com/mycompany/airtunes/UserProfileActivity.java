package com.mycompany.airtunes;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.Firebase;

import java.io.InputStream;
import java.net.URL;
import android.widget.Button;
import android.provider.MediaStore;

/**
 * Activity class that dynamically creates view for user profiles
 * */
public class UserProfileActivity extends ActionBarActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private String fullName;
    private String username; // Spotify user uri
    private String accountType; // Premium account
    private String profilePic; // URL grabbed from facebook
    String id;
    private Drawable drawable;

    private static FirebaseCalls fb;
    User me;

    private boolean privacy = false; // Initial value of privacy

    EditText editText;
    TextView textView;

    ImageView picture; // Profile picture
    Button upload; // For uploading profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Firebase.setAndroidContext(this);
        fb = FirebaseCalls.getInstance();

        me = fb.currentUser;

        // Check for user info passed into Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fullName = extras.getString("fullName");
            accountType = extras.getString("accountType");
            profilePic = extras.getString("profilePic");
            username = extras.getString("username");
            id = extras.getString("id");
        }

        TextView tv = (TextView) findViewById(R.id.fullName);
        tv.setText(MainActivity.fullName);

        new RetrieveFeedTask().execute();

        ToggleButton toggle = (ToggleButton) findViewById(R.id.privacyToggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fb.currentUser.privacy = true;
                } else {
                    fb.currentUser.privacy = false;
                }
                fb.toggleUserPrivacy(fb.currentUser);
            }
        });

        picture = (ImageView) findViewById(R.id.uploadProfilePic);
        upload = (Button) findViewById(R.id.uploadProfilePicButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID search for a group was selected
            case R.id.searchForAGroup:
                Toast.makeText(this, "Search for group selected", Toast.LENGTH_SHORT)
                        .show();
                Intent i = new Intent(getApplicationContext(), SearchGroupActivity.class);
                startActivity(i);
                break;
            // action with ID search for a user was selected
            case R.id.searchForAUser:
                Toast.makeText(this, "Search for user selected", Toast.LENGTH_SHORT)
                        .show();
                Intent ii = new Intent(getApplicationContext(), SearchUserActivity.class);
                startActivity(ii);
                break;
            // action with ID go to my own profile was selected
            case R.id.goToMyProfile:
                Toast.makeText(this, "Go to profile selected", Toast.LENGTH_SHORT)
                        .show();
                Intent iii = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(iii);
                break;
            // action with ID logout completely was selected
            case R.id.logout:
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT)
                        .show();
                Intent iiii = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(iiii);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Allows user to change username display
     * @param view View
     * */
    public void changeUsername(View view) {
        editText = (EditText) findViewById(R.id.username);
        textView = (TextView) findViewById(R.id.textuser);
        textView.setText(editText.getText());
    }

    /**
     * Allows user to upload new profile picture from uploads
     * @param view View
     * */
    public void uploadNewPicture(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /**
     * Handles events following proper login, like grabbing profile pic
     * @param request int
     * @param result int
     * @param i Intent
     * */
    @Override
    public void onActivityResult(int request, int result, Intent i) {
        super.onActivityResult(request, result, i);
        if (request == RESULT_LOAD_IMAGE && result == RESULT_OK) {
            Uri img = i.getData();
            picture.setImageURI(img);
        }
    }

    /**
     * Takes user to view where they can search for other users
     * @param view View
     * */
    public void launchSearch(View view) {
        Intent i = new Intent(getApplicationContext(), SearchUserActivity.class);
        startActivity(i);
    }

    /**
     * Takes user to view where they can search for groups
     * @param view View
     * */
    public void launchGroupSearch(View view) {
        Intent i = new Intent(getApplicationContext(), SearchGroupActivity.class);
        startActivity(i);
    }

    /**
     * Handles logout functionality
     * @param view View
     * */
    public void logout(View view) {
        MainActivity.logout(view);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    /**
     * Takes user to appropriate playlist
     * @param view View
     * */
    public void onGoToGroupButtonClick(View view) {
        Intent i = new Intent(getApplicationContext(), SearchGroupActivity.class);
        startActivity(i);
    }

    /**
     * Takes user to view of profile
     * @param view View
     * */
    public void onGoToProfileButtonClick(View view) {
        Intent i = new Intent(getApplicationContext(), SearchUserActivity.class);
        startActivity(i);
    }

    /**
     * Takes uer to view of favorite songs list
     * @param view View
     * */
    public void viewFavSongs(View view) {
        Intent i = new Intent(getApplicationContext(), FavoriteSongsDisplayActivity.class);
        startActivity(i);
    }

    /**
     * Retrieves the Spotify user account feed
     * Async to prevent locking of main UI thread
     * */
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
        private Exception exception;

        protected void onPreExecute() { }

        protected String doInBackground(Void... urls) {
            try {
                // Grabs user profile picture
                URL url;
                url = new URL(MainActivity.profilePic);
                InputStream content = (InputStream)url.getContent();
                Drawable d = Drawable.createFromStream(content , "src");
                drawable = d;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            ImageView iv = (ImageView) findViewById(R.id.profilePic);
            iv.setImageDrawable(drawable);
        }
    }

    public void onTogglePrivacyButtonClick(View view) {
//        fb.currentUser.privacy = !fb.currentUser.privacy;
//        fb.toggleUserPrivacy(fb.currentUser);
    }
}
