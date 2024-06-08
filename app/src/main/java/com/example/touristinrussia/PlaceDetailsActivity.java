package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

import com.example.touristinrussia.databinding.ActivityPlaceDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailsActivity extends AppCompatActivity {
    private DatabaseReference mDataBase;
    ActivityPlaceDetailsBinding binding;
    String name = "";
    String placeId = "";
    String activity = "";
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkIfFavorite();
        // Получение информации о достопримечательности из Intent
        placeId = getIntent().getStringExtra("placeId");
        activity = getIntent().getStringExtra("activity");
        // Запрос информации о достопримечательности из базы данных Firebase и отображение на экране
        mDataBase = FirebaseDatabase.getInstance().getReference("Place").child(placeId);
        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Place place = dataSnapshot.getValue(Place.class);
                if (place != null) {
                    name = place.getName();
                    getSupportActionBar().setTitle(place.getName());
                    Picasso.get().load(place.getImageUri()).into(binding.imageViewPlace);
                    binding.textViewPlaceName.setText(place.getName());
                    binding.textViewCityName.setText(place.getCity());
                    binding.textViewDescription.setText(place.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при получении данных из базы данных Firebase
                Log.e("PlaceDetailsActivity", "Error fetching place details", databaseError.toException());
            }
        });
        binding.addToFavorites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
            DatabaseReference favoriteRef = userRef.child("favorites");

            favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> favorites = (List<String>) dataSnapshot.getValue();
                    if (favorites == null) {
                        favorites = new ArrayList<>();
                    }
                    if (favorites.contains(placeId) && !isChecked) {
                        favorites.remove(placeId);
                        Toast.makeText(getApplicationContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    } else if (!favorites.contains(placeId) && isChecked){
                        favorites.add(placeId);
                        Toast.makeText(getApplicationContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    }
                    userRef.child("favorites").setValue(favorites);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Favorite", "Error toggling favorite status", databaseError.toException());
                }
            });
        });
    }

    private void checkIfFavorite() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.addToFavorites.setVisibility(View.GONE);

            return;
        }

        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("User")
                .child(user.getUid()).child("favorites");

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favorites = (List<String>) dataSnapshot.getValue();
                binding.addToFavorites.setChecked(favorites != null && favorites.contains(placeId));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("favorites", "Ошибка проверки избранности", databaseError.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        if(name.isEmpty()) name = "Достопримечательность";
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(activity.equals("AllPlacesActivity")){
                startActivity(new Intent(this, AllPlacesActivity.class));
            } else if (activity.equals("FavoritePlacesActivity")){
                startActivity(new Intent(this, FavoritePlacesActivity.class));
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (activity.equals("AllPlacesActivity")) {
            startActivity(new Intent(this, AllPlacesActivity.class));
        } else if (activity.equals("FavoritePlacesActivity")) {
            startActivity(new Intent(this, FavoritePlacesActivity.class));
        } else if (activity.equals("MainActivity")){
            startActivity(new Intent(this, MainActivity.class));
        }
    }

}