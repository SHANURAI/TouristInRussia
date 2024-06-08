package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.touristinrussia.databinding.ActivityAllPlacesBinding;
import com.example.touristinrussia.databinding.ActivityFavoritePlacesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritePlacesActivity extends AppCompatActivity {
    ActivityFavoritePlacesBinding binding;
    private FavoritePlaceAdapter adapter;
    private List<Place> places;
    final private String PLACE_KEY = "Place";
    FirebaseUser user;
    final private String USER_KEY = "User";
    private DatabaseReference mDataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritePlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = FirebaseAuth.getInstance().getCurrentUser();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        places = new ArrayList<>();
        adapter = new FavoritePlaceAdapter(this, places);
        binding.recyclerView.setAdapter(adapter);

        // Подключение к Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference =  database.getReference(USER_KEY)
                .child(user.getUid()).child("favorites");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                places.clear();
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String placeId = snapshot.getValue(String.class); // Получаем строку с идентификатором места
                        if (placeId != null) {
                            // Извлекаем объект типа Place на основе идентификатора места
                            DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference(PLACE_KEY).child(placeId);
                            placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot placeSnapshot) {
                                    if (placeSnapshot.exists()) {
                                        Place place = placeSnapshot.getValue(Place.class);
                                        if (place != null) {
                                            places.add(place);
                                            adapter.notifyDataSetChanged();
                                            binding.recyclerView.setVisibility(View.VISIBLE);
                                            binding.emptyTextView.setVisibility(View.GONE);
                                        }
                                    } else {
                                        binding.recyclerView.setVisibility(View.GONE);
                                        binding.emptyTextView.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Обработка ошибок при чтении данных
                                }
                            });
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyTextView.setVisibility(View.GONE);
                } else {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyTextView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setTitle("Избранные места");
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        super.onBackPressed();
            startActivity(new Intent(this, ProfileActivity.class));
    }
}