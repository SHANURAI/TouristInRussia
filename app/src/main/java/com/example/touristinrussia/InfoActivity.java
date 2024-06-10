package com.example.touristinrussia;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.touristinrussia.databinding.ActivityInfoBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class InfoActivity extends AppCompatActivity {
    ActivityInfoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FragmentStateAdapter pageAdapter = new InfoAdapter(this);
        binding.pager.setAdapter(pageAdapter);
        TabLayoutMediator tabLayoutMediator= new TabLayoutMediator(binding.tabLayout,
                binding.pager, (tab, position) -> {
            if (position == 0) {
                tab.setText("О приложении");
            }
            else if (position == 1) {
                tab.setText("Об авторе");
            }
            else if (position == 2) {
                tab.setText("Инструкция");
            }
        });
        tabLayoutMediator.attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("О приложении");
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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}