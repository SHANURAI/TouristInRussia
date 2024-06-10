package com.example.touristinrussia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

public class FavoritePlaceAdapter extends RecyclerView.Adapter<FavoritePlaceAdapter.PlaceViewHolder>{
    private List<Place> places;
    private Context context;
    FirebaseUser user;

    public FavoritePlaceAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.nameTextView.setText(place.getName());
        holder.locationTextView.setText(place.getCity());
        Picasso.get().load(place.getImageUri()).into(holder.imageView);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Обработка нажатий на элементы списка
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Передача информации о выбранной достопримечательности в новую активность
                Intent intent = new Intent(context, PlaceDetailsActivity.class);
                intent.putExtra("placeId", place.getId());
                intent.putExtra("activity", "FavoritePlacesActivity");
                context.startActivity(intent);
                // Анимация перехода
                if (context instanceof Activity) {
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });


        holder.removeFavoriteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                deletePlaceFromFavorite(adapterPosition);
            }
        });
    }

    private void deletePlaceFromFavorite(int position) {
        Place place = places.get(position);
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
                    Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                }
                userRef.child("favorites").setValue(favorites);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Favorite", "Ошибка удаления из избранного", databaseError.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView locationTextView;
        ImageButton removeFavoriteButton;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            removeFavoriteButton = itemView.findViewById(R.id.removeFavoriteButton);
        }
    }
}
