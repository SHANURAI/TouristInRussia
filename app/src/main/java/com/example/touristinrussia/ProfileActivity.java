package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.touristinrussia.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    final private String USER_KEY = "User";
    private DatabaseReference mDataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user == null) {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(ProfileActivity.this, "Выполнен выход из аккаунта",
                            Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finishAffinity();
                }
            }
        });
        binding.buttonFavoritePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, FavoritePlacesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        if(user == null){
            binding.userImageView.setVisibility(View.GONE);
            binding.textViewUsername.setText("Вы вошли в режиме гостя");
            binding.textViewEmail.setVisibility(View.GONE);
            binding.textViewIsAdmin.setVisibility(View.GONE);
            binding.buttonEditProfile.setVisibility(View.GONE);
            binding.buttonFavoritePlaces.setVisibility(View.GONE);
            binding.buttonLogout.setText("Войти/Зарегистрироваться");
        } else {
            mDataBase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User userFromDB = dataSnapshot.getValue(User.class);
                    if (userFromDB != null) {
                        Picasso.get().load(userFromDB.getImageUri()).into(binding.userImageView);
                        binding.textViewUsername.setText(userFromDB.getName());
                        binding.textViewEmail.setText(userFromDB.getEmail());
                        binding.textViewIsAdmin.setText(userFromDB.isAdmin() ? "Администратор" :
                                "Пользователь");
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("MyLogs", "Ошибка при чтении данных", error.toException());
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("Профиль");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}