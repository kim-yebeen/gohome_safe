package com.example.mobileappfinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.URI;

public class alarm extends AppCompatActivity implements View.OnClickListener
{

    private static final long MAX_PLAYBACK_DURATION = 1 * 60 * 1000; // 3분(밀리초 단위)

    private MediaPlayer mp;
    private String password;
    private Handler handler;
    private boolean isPlaying;
    private long playbackStartTime;


    private AlertDialog passwordDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //DBMangager객체 생성
        DBManager dbmgr = new DBManager(this);

        //SQLiteDB생성
        SQLiteDatabase sdb = dbmgr.getWritableDatabase();

        Cursor cursor = sdb.rawQuery("select name, password from persons",null);
        String dbPassword = null;
        String dbName = null;

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex("name");
            int passwordColumnIndex = cursor.getColumnIndex("password");
            if (nameColumnIndex != -1 && passwordColumnIndex != -1) {
                dbName = cursor.getString(nameColumnIndex);
                dbPassword = cursor.getString(passwordColumnIndex);
            } else {
                // 컬럼이 존재하지 않는 경우에 대한 처리
                Toast.makeText(alarm.this,"컬럼이 존재하지 않음",Toast.LENGTH_LONG).show();
            }
        }
        cursor.close();

        mp = MediaPlayer.create(this, R.raw.bibidibabidibuu);
        handler = new Handler();
        isPlaying = false;

        Button playButton = findViewById(R.id.stopButton);
        playButton.setOnClickListener(this);

        startPlayback();

        androidx.appcompat.app.AlertDialog.Builder passwordDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
        passwordDialogBuilder.setTitle("비밀번호를 입력하세요");
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        passwordDialogBuilder.setView(passwordInput);
        String finalDbPassword = dbPassword;
        passwordDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = passwordInput.getText().toString().trim();

                if (finalDbPassword != null) {
                    if (enteredPassword.equals(finalDbPassword)) {

                        startNewActivity(); // 비밀번호 일치 시 타이머 멈추기
                    } else {
                        Toast.makeText(alarm.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(alarm.this, "비밀번호를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        passwordDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        passwordDialog = passwordDialogBuilder.create();
    }

    @Override
    public void onClick(View v) {
        if (mp.isPlaying()) {
            // 알람 중지를 위한 비밀번호 입력 다이얼로그 표시
            passwordDialog.show();
        } else {
            mp.start();
        }
    }

    private void startPlayback() {
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (System.currentTimeMillis() - playbackStartTime < MAX_PLAYBACK_DURATION) {
                    mp.seekTo(0);
                    mp.start();
                } else{
                    openDialerWithNumber();
                }
            }
        });

        mp.start();
        playbackStartTime = System.currentTimeMillis();
        isPlaying = true;
    }

    private void openDialerWithNumber() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel: 112"));

        if (dialIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(dialIntent);
        } else {
            Toast.makeText(this, "전화 걸기 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewActivity() {
        Intent intent = new Intent(this, NewActivity.class);
        startActivity(intent);
        finish();
    }
}