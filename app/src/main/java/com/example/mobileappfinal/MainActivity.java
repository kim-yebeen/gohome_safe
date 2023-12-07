package com.example.mobileappfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Button button;
    SQLiteDatabase newDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(newDB==null){
            String dbName="new";
            openOrCreateDatabase(dbName, MODE_PRIVATE,null);
        }else{
            Toast.makeText(this,"test", Toast.LENGTH_LONG).show();
        }
        //newactivity로 가는 버튼
        Button btn_send = (Button) findViewById(R.id.button_login);
        btn_send.setOnClickListener(this);

        Button btn_singup=findViewById(R.id.button_sign);
        btn_singup.setOnClickListener(this);
    }

    public void onClick(View v){
        if(v.getId()==R.id.button_login) {
            //이름
            EditText et_name = (EditText) findViewById(R.id.edit_name);
            String str_name = et_name.getText().toString();

            //비밀번호
            EditText et_password = (EditText) findViewById(R.id.edit_password);
            String str_pw = et_password.getText().toString();

            //DBManager를 통해 DB에 저장된 회원 정보와 비교, 로그인 처리
            DBManager dbmgr = new DBManager(this);
            SQLiteDatabase sdb=dbmgr.getWritableDatabase();

            //입력한 이름과 비밀번호를 사용해 회원 정보 조회
            Cursor cursor = sdb.rawQuery("SELECT*FROM persons WHERE name =?AND password = ?", new String[]{str_name,str_pw});

            //로그인 성공
            if(cursor.moveToFirst()){
                Toast.makeText(this, "안녕하세요", Toast.LENGTH_SHORT).show();
                //액티비티 이동
                Intent it = new Intent(this, NewActivity.class);
                startActivity(it);
            }else {
                Toast.makeText(this,"일치하는 회원 정보가 없습니다.",Toast.LENGTH_SHORT).show();
            }
            cursor.close();
            sdb.close();
        }else if(v.getId()==R.id.button_sign){
            Intent it2 = new Intent(this, SignActivity.class);
            startActivity(it2);
        }

    }

}
