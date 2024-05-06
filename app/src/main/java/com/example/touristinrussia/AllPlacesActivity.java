package com.example.touristinrussia;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Magnifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.touristinrussia.databinding.ActivityAllPlacesBinding;

public class AllPlacesActivity extends AppCompatActivity {
    ActivityAllPlacesBinding binding;
    MapFragment mapFragment;
    PlacesListFragment placesListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.checkBoxShowMap.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, isChecked ?
                            new MapFragment() : new PlacesListFragment())
                    .commit();
        });
        // По умолчанию загружаем фрагмент со списком мест
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new PlacesListFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setTitle("Турист в России");
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
