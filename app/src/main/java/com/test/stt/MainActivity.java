package com.test.stt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_MENU = 101;
    Intent intent;
    SpeechRecognizer mRecognizer;
    Button sttBtn, totoTextView;
    TextView textView, likes, fishCount, tvLikes;
    ImageView totoImageView;
    AnimationDrawable frameAnimation;
    String key ="4258457626421016575";
    String str1;
    String data;
    String emotionJson;
    double scoreJson;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
            //호감도 0~10 고정
        SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
        if(10<pref.getInt("like", 0)){
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("like", 10);
            editor.commit();
        }
        if(0>pref.getInt("like", 0)){
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("like", 0);
            editor.commit();
        }
        SharedPreferences prefish =getSharedPreferences("fish", MODE_PRIVATE);
        tvLikes = (TextView)findViewById(R.id.textViewLikes);//호감도 숫자
        tvLikes.setText("" + pref.getInt("like", 0)+"/10");
        totoTextView = (Button) findViewById(R.id.imageButton);
        totoTextView.setVisibility(View.INVISIBLE); // 처음 말풍선 안 보이게
        textView = (TextView)findViewById(R.id.sttResult);
        likes = (TextView)findViewById(R.id.textView_likes);//텍스트 뷰의 " 호감도: " 숫자 표시 x
        fishCount = (TextView)findViewById(R.id.textView_fishcount);
        fishCount.setText("" + prefish.getInt("fish", 0));
        totoImageView =(ImageView)findViewById(R.id.totoImage);
        totoImageView.setBackgroundResource(R.drawable.normal_animation);
        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
        frameAnimation.start();

        sttBtn = (Button) findViewById(R.id.sttStart);
        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        sttBtn.setOnClickListener(v ->{
            mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });
    }

  //  public void onWindowFocusChanged(boolean hasFocus) {
  //     super.onWindowFocusChanged(hasFocus);
   //     if (hasFocus) {
            // Starting the animation when in Focus
  //          frameAnimation.start();
 //       } else {
            // Stoping the animation when not in Focus
  //          frameAnimation.stop();
  //      }
 //   }

    public void onButtonDiaryClicked(View v){
        Intent intent = new Intent(getApplicationContext(), CalenderActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MENU);
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            //감정 키워드
            String[] happyEmo = new String[]{"행복", "좋아", "좋은", "기뻤", "기뻐", "신나", "감사", "평온", "평화", "상쾌","완벽","활기",
                    "즐겁", "즐거워", "즐거웠어", "사랑", "만족", "다행", "뿌듯", "보람", "희망", "맛있", "편했", "안심", "고마워", "고마웠"};
            String[] angryEmo = new String[]{"화가나는", "화가 나", "짜증", "화가 났","화 나", "화났", "빡쳐", "빡침", "답답", "환멸", "죽여", "죽이고 싶어"};
            String[] badEmo = new String[]{"열등", "나빴","찜찜","허무", "맛없", "싫", "싫었", "노답", "싫음", "불편", "싫어", "나빠", "쓰레기", "안좋아", "안좋아", "개같아", "개 같아","무의미"};
            String[] anxiousEmo = new String[]{"죄책감", "심란", "후회", "무서워", "걱정", "깜깜", "긴장", "스트레스", "무서운", "막막", "어떡하냐", "어떡해"};
            String[] sadEmo = new String[]{"슬퍼", "슬픈", "슬펐", "마음이 아파", "마음이 아팠", "실망", "비통", "우울", "울적", "눈물"};
            String[] hurtEmo = new String[]{"질투", "배신", "억울", "버려진", "상처"};
            String[] isoEmo = new String[]{"외로워", "외로웠", "고독", "심심", "혼자", "쓸쓸", "외롭"};
            String[] embEmo = new String[]{"당황", "황당", "어이가 없", "어이 없", "어처구니", "혼란", "심란", "놀랐", "깜짝", "놀랬", "그랬지 뭐야"};
            String[] neutralEmo = new String[]{"그냥 그래", "쏘쏘", "괜찮아", "나쁘지 않아", "딱히"};
            String[] exaustedEmo = new String[]{"힘들", "힘든", "피곤", "지쳤", "지쳐", "아파", "아픈", "지친", "바빴", "바쁜", "스트레스", "무기력"};

            // 토토의 스페셜한 대답 배열
            String[] totoLove = new String[]{"감동이야..나도 많이 사랑해", "내가 널 더 사랑해", "내가 아껴두는 생선도 줄 수 있어", "나도! 앞으로도 나랑 잘 지내자!"};
            String[] totoLike = new String[]{"난 너를 좋아해", "너는 마음이 따뜻한 아이구나", "너가 점점 좋아진다"};
            String[] totoPlus = new String[]{"좋은 말 해줘서 고마워", "고마워.. 헤헤", "자신감이 생겨나!", "넌 남을 잘 칭찬하는 게 장점이야!"};
            String[] totoNegative = new String[]{"와 말넘심", "반사!", "너어 진짜~! 흥!", "너무해!"};
            String[] totoSuperNegative = new String[]{"너랑 안 놀아. 난 진지해.", "넌 나를 화나게 했어", "왜 그러는 거야?"};
            // 토토의 감정에 따른 대답들
            String[] totoHappy = new String[]{"헤헤 오늘 기분이 좋아보이네! 너가 기뻐서 나도 좋아", "기쁜일이 있었구나! 항상 좋은 일만 있길 바라"};
            String[] totoBad = new String[]{"그랬구나.. 기분이 안 좋았겠다.. ", "나쁜 기억은 어서 잊어버리고 나머지 하루를 즐겁게 보내자",
                    "내 친구가 주식을 하는데 오르락 내리락 하더라고...그래도 힘든 시간을 버티면 언젠가 복이 올가야!"};
            String[] totoAngry = new String[]{"그랬구나.. 나라도 화가 났을 거야. 내가 대신 혼내 주고 싶어!",
                    "뭐 그런 일이! 화가 많이 나지? 달달한 코코아를 타서 마시면 기분이 좀 나아져", "참나 그런 일이! 화가 나네!"};
            String[] totoAnxious = new String[]{"걱정이 될 때는 나를 떠올려봐!", "마음이 정말 심란하겠다... 내가 곁에 있어 줄게"};
            String[] totoSad = new String[]{"그랬구나... 너를 위해 기도할게", "너가 슬퍼서 나도 슬퍼", "슬픈 감정은 나에게 나눠 줘"};
            String[] totoHurt = new String[]{"그랬구나...토닥토닥", "상처가 되었겠다..."};
            String[] totoIso = new String[]{"내가 곁에 있어줄게", "인간은 원래 외롭게 태어났나봐", ""};
            String[] totoEmb = new String[]{"아니 그건 정말 황당한걸?", "아니 뭐 그런 일이!?", "예측할 수 없는 것이 참 많아"};
            String[] totoNeutral = new String[]{"그랬구나", "보통, 적당함의 미학..", "평범한 게 좋은 거지!"};
            String[] totoExausted = new String[]{"피곤하지! 이리와 어서 누워", "피곤할 때는 따뜻한 음료를 마시고 반신욕을 해봐. 피로가 풀릴 거야!",
                    "피곤하구나. 참 인생이 힘들지?고양이도 사람도 적당한 쉼이 필요해"};
            //특별 대화
            String[] totoGreet = new String[]{"안녕! 오늘도 좋은 하루 보내!", "안녕! 오늘도 너와 함께 해서 기뻐", "안녕~ 오늘은 특별한 일 없었어?"};
            String[] totoSelf = new String[]{"난 토토라고 해! 오늘 있었던 일이나 무엇이든지 너가 느낀 것을 나누고 싶다면 아래 버튼을 누르고 이야기 해봐! 참고로 [감정]에 대한 단어를 넣어서 이야기하면 내가 더 잘 알아들을 수 있어",
                    "나 토토잖아... 까먹었어? 장난이지?", "난 인간어를 배우고 있는 고양이 토토야. 잘 부탁해!",
                    "너의 친구 토토잖아! 내가 너 대신 일기장을 써주고, 넌 가끔 나에게 먹을 것을 주는 아주 좋은 관계지!"};
            String[] totoEat = new String[]{"냠냠...정말 맛있어!", "고마워! 덕분에 맛있게 먹었어", "냠냠냠 잘 먹었어!"};
            String[] totoFish = new String[]{"그런 일이... 내가 아껴두고 있던 건데 이거 먹고 힘내!",
                    "그랬구나..이거 먹고 힘내. 항상 좋은 일만 있길 바라", "안 좋은 감정은 다 털어내고 이거 먹고 힘내자!"};
            String[] totoQuestion = new String[]{"미안 잘 못알아들었어. 나에게 말을 걸때는 감정을 포함하는 문장으로 말을 거는 게 좋아",
                    "으음 더 쉬운 말로 설명해줄 수 있어? 내가 인간말은 아직 초급이라서.. 헤헤", "냐아~", "냐아냐냐아~"};
            String[] totoWant = new String[]{"너가 원하는 걸 이루기 바라", "실행에 옮기자"};
            //전송할 데이터
            ArrayList<String> emotionArr = new ArrayList<>();
            ArrayList<String> dateArr = new ArrayList<>();

            int happy = 0;
            int angry = 0;
            int bad = 0;
            int anxious = 0;
            int sad = 0;
            int hurt = 0;
            int iso = 0;
            int emb = 0;
            int neutral = 0;
            int exausted = 0;
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < matches.size(); i++) {
                textView.setText(matches.get(i));
                totoTextView.setText("");
            }

            str1 = (String) textView.getText();

            if (str1.contains("토토") || str1.contains("토 토") || str1.length() < 9) {
                if (str1.length() < 5 && (str1.contains("토토") || str1.contains("토 토"))) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("나 불렀어? 오늘은 어떻게 지냈는지 이야기 해 봐");
                }else if (str1.contains("사랑") ||str1.contains("좋아")|| ((str1.contains("토 토") || str1.contains("토토"))
                        && str1.contains("좋아"))) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
                    int count = pref.getInt("like", 0);
                    //호감도 증가
                    count++;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("like", count);
                    editor.commit();
                    tvLikes.setText("" + pref.getInt("like", 0)+"/10");
                    if(count<8) {
                        int r = (int) (Math.random() * totoLike.length);
                        totoTextView.setText(totoLike[r]);
                    }
                    else {
                        int r = (int) (Math.random() * totoLove.length);
                        totoTextView.setText(totoLove[r]);
                    }

                }else if (str1.contains("재수")||str1.contains("못난")||str1.contains("못됐어")||str1.contains("재수 없어")||str1.contains("멍청") || str1.contains("바보")||str1.contains("똥")||
                        str1.contains("미워")||str1.contains("너 싫어")||str1.contains("못 생겼")||
                str1.contains("흥")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.angry_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    //호감도 감소
                    SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
                    int count = pref.getInt("like", 0);
                    count--;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("like", count);
                    editor.commit();
                    tvLikes.setText("" + pref.getInt("like", 0)+"/10");
                    if(count>2) {
                        int r = (int) (Math.random() * totoNegative.length);
                        totoTextView.setText(totoNegative[r]);
                    }
                    else {int r = (int) (Math.random() * totoSuperNegative.length);
                        totoTextView.setText(totoSuperNegative[r]);}
                }else if (str1.contains("심심") || str1.contains("놀아") || str1.contains("놀자")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("심심해? 좋아! 나랑 놀자");
                }else if(str1.contains("뭐 해")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("널 기다렸어");
                }else if(str1.contains("안녕")||str1.contains("좋은아침")||str1.contains("굿 모닝")||
                        str1.contains("좋은하루")||str1.contains("좋은 아침")||str1.contains("굿모닝")||
                        str1.contains("좋은 하루")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int) (Math.random() * totoGreet.length);
                    totoTextView.setText(totoGreet[r]);
                }else if (str1.contains("배불러")||(str1.contains("배 불러"))) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("운동하러 가자!");
                }else if(str1.contains("누구")||str1.contains("너 누구야")||str1.contains("누구야")){//토토 자기소개
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int) (Math.random() * totoSelf.length);
                    totoTextView.setText(totoSelf[r]);
                } else if(str1.contains("배 고파")||str1.contains("배고파")||str1.contains("배고팠")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.surprize_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("꼬르륵......");
                } else if(str1.contains("귀여워")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
                    int count = pref.getInt("like", 0);
                    //호감도 증가
                    count++;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("like", count);
                    editor.commit();
                    tvLikes.setText("" + pref.getInt("like", 0)+"/10");
                    int r = (int) (Math.random() * totoPlus.length);
                    totoTextView.setText(totoPlus[r]);
                }else if(str1.contains("멋있어")||str1.contains("멋")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
                    int count = pref.getInt("like", 0);
                    //호감도 증가
                    count++;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("like", count);
                    editor.commit();
                    tvLikes.setText("" + pref.getInt("like", 0)+"/10");
                    int r = (int) (Math.random() * totoPlus.length);
                    totoTextView.setText(totoPlus[r]);
                }else if(str1.contains("똑똑이")||str1.contains("똑똑해")||str1.contains("천재")||
                        str1.contains("최고")||str1.contains("완벽")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    SharedPreferences pref =getSharedPreferences("like", MODE_PRIVATE);
                    int count = pref.getInt("like", 0);
                    //호감도 증가
                    count++;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("like", count);
                    editor.commit();
                    tvLikes.setText("" + pref.getInt("like", 0)+"/10");
                    int r = (int) (Math.random() * totoPlus.length);
                    totoTextView.setText(totoPlus[r]);
                }else if(str1.contains("하루")&&!str1.contains("오늘")&&!str1.contains("좋은")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.surprize_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("아니 그 이름은..! 또 다른 나의 이름을 아는 자...당신의 정체는 무엇이지?");
                }else if(str1.contains("밥 먹자")||str1.contains("밥 먹을")||str1.contains("밥 줄게")||str1.contains("밥")){
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                    int count = prefish.getInt("fish", 0);
                    //fish 감소 //생선
                    if(count>=1) {
                        count--;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText("" + count);
                        int r = (int) (Math.random() * totoEat.length);
                        totoTextView.setText(totoEat[r]);
                        totoImageView.setBackgroundResource(R.drawable.eat_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                    }
                    else {
                        totoTextView.setText("생선이 없어...");
                        totoImageView.setBackgroundResource(R.drawable.surprize_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                    }
                } else if(str1.contains("졸려")||str1.contains("하품")||str1.contains("졸리다")||
                str1.contains("자고 싶")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.sleepy_animation);//sleepy animation 추가
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("나도.. 나랑 같이 자자");
                }else if(true){//짧은 키워드 감정 추출
                        for (int i = 0; i < happyEmo.length; i++) {
                            if (str1.contains(happyEmo[i])) {
                                happy++;
                            }
                        }
                        for (int i = 0; i < angryEmo.length; i++) {
                            if (str1.contains(angryEmo[i])) {
                                angry++;
                            }
                        }
                        for (int i = 0; i < badEmo.length; i++) {
                            if (str1.contains(badEmo[i])) {
                                bad++;
                            }
                        }
                        for (int i = 0; i < anxiousEmo.length; i++) {
                            if (str1.contains(anxiousEmo[i])) {
                                anxious++;
                            }
                        }
                        for (int i = 0; i < sadEmo.length; i++) {
                            if (str1.contains(sadEmo[i])) {
                                sad++;
                            }
                        }
                        for (int i = 0; i < hurtEmo.length; i++) {
                            if (str1.contains(hurtEmo[i])) {
                                hurt++;
                            }
                        }
                        for (int i = 0; i < isoEmo.length; i++) {
                            if (str1.contains(isoEmo[i])) {
                                iso++;
                            }
                        }
                        for (int i = 0; i < embEmo.length; i++) {
                            if (str1.contains(embEmo[i])) {
                                emb++;
                            }
                        }
                        for (int i = 0; i < neutralEmo.length; i++) {
                            if (str1.contains(neutralEmo[i])) {
                                neutral++;
                            }
                        }
                        for (int i = 0; i < exaustedEmo.length; i++) {
                            if (str1.contains(exaustedEmo[i])) {
                                exausted++;
                            }
                        }if (happy > (angry + bad + anxious + sad + hurt + iso + emb)) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.happy_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int)(Math.random()*totoHappy.length);
                            totoTextView.setText (totoHappy[r]);
                            WriteFile("/ 감정:행복 /", str1);

                        } else if (angry > (happy + bad + anxious + sad + hurt + iso + emb)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            //
                              //랜덤 생선 이벤트 발생
                            int a = (int)(Math.random()*10);
                            if(a>3) {
                            totoImageView.setBackgroundResource(R.drawable.angry_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int)(Math.random()*totoAngry.length);
                            totoTextView.setText (totoAngry[r]);
                            WriteFile("/ 감정:화남 /", str1);
                            }
                            else{
                            SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                            int count = prefish.getInt("fish", 0);
                            //fish 증가 //생선
                            count++;
                            SharedPreferences.Editor edit = prefish.edit();
                            edit.putInt("fish", count);
                            edit.commit();
                            fishCount.setText(""+count);
                            int r = (int) (Math.random() * totoFish.length);
                            totoTextView.setText(totoFish[r]);
                            totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            WriteFile("/ 감정:화남 /", str1);
                             }

                        } else if (bad > (angry + happy + anxious + sad + hurt + iso + emb)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            //
                             //랜덤 생선 이벤트 발생
                            int a = (int)(Math.random()*10);
                            if(a>3) {
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoBad.length);
                                totoTextView.setText (totoBad[r]);
                                WriteFile("/ 감정:기분 나쁨 /", str1);
                            }
                            else{
                            SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                            int count = prefish.getInt("fish", 0);
                            //fish 증가 //생선
                            count++;
                            SharedPreferences.Editor edit = prefish.edit();
                            edit.putInt("fish", count);
                            edit.commit();
                            fishCount.setText(""+count);
                            int r = (int) (Math.random() * totoFish.length);
                            totoTextView.setText(totoFish[r]);
                            totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            WriteFile("/ 감정:기분 나쁨 /", str1);
                            }

                        } else if (anxious > (angry + bad + happy + sad + hurt + iso + emb)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.happy_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int)(Math.random()*totoAnxious.length);
                            totoTextView.setText (totoAnxious[r]);
                            WriteFile("/ 감정:걱정 /", str1);

                        } else if (sad > (angry + bad + anxious + happy + hurt + iso + emb)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                                    //
                            //랜덤 생선 이벤트 발생
                            int a = (int)(Math.random()*10);
                            if(a>3) {
                                totoImageView.setBackgroundResource(R.drawable.sad_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoSad.length);
                                totoTextView.setText (totoSad[r]);
                                WriteFile("/ 감정:슬픔 /", str1);
                            }
                            else{
                                SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                                int count = prefish.getInt("fish", 0);
                                //fish 증가 //생선
                                count++;
                                SharedPreferences.Editor edit = prefish.edit();
                                edit.putInt("fish", count);
                                edit.commit();
                                fishCount.setText(""+count);
                                int r = (int) (Math.random() * totoFish.length);
                                totoTextView.setText(totoFish[r]);
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                WriteFile("/ 감정:슬픔 /", str1);
                            }

                        } else if (hurt > (angry + bad + anxious + sad + happy + iso + emb)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            //랜덤 생선 이벤트 발생
                            int a = (int)(Math.random()*10);
                            if(a>3) {
                                int r = (int) (Math.random() * totoHurt.length);
                                totoTextView.setText(totoHurt[r]);
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                WriteFile("/ 감정:상처 /", str1);
                            }
                            else{
                                SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                                int count = prefish.getInt("fish", 0);
                                //fish 증가 //생선
                                count++;
                                SharedPreferences.Editor edit = prefish.edit();
                                edit.putInt("fish", count);
                                edit.commit();
                                fishCount.setText(""+count);
                                int r = (int) (Math.random() * totoFish.length);
                                totoTextView.setText(totoFish[r]);
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                WriteFile("/ 감정:상처 /", str1);
                            }


                        } else if (iso > (angry + bad + anxious + sad + hurt + happy + emb)) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.happy_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int) (Math.random() * totoIso.length);
                            totoTextView.setText(totoIso[r]);
                            WriteFile("/ 감정:외로운 /", str1);

                        } else if (emb > (angry + bad + anxious + sad + hurt + iso + happy)) {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();

                            totoImageView.setBackgroundResource(R.drawable.supersur_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int) (Math.random() * totoEmb.length);
                            totoTextView.setText(totoEmb[r]);
                            WriteFile("/ 감정:놀람 /", str1);

                        } else if (neutral > (happy + angry + happy + anxious + sad + hurt + iso + emb)) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int) (Math.random() * totoNeutral.length);
                            totoTextView.setText(totoNeutral[r]);
                            WriteFile("/ 감정:보통 /", str1);

                        } else if (exausted > (happy + angry + happy + anxious + sad + hurt + iso + emb + neutral)) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.tired_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int) (Math.random() * totoExausted.length);
                            totoTextView.setText(totoExausted[r]);
                            WriteFile("/ 감정: 지친 /", str1);

                        }else if (str1.contains("카페")) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            totoTextView.setText("나도 카페에서 시원~한 아아를 마시고 싶어");
                            WriteFile("/ 일상:카페 /", str1);

                        } else if (str1.contains("학교") || str1.contains("일")||str1.contains("과제")
                                ||str1.contains("집안일")||str1.contains("청소")) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            totoTextView.setText("오늘도 수고했어!");
                            WriteFile("/ 일상:토닥토닥 /", str1);
                        } else if (str1.contains("공원") || str1.contains("운동") || str1.contains("헬스") || str1.contains("산책")) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.happy_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            WriteFile("/ 일상:운동 /", str1);
                            totoTextView.setText("잘 했어! 칭찬해요~ 항상 건강해야 해!");
                        } else if (str1.contains("집") || str1.contains("쉬었어")||str1.contains("쉬고")||str1.contains("잤어")||
                            str1.contains("아무 것도 안")||str1.contains("아무 것도 하지 않")||str1.contains("누워")) {
                        totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            totoTextView.setText("그런 날도 필요하지");
                            WriteFile("/ 일상:쉼 /", str1);

                        } else if (true) {//키워드 작동 안하면
                        Emot t0 = new Emot();
                        t0.start();
                        try {
                            t0.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (scoreJson < 0.8) {
                            Senti t1 = new Senti();
                            t1.start();
                        }
                        if (scoreJson >= 0.8) {
                            if (emotionJson.equals("부정")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("기분이 별로 좋지 않구나. 그럴 때는 맛있는 거를 먹고 산책을 하는 게 좋아");
                                WriteFile("/감정: 나쁨 /", str1);

                            } else if (emotionJson.equals("중립")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoNeutral.length);
                                totoTextView.setText(totoNeutral[r]);
                                WriteFile("/ 감정: 보통 /", str1);

                            } else if (emotionJson.equals("긍정")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoHappy.length);
                                totoTextView.setText(totoHappy[r]);
                                WriteFile("/ 감정: 좋음 /", str1);

                            } else if (emotionJson.equals("기쁨")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoHappy.length);
                                totoTextView.setText(totoHappy[r]);
                                WriteFile("/ 감정: 행복 /", str1);

                            } else if (emotionJson.equals("신뢰")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("그렇구나..! 너가 믿는 걸 나도 믿어.");
                                WriteFile("/ 감정: 신뢰 /", str1);

                            } else if (emotionJson.equals("공포")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("마음이 정말 심란하겠다... 내가 곁에 있어 줄게");
                                WriteFile("/ 감정: 공포 /", str1);

                            } else if (emotionJson.equals("기대")) {
                                if (str1.contains("싶어") ||str1.contains("싶다") ||str1.contains("싶") || str1.contains("할래") || str1.contains("갈래") || str1.contains("거야")) {
                                    totoTextView.setVisibility(View.VISIBLE);
                                    frameAnimation.stop();
                                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                    frameAnimation.start();
                                    WriteFile("/ 일상: 의지/", str1);
                                    int r = (int)(Math.random()*totoWant.length);
                                    totoTextView.setText (totoWant[r]);
                                }
                                else {
                                    //감성 종류 : [부정, 중립, 긍정]
                                    // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                    totoTextView.setVisibility(View.VISIBLE);
                                    frameAnimation.stop();
                                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                    frameAnimation.start();
                                    totoTextView.setText("헤헤 너 정말 기분이 좋아 보인다! 그렇게 기대되?");
                                    WriteFile("/ 감정: 기대 /", str1);
                                }
                            } else if (emotionJson.equals("놀라움")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.surprize_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("와...그런 일이 있다니!");
                                WriteFile("/ 감정: 놀라움 /", str1);

                            } else if (emotionJson.equals("슬픔")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.sad_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("너가 슬퍼서 나도 슬퍼");
                                WriteFile("/ 감정: 슬픔 /", str1);

                            } else if (emotionJson.equals("혐오")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoBad.length);
                                totoTextView.setText(totoBad[r]);
                                WriteFile("/ 감정: 혐오 /", str1);

                            } else if (emotionJson.equals("분노")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.angry_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoAngry.length);
                                totoTextView.setText(totoAngry[r]);
                                WriteFile("/ 감정: 화남 /", str1);

                            }else {
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int) (Math.random() * totoQuestion.length);
                                totoTextView.setText(totoQuestion[r]); }} else {
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int) (Math.random() * totoQuestion.length);
                            totoTextView.setText(totoQuestion[r]); }
                } }}else if (true) { //긴문장 감정
                for (int i = 0; i < happyEmo.length; i++) {
                    if (str1.contains(happyEmo[i])) {
                        happy++;
                    }
                }
                for (int i = 0; i < angryEmo.length; i++) {
                    if (str1.contains(angryEmo[i])) {
                        angry++;
                    }
                }
                for (int i = 0; i < badEmo.length; i++) {
                    if (str1.contains(badEmo[i])) {
                        bad++;
                    }
                }
                for (int i = 0; i < anxiousEmo.length; i++) {
                    if (str1.contains(anxiousEmo[i])) {
                        anxious++;
                    }
                }
                for (int i = 0; i < sadEmo.length; i++) {
                    if (str1.contains(sadEmo[i])) {
                        sad++;
                    }
                }
                for (int i = 0; i < hurtEmo.length; i++) {
                    if (str1.contains(hurtEmo[i])) {
                        hurt++;
                    }
                }
                for (int i = 0; i < isoEmo.length; i++) {
                    if (str1.contains(isoEmo[i])) {
                        iso++;
                    }
                }
                for (int i = 0; i < embEmo.length; i++) {
                    if (str1.contains(embEmo[i])) {
                        emb++;
                    }
                }
                for (int i = 0; i < neutralEmo.length; i++) {
                    if (str1.contains(neutralEmo[i])) {
                        neutral++;
                    }
                }
                for (int i = 0; i < exaustedEmo.length; i++) {
                    if (str1.contains(exaustedEmo[i])) {
                        exausted++;
                    }
                }

                if (happy > (angry + bad + anxious + sad + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int)(Math.random()*totoHappy.length);
                    totoTextView.setText (totoHappy[r]);
                    WriteFile("/ 감정:행복 /", str1);

                } else if (angry > (happy + bad + anxious + sad + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    //
                    int a = (int)(Math.random()*10);
                    if(a>3) {
                        int r = (int) (Math.random() * totoAngry.length);
                        totoTextView.setText(totoAngry[r]);
                        totoImageView.setBackgroundResource(R.drawable.angry_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:분노 /", str1);
                    }
                    else{
                        SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                        int count = prefish.getInt("fish", 0);
                        //fish 증가 //생선
                        count++;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText(""+count);
                        int r = (int) (Math.random() * totoFish.length);
                        totoTextView.setText(totoFish[r]);
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:분노 /", str1);
                    }

                } else if (bad > (angry + happy + anxious + sad + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    ///
                    int a = (int)(Math.random()*10);
                    if(a>3) {
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        int r = (int)(Math.random()*totoBad.length);
                        totoTextView.setText (totoBad[r]);
                        WriteFile("/ 감정:기분 나쁨 /", str1);
                    }
                    else{
                        SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                        int count = prefish.getInt("fish", 0);
                        //fish 증가 //생선
                        count++;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText(""+count);
                        int r = (int) (Math.random() * totoFish.length);
                        totoTextView.setText(totoFish[r]);
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:기분 나쁨 /", str1);
                    }

                } else if (anxious > (angry + bad + happy + sad + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    //
                    int a = (int)(Math.random()*10);
                    if(a>3) {
                        totoImageView.setBackgroundResource(R.drawable.happy_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        int r = (int)(Math.random()*totoAnxious.length);
                        totoTextView.setText (totoAnxious[r]);
                        WriteFile("/ 감정:걱정 /", str1);
                    }
                    else{
                        SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                        int count = prefish.getInt("fish", 0);
                        //fish 증가 //생선
                        count++;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText(""+count);
                        int r = (int) (Math.random() * totoFish.length);
                        totoTextView.setText(totoFish[r]);
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:걱정 /", str1);
                    }


                } else if (sad > (angry + bad + anxious + happy + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    //
                    int a = (int)(Math.random()*10);
                    if(a>3) {
                        totoImageView.setBackgroundResource(R.drawable.sad_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        int r = (int)(Math.random()*totoSad.length);
                        totoTextView.setText (totoSad[r]);
                        WriteFile("/ 감정:슬픔 /", str1);
                    }
                    else{
                        SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                        int count = prefish.getInt("fish", 0);
                        //fish 증가 //생선
                        count++;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText(""+count);
                        int r = (int) (Math.random() * totoFish.length);
                        totoTextView.setText(totoFish[r]);
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:슬픔 /", str1);
                    }

                } else if (hurt > (angry + bad + anxious + sad + happy + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();

                    int a = (int)(Math.random()*10);
                    if(a>3) {
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        int r = (int)(Math.random()*totoSad.length);
                        totoTextView.setText (totoSad[r]);
                        WriteFile("/ 감정:상처 /", str1);
                    }
                    else{
                        SharedPreferences prefish = getSharedPreferences("fish", MODE_PRIVATE);
                        int count = prefish.getInt("fish", 0);
                        //fish 증가 //생선
                        count++;
                        SharedPreferences.Editor edit = prefish.edit();
                        edit.putInt("fish", count);
                        edit.commit();
                        fishCount.setText(""+count);
                        int r = (int) (Math.random() * totoFish.length);
                        totoTextView.setText(totoFish[r]);
                        totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                        frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                        frameAnimation.start();
                        WriteFile("/ 감정:상처 /", str1);
                    }

                } else if (iso > (angry + bad + anxious + sad + hurt + happy + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int)(Math.random()*totoIso.length);
                    totoTextView.setText (totoIso[r]);
                    WriteFile("/ 감정:외로운 /", str1);

                } else if (emb > (angry + bad + anxious + sad + hurt + iso + happy)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.supersur_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int)(Math.random()*totoEmb.length);
                    totoTextView.setText (totoEmb[r]);
                    WriteFile("/ 감정:놀람 /", str1);

                } else if (neutral > (happy + angry + happy + anxious + sad + hurt + iso + emb)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int)(Math.random()*totoNeutral.length);
                    totoTextView.setText (totoNeutral[r]);
                    WriteFile("/ 감정:보통 /", str1);

                } else if (exausted > (happy + angry + happy + anxious + sad + hurt + iso + emb + neutral)) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.tired_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    int r = (int)(Math.random()*totoExausted.length);
                    totoTextView.setText (totoExausted[r]);
                    WriteFile("/ 감정: 지친 /", str1);

                }else if (str1.contains("카페")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("나도 카페에서 시원~한 아아를 마시고 싶어");
                    WriteFile("/ 일상:카페 /", str1);

                } else if (str1.contains("학교") || str1.contains("일")||str1.contains("과제")
                        ||str1.contains("집안일")||str1.contains("청소")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("오늘도 수고했어!");
                    WriteFile("/ 일상:토닥토닥 /", str1);
                }else if (str1.contains("싶어") ||str1.contains("싶다") ||str1.contains("싶") || str1.contains("할래") || str1.contains("갈래") || str1.contains("거야")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    WriteFile("/ 일상: 의지/", str1);
                    int r = (int)(Math.random()*totoWant.length);
                    totoTextView.setText (totoWant[r]);
                } else if (str1.contains("공원") || str1.contains("운동") || str1.contains("헬스") || str1.contains("산책")) {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    WriteFile("/ 일상:운동 /", str1);
                    totoTextView.setText("잘 했어! 칭찬해요~ 항상 건강해야 해!");
                } else if(str1.contains("집") || str1.contains("쉬었어")||str1.contains("쉬고")||str1.contains("누워")||
                        str1.contains("아무 것도 안")||str1.contains("아무 것도 하지 않")||str1.contains("잤어"))  {
                    totoTextView.setVisibility(View.VISIBLE);
                    frameAnimation.stop();
                    totoImageView.setBackgroundResource(R.drawable.normal_animation);
                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                    frameAnimation.start();
                    totoTextView.setText("그런 날도 필요하지");
                    WriteFile("/ 일상:쉼 /", str1);

                }  else if (true) {//키워드 작동 안하면
                        Emot t0 = new Emot();
                        t0.start();
                        try {
                            t0.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (scoreJson < 0.8) {
                        Senti t1 = new Senti();
                        t1.start();
                        }
                        if (scoreJson >= 0.8) {
                            if (emotionJson.equals("부정")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoBad.length);
                                totoTextView.setText (totoBad[r]);
                                WriteFile("/감정: 나쁨 /", str1);

                            } else if (emotionJson.equals("중립")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoNeutral.length);
                                totoTextView.setText (totoNeutral[r]);
                                WriteFile("/ 감정: 보통 /", str1);

                            } else if (emotionJson.equals("긍정")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoHappy.length);
                                totoTextView.setText (totoHappy[r]);
                                WriteFile("/ 감정: 좋음 /", str1);

                            } else if (emotionJson.equals("기쁨")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoHappy.length);
                                totoTextView.setText (totoHappy[r]);
                                WriteFile("/ 감정: 행복 /", str1);

                            } else if (emotionJson.equals("신뢰")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("그렇구나..! 너가 믿는 걸 나도 믿어.");
                                WriteFile("/ 감정: 신뢰 /", str1);

                            } else if (emotionJson.equals("공포")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                totoTextView.setText("마음이 정말 심란하겠다... 내가 곁에 있어 줄게");
                                WriteFile("/ 감정: 공포 /", str1);

                            } else if (emotionJson.equals("기대")) { //기대의 경우 싶다가 들어있다면 want키워드
                                if (str1.contains("싶어") ||str1.contains("싶다") ||str1.contains("싶") || str1.contains("할래") || str1.contains("갈래") || str1.contains("거야")) {
                                    totoTextView.setVisibility(View.VISIBLE);
                                    frameAnimation.stop();
                                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                    frameAnimation.start();
                                    WriteFile("/ 일상: 의지/", str1);
                                    int r = (int)(Math.random()*totoWant.length);
                                    totoTextView.setText (totoWant[r]);
                                }
                                else {
                                    //감성 종류 : [부정, 중립, 긍정]
                                    // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                    totoTextView.setVisibility(View.VISIBLE);
                                    frameAnimation.stop();
                                    totoImageView.setBackgroundResource(R.drawable.happy_animation);
                                    frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                    frameAnimation.start();
                                    totoTextView.setText("헤헤 너 정말 기분이 좋아 보인다! 그렇게 기대되?");
                                    WriteFile("/ 감정: 기대 /", str1);
                                }
                            } else if (emotionJson.equals("놀라움")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.surprize_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoEmb.length);
                                totoTextView.setText (totoEmb[r]);
                                WriteFile("/ 감정: 놀라움 /", str1);

                            } else if (emotionJson.equals("슬픔")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.sad_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoSad.length);
                                totoTextView.setText (totoSad[r]);
                                WriteFile("/ 감정: 슬픔 /", str1);

                            } else if (emotionJson.equals("혐오")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.hurt_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoBad.length);
                                totoTextView.setText (totoBad[r]);
                                WriteFile("/ 감정: 혐오 /", str1);

                            } else if (emotionJson.equals("분노")) {                  //감성 종류 : [부정, 중립, 긍정]
                                // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.angry_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoAngry.length);
                                totoTextView.setText (totoAngry[r]);
                                WriteFile("/ 감정: 화남 /", str1);

                            } else {
                                totoTextView.setVisibility(View.VISIBLE);
                                frameAnimation.stop();
                                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                                frameAnimation.start();
                                int r = (int)(Math.random()*totoQuestion.length);
                                totoTextView.setText (totoQuestion[r]);

                            }
                        }else{
                            totoTextView.setVisibility(View.VISIBLE);
                            frameAnimation.stop();
                            totoImageView.setBackgroundResource(R.drawable.normal_animation);
                            frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                            frameAnimation.start();
                            int r = (int)(Math.random()*totoQuestion.length);
                            totoTextView.setText (totoQuestion[r]);
                        }
                    } }else{
                totoTextView.setVisibility(View.VISIBLE);
                frameAnimation.stop();
                totoImageView.setBackgroundResource(R.drawable.normal_animation);
                frameAnimation = (AnimationDrawable) totoImageView.getBackground();
                frameAnimation.start();
                int r = (int)(Math.random()*totoQuestion.length);
                totoTextView.setText (totoQuestion[r]);
            }
                }



        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };


    String getXmlData( String str){

        //EditText에 작성된 Text얻어오기
        String receiveMsg="";

        String queryUrl="http://api.adams.ai/datamixiApi/omAnalysis?" +
        "&key=" + key + "&query=" + str + "&type=" +1;


        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("x-waple-authorization", key);

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ", receiveMsg);

                reader.close();
            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receiveMsg;
    }


    String getXmlData0( String str){

        //EditText에 작성된 Text얻어오기
        String receiveMsg="";

        String queryUrl="http://api.adams.ai/datamixiApi/omAnalysis?" +
                "&key=" + key + "&query=" + str + "&type=" +0;


        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("x-waple-authorization", key);

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ", receiveMsg);

                reader.close();
            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receiveMsg;
    }


    public class Emot extends Thread{
        public void run() {
            data = getXmlData(str1);
            Log.v("데이터: ", "감정");
            //        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject return_object = jsonObject.getJSONObject("return_object");
                JSONArray result = return_object.getJSONArray("Result");
                JSONArray context = result.getJSONArray(0);
                {
                    // jsonObject = result.getJSONObject(0);
                    scoreJson = context.getDouble(0);
                    emotionJson = context.getString(1);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class Senti extends Thread{
        public void run() {
            data = getXmlData(str1);
            Log.v("데이터: ", "감정");
            //        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject return_object = jsonObject.getJSONObject("return_object");
                JSONArray result = return_object.getJSONArray("Result");
                JSONArray context = result.getJSONArray(0);
                {
                    // jsonObject = result.getJSONObject(0);
                    scoreJson = context.getDouble(0);
                    emotionJson = context.getString(1);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    void WriteFile(String curEmo, String text){
        try {
            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
            //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
            FileOutputStream fos=openFileOutput(date_text+""+".txt", Context.MODE_APPEND);
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String nowTime = sdf.format(date);
            PrintWriter writer= new PrintWriter(fos);
            writer.append(nowTime + " ");
          // writer.println(nowTime+ " ");
            writer.append(curEmo+ " ");
            writer.append(text);
            writer.append("\n");
            writer.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        /////////////////////// 파일 쓰기 ///////////////////////
        String emotion = curEmo;
        String str = text;
        // 파일 생성
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camdata"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime = sdf.format(date);

            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/CarnumData.txt", true));
            buf.append(nowTime + " "); // 날짜 쓰기
            buf.append(emotion + " ");//감정 쓰기
            buf.append(str); // 일기 쓰기
            buf.newLine(); // 개행
            buf.close();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

}

