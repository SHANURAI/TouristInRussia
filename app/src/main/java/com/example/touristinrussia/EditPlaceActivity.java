package com.example.touristinrussia;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.touristinrussia.databinding.ActivityEditPlaceBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class EditPlaceActivity extends AppCompatActivity {
    ActivityEditPlaceBinding binding;
    private ObjectAnimator rotateAnimator;
    private String placeId;
    private DatabaseReference mDataBase;
    public Uri uploadUri = Uri.parse("https://firebasestorage" +
            ".googleapis.com/v0/b/touristinrussia2024.appspot.com/o/DefaultPlace%2" +
            "FThe-Good-Place.png?alt=media&token=475d31c0-9bed-4e87-b9a2-c093cc03cd13");
    private StorageReference mStorageRef;
    final private String PLACE_KEY = "Place";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBarLayout.setVisibility(View.GONE);
        placeId = getIntent().getStringExtra("placeId");
        mDataBase = FirebaseDatabase.getInstance().getReference(PLACE_KEY).child(placeId);
        mStorageRef = FirebaseStorage.getInstance().getReference(PLACE_KEY);
        loadPlaceData();
        binding.saveButton.setOnClickListener(v -> savePlace());
        binding.buttonChooseImage.setOnClickListener(v -> chooseImage());
    }
    private void chooseImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/*");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null && data.getData() != null){
            if(resultCode == RESULT_OK){
                binding.imageView.setImageURI(data.getData());
                uploadImage();
            }
        }
    }
    private void uploadImage(){
        Bitmap bitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        final StorageReference mRef = mStorageRef.child(System.currentTimeMillis() + "img");
        UploadTask uploadTask = mRef.putBytes(bytes);
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
            }
        });
    }
    private void loadPlaceData() {
        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Place place = dataSnapshot.getValue(Place.class);
                    if (place != null) {
                        binding.editTextName.setText(place.getName());
                        binding.editTextCity.setText(place.getCity());
                        binding.editTextDescription.setText(place.getDescription());
                        binding.editTextLatitude.setText(Double.toString(place.getLatitude()));
                        binding.editTextLongitude.setText(Double.toString(place.getLongitude()));
                        uploadUri = Uri.parse(place.getImageUri());
                        Picasso.get().load(place.getImageUri()).into(binding.imageView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditPlaceActivity.this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void savePlace() {
        String name = binding.editTextName.getText().toString().trim();
        String city = binding.editTextCity.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();
        double latitude = Double.parseDouble(binding.editTextLatitude.getText().toString().trim());
        double longitude = Double.parseDouble(binding.editTextLongitude.getText().toString().trim());
        if (TextUtils.isEmpty(name)) {
            binding.editTextName.setError("Введите название достопримечательности");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            binding.editTextCity.setError("Введите город достопримечательности");
            return;
        }
        if (TextUtils.isEmpty(binding.editTextLatitude.getText().toString().trim())
                || TextUtils.isEmpty(binding.editTextLongitude.getText().toString().trim())) {
            Toast.makeText(this, "Введите широту и долготу", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (latitude < -90.0 || latitude > 90.0) {
                Toast.makeText(this, "Широта должна быть в диапазоне от -90.0 до 90.0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (longitude < -180.0 || longitude > 180.0) {
                Toast.makeText(this, "Долгота должна быть в диапазоне от -180.0 до 180.0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректные значения широты и долготы", Toast.LENGTH_SHORT).show();
        }
        startAnimation();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Place updatedPlace = new Place(placeId, name, city, description,
                        uploadUri.toString(), latitude, longitude);
                mDataBase.setValue(updatedPlace).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditPlaceActivity.this, "Достопримечательность обновлена", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditPlaceActivity.this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
                    }
                });
                finishAnimation();
            }
        }, 3000);

    }

    private void startAnimation(){
        binding.editTextName.setEnabled(false);
        binding.editTextCity.setEnabled(false);
        binding.editTextDescription.setEnabled(false);
        binding.editTextLongitude.setEnabled(false);
        binding.editTextLatitude.setEnabled(false);
        binding.buttonChooseImage.setEnabled(false);
        binding.saveButton.setEnabled(false);
        rotateAnimator = ObjectAnimator.ofFloat(binding.progressBar, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.start();
        binding.progressBarLayout.setVisibility(View.VISIBLE);
    }
    private void finishAnimation(){
        binding.editTextName.setEnabled(true);
        binding.editTextCity.setEnabled(true);
        binding.editTextDescription.setEnabled(true);
        binding.editTextLongitude.setEnabled(true);
        binding.editTextLatitude.setEnabled(true);
        binding.buttonChooseImage.setEnabled(true);
        binding.saveButton.setEnabled(true);
        rotateAnimator.cancel();
        binding.progressBarLayout.setVisibility(View.GONE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("Редактирование достопримечательности");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, AllPlacesActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, AllPlacesActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAffinity();
    }
}