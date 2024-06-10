package com.example.touristinrussia;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;
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

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>{
    private List<Place> places;
    private static final String CHANNEL_ID = "default_channel";
    private Context context;
    final private String PLACE_KEY = "Place";
    final private String USER_KEY = "User";
    FirebaseUser user;
    private DatabaseReference mDataBase;

    public PlaceAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.nameTextView.setText(place.getName());
        holder.locationTextView.setText(place.getCity());
        Picasso.get().load(place.getImageUri()).into(holder.imageView);

        // Обработка нажатий на элементы списка
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Передача информации о выбранной достопримечательности в новую активность
                Intent intent = new Intent(context, PlaceDetailsActivity.class);
                intent.putExtra("placeId", place.getId());
                intent.putExtra("activity", "AllPlacesActivity");
                context.startActivity(intent);
                // Анимация перехода
                if (context instanceof Activity) {
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditPlaceActivity.class);
            intent.putExtra("placeId", place.getId());
            context.startActivity(intent);
            // Анимация перехода
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                deletePlace(adapterPosition);
            }
        });

        isAdmin(holder);
        checkIfFavorite(holder, place);

        holder.favoriteButton.setOnClickListener(v -> {
            toggleFavorite(holder, place);
        });
    }
    private void isAdmin(PlaceViewHolder holder){
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        if(user == null){
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            mDataBase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User userFromDB = dataSnapshot.getValue(User.class);
                    if (userFromDB != null) {
                        holder.editButton.setVisibility(userFromDB.isAdmin() ? View.VISIBLE :
                                View.GONE);
                        holder.deleteButton.setVisibility(userFromDB.isAdmin() ?
                                View.VISIBLE : View.GONE);
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
    public int getItemCount() {
        return places.size();
    }

    private void deletePlace(int position) {
        Place place = places.get(position);
        DatabaseReference reference =
                FirebaseDatabase.getInstance().getReference(PLACE_KEY).child(place.getId());
        reference.removeValue();
    }

    private void checkIfFavorite(PlaceViewHolder holder, Place place) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            holder.favoriteButton.setVisibility(View.GONE);
            return;
        }

        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("User")
                .child(user.getUid()).child("favorites");

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favorites = (List<String>) dataSnapshot.getValue();
                boolean isFavorite = favorites != null && favorites.contains(place.getId());
                holder.favoriteButton.setImageResource(isFavorite ? R.drawable.ic_favorite_filled :
                        R.drawable.ic_favorite_empty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Favorite", "Error checking favorite status", databaseError.toException());
            }
        });
    }

    private void toggleFavorite(PlaceViewHolder holder, Place place) {
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
                if (favorites.contains(place.getId())) {
                    favorites.remove(place.getId());
                    holder.favoriteButton.setImageResource(R.drawable.ic_favorite_empty);
                    Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                } else {
                    favorites.add(place.getId());
                    showNotification("Избранное", place.getName() + " теперь в " +
                            "избранном! Проверьте страницу профиля");
                    holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                    Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                }

                userRef.child("favorites").setValue(favorites).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("Favorite", "Error updating favorites", task.getException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Favorite", "Error toggling favorite status", databaseError.toException());
            }
        });
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageButton deleteButton, editButton, favoriteButton;
        TextView nameTextView;
        TextView locationTextView;
        ImageView imageView;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);

        }
    }

    // Метод для отображения уведомления
    private void showNotification(String title, String message) {
        // Создаем канал уведомлений (необходимо для Android 8.0 и выше)
        createNotificationChannel();
        Intent intent = new Intent(context, FavoritePlacesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE нужен для Android 12 и выше
        );
        // Создаем объект уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_icon) // Убедитесь, что иконка существует
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.app_icon));
        // Убедитесь, что иконка существует

        // Получаем экземпляр менеджера уведомлений
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
