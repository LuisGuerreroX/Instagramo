package com.altice.hojuelita.instagramo;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import com.google.android.gms.location.LocationServices;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class FragmentReport extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private View rootView;
    private Bitmap picture;

    //Noticia noticia = new Noticia();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_report, container, false);

        FusedLocationProviderClient mFusedLocationClient;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;

        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            final TextView locationTv = rootView.findViewById(R.id.location);
                            final ImageView mImageView = rootView.findViewById(R.id.thumbnail);
                            final Button removePicBtn = rootView.findViewById(R.id.remove_pic_btn);
                            ImageButton locationBtn = rootView.findViewById(R.id.locationBtn);
                            locationBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String lc = "Longitude: " +
                                            String.valueOf(location.getLongitude()) +
                                            ", Latitude: " + String.valueOf(location.getLatitude());
                                    locationTv.setText(lc);
                                    if (mImageView.getVisibility() == View.VISIBLE){
                                        mImageView.setVisibility(View.GONE);
                                        removePicBtn.setVisibility(View.GONE);
                                        locationTv.setVisibility(View.VISIBLE);
                                        mImageView.setVisibility(View.VISIBLE);
                                        removePicBtn.setVisibility(View.VISIBLE);
                                    } else {
                                        locationTv.setVisibility(View.VISIBLE);
                                    }

                                }
                            });
                        }
                    }
                });
        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        final TextView location = rootView.findViewById(R.id.location);
        final EditText descripcion = rootView.findViewById(R.id.editText);
        final ImageView thumbnail = rootView.findViewById(R.id.thumbnail);
        final ImageButton camera = rootView.findViewById(R.id.cameraBtn);
        final Button removeThumbnailBtn = rootView.findViewById(R.id.remove_pic_btn);


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        final Button submit = rootView.findViewById(R.id.submitBtn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (picture != null && !location.getText().toString().isEmpty() &&
                        !descripcion.getText().toString().isEmpty()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    picture.compress(Bitmap.CompressFormat.PNG, 100,baos);
                    byte[] data = baos.toByteArray();
                    String path = "Noticias/" + UUID.randomUUID() + ".png";
                    final StorageReference noticiasRef = storage.getReference(path);
                    final ProgressDialog loading = new ProgressDialog(getActivity());
                    loading.setMessage(getString(R.string.please_wait));
                    loading.setCanceledOnTouchOutside(false);
                    loading.show();
                    submit.setEnabled(false);

                    final UploadTask uploadTask = noticiasRef.putBytes(data);
                    uploadTask.addOnSuccessListener(getActivity(),
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("unused")
                            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        loading.dismiss();
                                        loading.setMessage(getString(R.string.error_occurred));
                                        loading.setCanceledOnTouchOutside(true);
                                        loading.show();
                                    }

                                    // Continue with the task to get the download URL
                                    return noticiasRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Map<String, Object> noticia = new HashMap<>();
                                        noticia.put("Location", location.getText().toString());
                                        noticia.put("Description", descripcion.getText().toString());
                                        noticia.put("URL",downloadUri.toString());

                                        loading.dismiss();
                                        loading.setMessage("Posting...");
                                        loading.show();

                                        // Add a new document with a generated ID
                                        db.collection("Noticias")
                                                .add(noticia)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        location.clearComposingText();
                                                        location.setVisibility(View.GONE);
                                                        thumbnail.setImageResource(0);
                                                        thumbnail.setVisibility(View.GONE);
                                                        removeThumbnailBtn.setVisibility(View.GONE);
                                                        camera.setVisibility(View.VISIBLE);
                                                        descripcion.setText("");
                                                        loading.dismiss();
                                                        submit.setEnabled(true);
                                                        Toast toast = Toast.makeText(getActivity(),
                                                                getString(R.string.post_complete),Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding document", e);
                                                        submit.setEnabled(true);
                                                        loading.dismiss();
                                                        Toast toast = Toast.makeText(getActivity(),
                                                                getString(R.string.error_occurred),Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }
                                                });
                                    } else {
                                        // Handle failures
                                        submit.setEnabled(true);
                                        loading.dismiss();
                                        Toast toast = Toast.makeText(getActivity(),
                                                getString(R.string.error_occurred),Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(getActivity(),
                            getString(R.string.fill_camps),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ImageView mImageView = rootView.findViewById(R.id.thumbnail);
        final ImageButton camera = rootView.findViewById(R.id.cameraBtn);
        final Button removePicBtn = rootView.findViewById(R.id.remove_pic_btn);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                picture = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
                camera.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                removePicBtn.setVisibility(View.VISIBLE);
            }

            removePicBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removePicBtn.setVisibility(View.GONE);
                    mImageView.setVisibility(View.GONE);
                    camera.setVisibility(View.VISIBLE);
                    picture = null;
                }
            });
        }
    }

}
