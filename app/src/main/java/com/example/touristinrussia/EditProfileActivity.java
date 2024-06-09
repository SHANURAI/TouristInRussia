package com.example.touristinrussia;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.touristinrussia.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;
    private ObjectAnimator rotateAnimator;
    private DatabaseReference mDataBase;
    boolean admin = false;
    FirebaseUser user;
    public Uri uploadUri = Uri.parse("https://firebasestorage.googleapis.com" +
            "/v0/b/touristinrussia2024.appspot.com/o/DefaultAvatar%2Favatar_default" +
            ".jpg?alt=media&token=0f621007-407c-4c3d-b85e-f7dd7703ad61");
    private StorageReference mStorageRef;
    final private String USER_KEY = "User";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = FirebaseAuth.getInstance().getCurrentUser();
        binding.progressBarLayout.setVisibility(View.GONE);
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        mStorageRef = FirebaseStorage.getInstance().getReference("Avatar");
        init();
        binding.buttonSave.setOnClickListener(v -> saveUser());
        binding.buttonChooseImage.setOnClickListener(v -> chooseImage());
        binding.buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        mDataBase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userFromDB = dataSnapshot.getValue(User.class);
                if (userFromDB != null) {
                    Picasso.get().load(userFromDB.getImageUri()).into(binding.userImageView);
                    binding.editTextName.setText(userFromDB.getName());
                    admin = userFromDB.isAdmin();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("MyLogs", "Ошибка при чтении данных", error.toException());
            }
        });

    }


    private void saveUser() {
        String name = binding.editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            binding.editTextName.setError("Введите название достопримечательности");
            return;
        }
        startAnimation();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                User updatedUser = new User(uploadUri.toString(), name, user.getEmail(), admin,
                        new ArrayList<String>());
                mDataBase.child(user.getUid()).setValue(updatedUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Профиль обновлен",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
                    }
                });
                finishAnimation();
            }
        }, 3000);

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
                binding.userImageView.setImageURI(data.getData());
                uploadImage();
            }
        }
    }
    private void uploadImage(){
        Bitmap bitmap = ((BitmapDrawable) binding.userImageView.getDrawable()).getBitmap();
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
    private void startAnimation(){
        binding.editTextName.setEnabled(false);
        binding.buttonChooseImage.setEnabled(false);
        binding.buttonSave.setEnabled(false);
        binding.buttonChangePassword.setEnabled(false);
        rotateAnimator = ObjectAnimator.ofFloat(binding.progressBar, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.start();
        binding.progressBarLayout.setVisibility(View.VISIBLE);
    }
    private void finishAnimation(){
        binding.editTextName.setEnabled(true);
        binding.buttonChooseImage.setEnabled(true);
        binding.buttonSave.setEnabled(true);
        binding.buttonChangePassword.setEnabled(true);
        rotateAnimator.cancel();
        binding.progressBarLayout.setVisibility(View.GONE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("Редактирование профиля");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}