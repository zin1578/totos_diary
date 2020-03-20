package com.test.stt;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DairyActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy);

        textView = (TextView)findViewById(R.id.textView);
        Button homeButton = (Button) findViewById(R.id.homeButton);
        ReadFile();
         homeButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
    }

    void ReadFile(){
        StringBuffer buffer= new StringBuffer();
        try {
            //FileInputStream 객체생성, 파일명 "data.txt"
            FileInputStream fis=openFileInput("data.txt");
            BufferedReader reader= new BufferedReader(new InputStreamReader(fis));
            String str=reader.readLine();//한 줄씩 읽어오기
            while(str!=null){
                buffer.append(str+"\n");
                str=reader.readLine();
            }
            textView.setText(buffer.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        /*
        /////////////////////// 파일 읽기 ///////////////////////
// 파일 생성
        String line = null; // 한줄씩 읽기
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camdata"); // 저장 경로
// 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedReader buf = new BufferedReader(new FileReader(saveFile+"/CarnumData.txt"));
            while((line=buf.readLine())!=null){
                textView.append(line);
                textView.setText(line);
                textView.append("\n");
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    */
    }

}

