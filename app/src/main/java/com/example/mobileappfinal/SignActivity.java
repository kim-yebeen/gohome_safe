package com.example.mobileappfinal;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        Button btn_send = (Button) findViewById(R.id.button_singup);
        btn_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        //회원가입 로직 구현
        if (v.getId() == R.id.button_singup) {
            EditText et_name = findViewById(R.id.edit_name);
            String str_name = et_name.getText().toString();

            EditText et_password = findViewById(R.id.edit_password);
            String str_pw = et_password.getText().toString();

            EditText et_passwordCheck = findViewById(R.id.edit_passwordcheck);
            String str_pwCheck = et_passwordCheck.getText().toString();

            EditText et_num = findViewById(R.id.edit_num);
            String str_num = et_num.getText().toString();

            EditText et_egnum = findViewById(R.id.edit_egnum);
            String str_egnum = et_egnum.getText().toString();

            //비밀번호와 비밀번호 확인이 일치하는지 검사
            if (!str_pw.equals(str_pwCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            //회원 정보 DB에 저장
            DBManager dbmgr = new DBManager(this);
            SQLiteDatabase sdb = dbmgr.getWritableDatabase();
            sdb.execSQL("INSERT INTO persons VALUES('" + str_name + "', '" + str_num + "', '" + str_pw + "', '" + str_egnum + "')");

            Toast.makeText(this, "회원 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();

            // 로그인으로 이동
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);

        }
    }
}