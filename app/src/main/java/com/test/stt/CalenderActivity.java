package com.test.stt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class CalenderActivity extends AppCompatActivity {

    DatePicker datePicker;  //  datePicker - 날짜를 선택하는 달력
    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
    EditText edtDiary;   //  edtDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnSave, home;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼

    String fileName;   //  fileName - 돌고 도는 선택된 날짜의 파일 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 앱 첫 시작 시 돌아가는 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);
        setTitle("토토가 대신 써주는 일기장");

        // 뷰에 있는 위젯들 리턴 받아두기
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        viewDatePick = (TextView) findViewById(R.id.viewDatePick);
        edtDiary = (EditText) findViewById(R.id.edtDiary);
        btnSave = (Button) findViewById(R.id.btnSave);
        home = (Button)findViewById(R.id.homebutton);
        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();
        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);
        int cDay = c.get(Calendar.DAY_OF_MONTH);

        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
        checkedDay(cYear, cMonth, cDay);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // datePick 기능 만들기
        // datePicker.init(연도,달,일)
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 이미 선택한 날짜에 일기가 있는지 없는지 체크해야할 시간이다
                checkedDay(year, monthOfYear, dayOfMonth);
            }
        });

        // 저장/수정 버튼 누르면 실행되는 리스너
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fileName을 넣고 저장 시키는 메소드를 호출
                saveDiary(fileName);
            }
        });
    }

    // 일기 파일 읽기
    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {

        // 받은 날짜로 날짜 보여주는
        viewDatePick.setText(year + " - " + (monthOfYear+1)+ " - " + dayOfMonth);
        // 파일 이름을 만들어준다. 파일 이름은 "20170318.txt" 이런식으로 나옴
        if(dayOfMonth<10)
            fileName = year + "" + (monthOfYear+1) + "0" + dayOfMonth + ".txt";
        else
            fileName = year + "" + (monthOfYear+1) + "" + dayOfMonth + ".txt";
        // 읽어봐서 읽어지면 일기 가져오고
        // 없으면 catch 그냥 살아? 아주 위험한 생각같다..
        StringBuffer buffer= new StringBuffer();
        try {
            FileInputStream fis=openFileInput(fileName);
            BufferedReader reader= new BufferedReader(new InputStreamReader(fis));
            String str=reader.readLine();//한 줄씩 읽어오기
            while(str!=null){
                buffer.append(str+"\n");
                str=reader.readLine();
            }
            // 읽어서 토스트 메시지로 보여줌
     //      Toast.makeText(getApplicationContext(), "일기 써둔 날", Toast.LENGTH_SHORT).show();
            edtDiary.setText(buffer.toString());
    //        btnSave.setText("저장");

        } catch (Exception e) { // UnsupportedEncodingException , FileNotFoundException , IOException
            // 없어서 오류가 나면 일기가 없는 것 -> 일기를 쓰게 한다.
          //  Toast.makeText(getApplicationContext(), "일기 없는 날", Toast.LENGTH_SHORT).show();
            edtDiary.setText("");
     //      btnSave.setText("저장");

            e.printStackTrace();
        }

    }

    // 일기 저장하는 메소드
    @SuppressLint("WrongConstant")
    private void saveDiary(String readDay) {

        FileOutputStream fos = null;

        try {
            fos = openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS); //MODE_WORLD_WRITEABLE
            String content = edtDiary.getText().toString();

            // String.getBytes() = 스트링을 배열형으로 변환?
            fos.write(content.getBytes());
            //fos.flush();
            fos.close();

            // getApplicationContext() = 현재 클래스.this ?
            Toast.makeText(getApplicationContext(), "일기 저장됨", Toast.LENGTH_SHORT).show();

        } catch (Exception e) { // Exception - 에러 종류 제일 상위 // FileNotFoundException , IOException
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류오류", Toast.LENGTH_SHORT).show();
        }
    }

}

