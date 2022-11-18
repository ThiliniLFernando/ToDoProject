package com.smart.planner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceManager;

import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smart.planner.Classes.NetworkUtils;
import com.smart.planner.Classes.RequestCodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileScreen extends AppCompatActivity {

    private Toolbar thisToolbar;
    private ImageView thisProfileImage;
    private ImageButton imageOption;
    private Button changeProfileButton;
    private ProgressBar imageUploadProgress;

    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference rootReference;
    private StorageReference profileReference;
    private SharedPreferences sharedPreferences;

    private String tempImageFileLocation;
    private Uri tempPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Main.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);
        initComponents();
    }

    private void initComponents() {
        thisToolbar = findViewById(R.id.ps_toolbar);
        thisProfileImage = findViewById(R.id.ps_user_profile);
        imageOption = findViewById(R.id.change_picture_option);
        changeProfileButton = findViewById(R.id.change_profile_button);
        imageUploadProgress = findViewById(R.id.upload_progress_circle);

        imageUploadProgress.setVisibility(View.INVISIBLE);

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        rootReference = firebaseStorage.getReference();
        profileReference = rootReference.child(Main.CURRENT_USER_KEY +""+ Main.PROFILE_PATH);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfilePicture();
            }
        });

        setSupportActionBar(thisToolbar);
        getSupportActionBar().setTitle("Change Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setProfileImage();

        setProfileImage();

        imageOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionDialog();
            }
        });
    }

    private void showOptionDialog() {
        String options[] = new String[]{"Open Camera", "Select from Gallery"};
        final AlertDialog.Builder option_dialog = new AlertDialog.Builder(this);
        option_dialog.setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestCameraOrGallery(which);
                dialog.dismiss();
            }
        });
        AlertDialog alert = option_dialog.create();
        alert.show();
    }

    private void requestCameraOrGallery(int selectedIndex) {
        switch (selectedIndex) {
            case 0: {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // camera permission granted
                    openCamera();
                } else {
                    // camera permission not granted
                    requestPermissions(
                            new String[]{Manifest.permission.CAMERA},
                            RequestCodes.CAMERA_PERMISSION_REQUEST_CODE);
                }
                return;
            }

            case 1: {
                openGallery();
                return;
            }
        }
    }

    private void openGallery() {
        Intent callGalleryRequestIntent = new Intent();
        callGalleryRequestIntent.setType("image/*");
        callGalleryRequestIntent.setAction(Intent.ACTION_GET_CONTENT);
        openGalleryInt.launch(Intent.createChooser(callGalleryRequestIntent, "Select Picture"));
    }

    private void openCamera() {
        Intent callCameraAppIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        Uri photoURI = FileProvider.getUriForFile(
                getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                photoFile);
        callCameraAppIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        openCameraInt.launch(callCameraAppIntent);
    }

    private void setProfileImage() {
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ProfileScreen.this)
                        .load(profileReference)
                        .apply(new RequestOptions().signature(new ObjectKey(Main.getProfileSignature())))
                        .into(thisProfileImage);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case RequestCodes.CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    // write what to do when permission denied !
                }
                return;
            }
        }
    }

    ActivityResultLauncher<Intent> openCameraInt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== RESULT_OK){
                        rotateImage(setReducedImageSize());
                        if (tempImageFileLocation != null) {
                            File image = new File(tempImageFileLocation);
                            tempPhotoUri = Uri.fromFile(image);
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> openGalleryInt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== RESULT_OK){
                        Uri targetUri = result.getData().getData();
                        Bitmap bitmap;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                            thisProfileImage.setImageBitmap(bitmap);
                            tempPhotoUri = targetUri;
                        } catch (FileNotFoundException e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Toast.makeText(this, storageDirectory.getPath(), Toast.LENGTH_SHORT).show();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDirectory
        );
        tempImageFileLocation = image.getAbsolutePath();
        return image;
    }

    private Bitmap setReducedImageSize() {
        int targetImageViewWidth = thisProfileImage.getWidth();
        int targetImageViewHeight = thisProfileImage.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(tempImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(
                cameraImageWidth / targetImageViewWidth,
                cameraImageHeight / targetImageViewHeight
        );

        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(tempImageFileLocation, bmOptions);
    }

    private void rotateImage(Bitmap source) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(tempImageFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
                // matrix.setRotate(0);
        }

        Bitmap rotateBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        thisProfileImage.setImageBitmap(rotateBitmap);
    }

    private void uploadProfilePicture() {
        if (thisProfileImage.getDrawable() != null && tempPhotoUri != null) {
            if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                final String USER_KEY = sharedPreferences.getString("current_user_KEY", null);
                if (USER_KEY != null) {
                    imageUploadProgress.setVisibility(View.VISIBLE);
                    thisProfileImage.setAlpha(0.5f);
                    changeProfileButton.setClickable(false);
                    rootReference.child(USER_KEY + "/profile/current_profile.jpg").putFile(tempPhotoUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Map<String, String> path = new HashMap<>();
                                    path.put("profilePath", taskSnapshot.getUploadSessionUri().toString());
                                    firestore.collection("Users").document(USER_KEY)
                                            .collection("profilePath").document("path").set(path)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    tempImageFileLocation = null;
                                                    tempPhotoUri = null;
                                                    imageUploadProgress.setVisibility(View.GONE);
                                                    thisProfileImage.setAlpha(1.0f);
                                                    changeProfileButton.setClickable(true);
                                                    Main.changeProfileSignature();
                                                    setProfileImage();
                                                    Toast.makeText(ProfileScreen.this, "successfully profile updated !", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    imageUploadProgress.setVisibility(View.GONE);
                                    thisProfileImage.setAlpha(1.0f);
                                    changeProfileButton.setClickable(true);
                                    Toast.makeText(ProfileScreen.this, "Unable to change profile due to " + e.getLocalizedMessage() + " !", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Please sign in or create new account !", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please check your internet connection ! Image cannot upload !", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please Upload Image first...", Toast.LENGTH_SHORT).show();
        }
    }
}
