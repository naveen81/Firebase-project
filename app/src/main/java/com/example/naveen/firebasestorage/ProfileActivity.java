package com.example.naveen.firebasestorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {


    private static final int PICK_IMAGE_REQUEST = 123;
    private static final int CAPTURE_REQUEST_CODE = 456;
    private static final String LAT_TAG = "Latitude";
    private static final String LONG_TAG = "Longitude";
    private static final String TAG = "ProfileActivity";
    private Button buttonChoose, buttonUpload, buttonLogout, buttonCapture, saveLocation, buttonSend;
    private EditText etMessage;
    private ImageView imageView;
    private Uri filePath;
    private StorageReference storageReference;
    private DatabaseReference databaseReference, latValue, longValue;

    private FirebaseAuth firebaseAuth;
    private TwitterSession session;

    TextView textViewLat,textViewLong,textUserEmail,tvNotificationDetails;
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        saveLocation = findViewById(R.id.saveLocation);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonChoose = findViewById(R.id.buttonChoose);
        buttonCapture = findViewById(R.id.buttonCapture);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSend = findViewById(R.id.buttonSend);
        textUserEmail = findViewById(R.id.textUserEmail);
        textViewLat = findViewById(R.id.textViewLat);
        textViewLong = findViewById(R.id.textViewLong);
        etMessage = findViewById(R.id.etMessage);
        //tvNotificationDetails = findViewById(R.id.tvNotificationDetails);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

//        setNotificationData(getIntent().getExtras());

        imageView = findViewById(R.id.imageView);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        latValue = databaseReference.child("Latitude");
        longValue = databaseReference.child("Longitude");

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user.getEmail() != null)
            textUserEmail.setText("Welcome\n" + user.getEmail());
        else
            textUserEmail.setText("Welcome\n" + user.getDisplayName());

        saveLocation.setOnClickListener(this);
        buttonLogout.setOnClickListener(this);
        buttonChoose.setOnClickListener(this);
        buttonCapture.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonSend.setOnClickListener(this);

    }

    //function for firebase notification

//    private void setNotificationData(Bundle extras) {
//        if (extras == null)
//            return;
//        StringBuilder text = new StringBuilder("");
//        text.append("Message Details:");
//        text.append("\n");
//        text.append("\n");
//        if (extras.containsKey("title")) {
//            text.append("Title: ");
//            text.append(extras.get("title"));
//        }
//        text.append("\n");
//        if (extras.containsKey("message")) {
//            text.append("Message: ");
//            text.append(extras.get("message"));
//        }
//        tvNotificationDetails.setText(text);
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                filePath = createFileFromBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            filePath = data.getData();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            imageView.setImageBitmap(imageBitmap);


            // Create a file n then get Object of URI
            try {
                filePath=   createFileFromBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                filePath=null;
                Log.e(getClass().getName(), "onActivityResult: some problem occur to getting URI   "+e.getMessage());
            }
        }
    }

    private Uri createFileFromBitmap(Bitmap bitmap) throws IOException {
        String name = "image:";
        File f = new File(getCacheDir(), name + System.currentTimeMillis()+ ".jpg");
        f.createNewFile();

//Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return Uri.fromFile(f);
    }

    private void uploadFile() {
        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference imageRef = storageReference.child("images").child(filePath.getLastPathSegment());

            imageRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded Successfully", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage((int) progress + "% Uploaded...");
                        }
                    })
            ;
        } else {
            //error
            Toast.makeText(getApplicationContext(), "Choose file to upload", Toast.LENGTH_LONG).show();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQUEST);
    }

    private void onLaunchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(intent, CAPTURE_REQUEST_CODE);
        }
    }

    @Override
    public void onClick(View view) {
        //file chooser
        if (view == buttonChoose) {
            showFileChooser();
        } else if (view == buttonCapture) {
            onLaunchCamera();
        }
        //button for upload
        else if (view == buttonUpload) {
            uploadFile();
        }
        else if (view == buttonLogout) {

            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else if (view == saveLocation){
            saveLocation();
        }
        else if(view == buttonSend){
            //new updateTwitterStatus().execute(status);
            shareToTwitter();
        }
    }

    private void shareToTwitter() {

        final String status = etMessage.getText().toString();
        if(status.trim().length() > 0){

            session = TwitterCore.getInstance().getSessionManager().getActiveSession();

            final Intent intent = new ComposerActivity.Builder(ProfileActivity.this)
                    .session(session)
                    .text(status)
                    .image(filePath)
                    .hashtags("#disaster")
                    .createIntent();
            startActivity(intent);

            new MyResultReceiver();
            Log.d(TAG, "success: Tweet shared");
        } else {
            Toast.makeText(getApplicationContext(), "Message is Empty!", Toast.LENGTH_SHORT).show();
        }
    }

    class updateTwitterStatus extends AsyncTask<String, String, Void>{

        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getApplicationContext());
            pDialog.setMessage("Posting to Twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String status = params[0];
            try{

                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(String.valueOf(R.string.twitter_consumer_key));
                builder.setOAuthConsumerSecret(String.valueOf(R.string.twitter_consumer_secret));

                String access_token = session.getAuthToken().token;
                String access_token_secret = session.getAuthToken().secret;
                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                StatusUpdate statusUpdate = new StatusUpdate(status);
                InputStream is = getResources().openRawResource( + R.drawable.sign_in);
                statusUpdate.setMedia("test.jpg", is);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Posted To Twitter.", Toast.LENGTH_SHORT).show();
            etMessage.setText("");
        }

    }

    private void saveLocation() {
        String latitude = textViewLat.getText().toString();
        String longitude = textViewLong.getText().toString();
        if(!latitude.isEmpty() && !longitude.isEmpty()){
            latValue.setValue(latitude);
            longValue.setValue(longitude);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        textViewLat.setText(""+location.getLatitude());
        textViewLong.setText(""+location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("Latitude","status");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("Latitude","disable");
    }
}
