package com.cookandroid.term_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button mapBtn, loginBtn;
    EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 버튼 및 입력 필드 초기화
        mapBtn = (Button) findViewById(R.id.map);
        loginBtn = (Button) findViewById(R.id.login);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);

        // 로그인 버튼 클릭 리스너 설정
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // 간단한 로그인 검증 (예: 아이디 "pknu"와 비밀번호 "1234"일 때 성공)
                if (username.equals("pknu") && password.equals("1234")) {
                    Toast.makeText(MainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    // 지도 화면으로 이동
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(intent);
                } else {
                    // 로그인 실패 메시지
                    Toast.makeText(MainActivity.this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 지도 버튼 클릭 리스너 설정
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
