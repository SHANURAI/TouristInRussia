package com.example.touristinrussia;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
    private static final String CHANNEL_ID = "default_channel";
    ActivityPlaceDetailsBinding binding;
    String name = "";
    String placeId = "";
    String activity = "";
    FirebaseUser user;
    Place place;
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
                place = dataSnapshot.getValue(Place.class);
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
                        showNotification("Избранное", place.getName() + " теперь в " +
                                "избранном! Проверьте страницу профиля");
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
        binding.buttonToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra("latitude", place.getLatitude());
                intent.putExtra("longitude", place.getLongitude());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
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
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    // Метод для отображения уведомления
    private void showNotification(String title, String message) {
        // Создаем канал уведомлений (необходимо для Android 8.0 и выше)
        createNotificationChannel();
        Intent intent = new Intent(this, FavoritePlacesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE нужен для Android 12 и выше
        );
        // Создаем объект уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_icon) // Убедитесь, что иконка существует
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon)); // Убедитесь, что иконка существует

        // Получаем экземпляр менеджера уведомлений
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Отображаем уведомление
        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }
    // Метод для создания канала уведомлений (для Android 8.0 и выше)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Channel for default notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Регистрация канала с системой
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}