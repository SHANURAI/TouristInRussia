package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.touristinrussia.databinding.ActivityAllPlacesBinding;
import com.example.touristinrussia.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String placeId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fetchRandomPlace();

        binding.buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.buttonAllPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AllPlacesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.layoutPlaceOfTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlaceDetailsActivity.class);
                intent.putExtra("placeId", placeId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void fetchRandomPlace() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Place");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Place> places = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Place place = snapshot.getValue(Place.class);
                    places.add(place);
                }

                if (!places.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(places.size());
                    Place randomPlace = places.get(randomIndex);
                    displayPlaceOfTheDay(randomPlace);
                } else {
                    displayPlaceOfTheDay(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }
    private void displayPlaceOfTheDay(Place place) {
        if(place != null) {
            placeId = place.getId();
            binding.nameOfPlaceOfTheDay.setText(place.getName());
            Picasso.get().load(place.getImageUri()).into(binding.imagePlaceOfTheDay);
        } else {
            binding.nameOfPlaceOfTheDay.setText("Неизвестно");
            binding.layoutPlaceOfTheDay.setClickable(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setTitle("Турист в России");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}