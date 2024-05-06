package com.example.touristinrussia;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.touristinrussia.databinding.ActivityLoginBinding;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Обработка нажатия кнопки "Войти"
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Обработка нажатия текста "Зарегистрироваться"
        binding.textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        // Обработка нажатия текста "Войти как гость"
        binding.textViewLoginAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Выполнен вход как гость", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    // Метод для входа пользователя
    private void loginUser() {
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

        // Здесь вы можете добавить логику для проверки введенных данных и входа пользователя
        // Например, проверить email и пароль в базе данных или другом источнике данных

        // В данном примере просто выведем сообщение об успешном входе
        Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();

        // После успешного входа можно перейти на другую активность
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Закрыть активность входа, чтобы пользователь не мог вернуться к ней
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setTitle("Турист в России");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));
        return super.onCreateOptionsMenu(menu);
    }
}
