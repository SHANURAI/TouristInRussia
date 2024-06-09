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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.touristinrussia.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends AppCompatActivity {
    private ObjectAnimator rotateAnimator;
    private FirebaseAuth mAuth;
    private ActivityRegisterBinding binding;
    private DatabaseReference mDataBase;
    final private String USER_KEY = "User";
    private StorageReference mStorageRef;
    public Uri uploadUri = Uri.parse("https://firebasestorage.googleapis.com" +
            "/v0/b/touristinrussia2024.appspot.com/o/DefaultAvatar%2Favatar_default" +
            ".jpg?alt=media&token=0f621007-407c-4c3d-b85e-f7dd7703ad61");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBarLayout.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        mStorageRef = FirebaseStorage.getInstance().getReference("Avatar");
        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        binding.buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
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


    private void registerUser() {
        String name = binding.editTextName.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.editTextEmail.setError("Введите ваш email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Введите пароль");
            return;
        }
        startAnimation();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser(); // Получение объекта FirebaseUser
                            String userId = firebaseUser.getUid(); // Получение UID пользователя
                            List<String> stringList = new ArrayList<String>();
                            User newUser = new User(uploadUri.toString(), name,
                                    email, false, stringList);
                            mDataBase.child(userId).setValue(newUser);
                            Toast.makeText(getApplicationContext(), "Регистрация выполнена успешно", Toast.LENGTH_SHORT).show();
                            loginUser(email, password);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Регистрация не выполнена", Toast.LENGTH_SHORT).show();
                        }
                        finishAnimation();
                    }
                });
            }
        }, 3000);

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finishAffinity();
                        } else {
                            Toast.makeText(getApplicationContext(), "Вход не выполнен",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void startAnimation(){
        binding.buttonChooseImage.setEnabled(false);
        binding.editTextName.setEnabled(false);
        binding.editTextEmail.setEnabled(false);
        binding.editTextPassword.setEnabled(false);
        binding.buttonRegister.setEnabled(false);
        rotateAnimator = ObjectAnimator.ofFloat(binding.progressBar, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.start();
        binding.progressBarLayout.setVisibility(View.VISIBLE);
    }
    private void finishAnimation(){
        rotateAnimator.cancel();
        binding.progressBarLayout.setVisibility(View.GONE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("Регистрация");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}