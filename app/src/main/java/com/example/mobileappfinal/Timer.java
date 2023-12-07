package com.example.mobileappfinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Timer extends AppCompatActivity implements View.OnClickListener {
    //타이머 시간
    private TextView textViewTimer;
    //시작 버튼
    private Button buttonStart;
    //스탑 버튼
    private Button buttonStop;
    //재시작 버튼
    private Button buttonRestart;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis;
    private long startTimeInMillis = 60000; // 1분
    private long pausedTimeInMillis = 0; //멈춤 시간 저장

    private String password="";
    private String name;

    private static final int DIALOG_DURATION = 60000; // 다이얼로그 지속 시간 (1분)
    private AlertDialog passwordDialog; // 멈춤 버튼을 클릭했을 때의 비밀번호 입력 다이얼로그
    private AlertDialog finishDialog; // 타이머가 종료되었을 때의 다이얼로그
    private CountDownTimer dialogCountDownTimer; // 다이얼로그 타이머

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

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
                Toast.makeText(Timer.this,"컬럼이 존재하지 않음",Toast.LENGTH_LONG).show();
            }
        }

        cursor.close();
        textViewTimer = findViewById(R.id.textview_timer);
        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);
        buttonRestart = findViewById(R.id.button_restart);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonRestart.setOnClickListener(this);

        // 예상 시간을 인텐트로부터 받아옵니다.
        Intent intent = getIntent();
        if (intent != null) {
            String duration = intent.getStringExtra("duration");
            if (duration != null) {
                int hours = Integer.parseInt(duration.split(":")[0]);
                int minutes = Integer.parseInt(duration.split(":")[1]);

                // 예상 시간을 밀리초로 변환하여 기본 타이머 세팅으로 설정합니다.
                timeLeftInMillis = (hours * 60 + minutes) * 60 * 1000;
            }
        }

        updateTimerText();



        // 멈춤 버튼을 클릭했을 때의 비밀번호 입력 다이얼로그 초기화
        AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(this);
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
                        stopTimer(); // 비밀번호 일치 시 타이머 멈추기
                    } else {
                        Toast.makeText(Timer.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Timer.this, "비밀번호를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
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

        // 타이머가 종료되었을 때의 다이얼로그 초기화
        AlertDialog.Builder finishDialogBuilder = new AlertDialog.Builder(this);
        finishDialogBuilder.setTitle("타이머 종료");
        finishDialogBuilder.setMessage("비밀번호를 입력하세요\n 비밀번호가 틀릴 경우 바로 경보음이 울립니다.");
        final EditText finishDialogInput = new EditText(this);
        finishDialogInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        finishDialogBuilder.setView(finishDialogInput);
        String finalDbpassword = dbPassword;
        finishDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = finishDialogInput.getText().toString().trim();
                if(finalDbPassword!=null){
                    if(enteredPassword.equals(finalDbPassword)){
                        Toast.makeText(Timer.this,"좋은 밤 되세요",Toast.LENGTH_SHORT).show();
                        startNewActivity();
                    }else{
                        Toast.makeText(Timer.this,"비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                        startAlarmActivity();
                    }
                }else{
                    Toast.makeText(Timer.this,"비밀번호를 불러올 수 없습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        finishDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        finishDialog = finishDialogBuilder.create();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                if (!timerRunning) {
                    startTimer();
                }
                break;
            case R.id.button_stop:
                if (timerRunning) {
                    passwordDialog.show();
                }
                break;
            case R.id.button_restart:
                if (!timerRunning) {
                    restartTimer();
                }
                break;
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateTimerText();
                showFinishDialog();
            }
        }.start();

        timerRunning = true;
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        buttonRestart.setEnabled(false);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        pausedTimeInMillis = timeLeftInMillis; // 현재 시간을 멈춘 시간으로 저장
        timerRunning = false;
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonRestart.setEnabled(true);
    }

    private void restartTimer() {
        timeLeftInMillis = pausedTimeInMillis > 0 ? pausedTimeInMillis : startTimeInMillis;
        pausedTimeInMillis = 0;
        updateTimerText();
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        buttonRestart.setEnabled(false);
        startTimer();
    }

    private void updateTimerText() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.KOREA, "%02d:%02d:%02d", hours, minutes, seconds);
        textViewTimer.setText(timeLeftFormatted);
    }

    private void showFinishDialog() {
        finishDialog.show();
        dialogCountDownTimer = new CountDownTimer(DIALOG_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                // 남은 시간 업데이트
            }

            public void onFinish() {
                finishDialog.dismiss();
                startAlarmActivity();
            }
        }.start();
    }

    private void startNewActivity() {
        if (dialogCountDownTimer != null) {
            dialogCountDownTimer.cancel();
        }
        Intent intent = new Intent(this, NewActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAlarmActivity() {
        if (dialogCountDownTimer != null) {
            dialogCountDownTimer.cancel();
        }
        Intent intent = new Intent(this, alarm.class);
        startActivity(intent);
        finish();
    }
}