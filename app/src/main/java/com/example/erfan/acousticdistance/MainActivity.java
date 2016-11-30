package com.example.erfan.acousticdistance;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private PlayToneThread genTone;
    private Decoder decoder=new Decoder();
    long sent=0;
    private  GenerateTone gt=new GenerateTone();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn=(Button)findViewById(R.id.button2);
        btn.setText("Receiving");
        decoder.activity=this;
        decoder.trigger();
    }

    public void play(View v){

        switch (v.getId()){
            case R.id.button:{
                EditText et=(EditText)findViewById(R.id.tv);

                sent=System.currentTimeMillis();
                genTone=null;
                genTone=new PlayToneThread(19000,0.5,Float.parseFloat(et.getText().toString()));
                genTone.start();
                break;
            }
            case R.id.button2:{
                SharedVar.mode=1-SharedVar.mode;
                Button btn=(Button)findViewById(R.id.button2);
                if(SharedVar.mode==0)
                    btn.setText("Receiving");
                else
                    btn.setText("Sending");
                }
        }

    }
    int counter=0;
    public void detected(int x,long ms){
        if(x==2){
            counter=1-counter;
            if(counter==0){
                Button btn=(Button)findViewById(R.id.button3);
                btn.setBackgroundColor(Color.argb(255, 255, 0, 0));
            }else {
                Button btn=(Button)findViewById(R.id.button3);
                btn.setBackgroundColor(Color.argb(255, 0, 255,0));
            }
        }
        if(x==71){
            EditText et=(EditText)findViewById(R.id.tv);
            et.setText(Integer.toString(71));
            genTone=null;
            genTone=new PlayToneThread(19600,0.3,0.1f);
            genTone.start();
        }
        if(x==57){
            EditText et=(EditText)findViewById(R.id.tv);
            et.setText(Long.toString((ms - sent)));
        }
    }




}
