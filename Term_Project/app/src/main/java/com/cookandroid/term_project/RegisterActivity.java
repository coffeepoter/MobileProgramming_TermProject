package com.cookandroid.term_project;
import android.content.Intent;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etBirthdate, etId, etPassword, etConfirmPassword;
    private TextView tvPasswordHint;
    private RadioGroup rgGender;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // View 연결
        etName = findViewById(R.id.et_name);
        etBirthdate = findViewById(R.id.et_birthdate);
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvPasswordHint = findViewById(R.id.tv_password_hint); // 비밀번호 힌트
        rgGender = findViewById(R.id.rg_gender);
        btnRegister = findViewById(R.id.btn_register);

        // 생년월일 선택
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // 비밀번호 입력에 따른 경고 문구 표시
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 사용하지 않음
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 사용하지 않음
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (password.length() >= 8 && password.length() <= 16) {
                    tvPasswordHint.setVisibility(View.GONE); // 조건 충족 시 숨김
                } else {
                    tvPasswordHint.setVisibility(View.VISIBLE); // 조건 불충족 시 표시
                }
            }
        });

        // 회원가입 버튼 클릭
        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    etBirthdate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!password.equals(confirmPassword)) {
            showAlert("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 회원가입 완료 메시지
        showAlert("회원가입이 완료되었습니다. 환영합니다 " + name + "님!");

        // map_main.xml로 이동
        Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }


    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }
}
