package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.touristinrussia.databinding.ActivityAllPlacesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AllPlacesActivity extends AppCompatActivity {
    ActivityAllPlacesBinding binding;
    private PlaceAdapter adapter;
    private List<Place> places;
    final private String PLACE_KEY = "Place";
    FirebaseUser user;
    final private String USER_KEY = "User";
    private DatabaseReference mDataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        places = new ArrayList<>();
        adapter = new PlaceAdapter(this, places);
        binding.recyclerView.setAdapter(adapter);

        // Подключение к Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(PLACE_KEY);

        isAdmin();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                places.clear();
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Place place = snapshot.getValue(Place.class);
                        places.add(place);
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
        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllPlacesActivity.this, AddPlaceActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
    private void isAdmin(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        if(user == null){
            binding.addButton.setVisibility(View.GONE);
        } else {
            mDataBase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User userFromDB = dataSnapshot.getValue(User.class);
                    if (userFromDB != null) {
                        binding.addButton.setVisibility(userFromDB.isAdmin() ? View.VISIBLE : View.GONE);
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
        getSupportActionBar().setTitle("Достопримечательности");
        menu.findItem(R.id.info).setVisible(false);
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
    @Override
    public void finish() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
