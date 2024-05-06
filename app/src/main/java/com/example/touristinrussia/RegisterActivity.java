package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.touristinrussia.databinding.ActivityRegisterBinding;


public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Обработка нажатия кнопки "Зарегистрироваться"
        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // Метод для регистрации пользователя
    private void registerUser() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.editTextEmail.setError("Введите ваш email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Введите пароль");
            return;
        }

        // Здесь вы можете добавить логику для регистрации пользователя
        // Например, отправить данные на сервер или сохранить в локальном хранилище

        // В данном примере просто выведем сообщение об успешной регистрации
        Toast.makeText(this, "Регистрация выполнена успешно", Toast.LENGTH_SHORT).show();

        // После успешной регистрации можно перейти на активность входа
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish(); // Закрыть активность регистрации
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.info).setVisible(false);
        getSupportActionBar().setTitle("Регистрация");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}