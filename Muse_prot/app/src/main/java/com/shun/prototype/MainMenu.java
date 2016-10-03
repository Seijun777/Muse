package com.shun.prototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity
{
    static final int RESULT = 1000;
    int beat=100;
    int songNo=0;

    int Arrange1InstList[] = { 0, 22, 40 };
    int Arrange2InstList[] = { 0, 22, 40 };
    int melodyInstList[] = { 0, 53, 23, 25 };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

	    // SoundFrontへ遷移するボタン
        Button sendButton1 = (Button)findViewById(R.id.send_button1);
        sendButton1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent1 = new Intent(getApplication(),Sound_Front.class);
                intent1.putExtra("SongNo", songNo);
                intent1.putExtra("A1Inst0", Arrange1InstList[0]);
                intent1.putExtra("A1Inst1", Arrange1InstList[1]);
                intent1.putExtra("A1Inst2", Arrange1InstList[2]);
                intent1.putExtra("A2Inst0", Arrange2InstList[0]);
                intent1.putExtra("A2Inst1", Arrange2InstList[1]);
                intent1.putExtra("A2Inst2", Arrange2InstList[2]);
                intent1.putExtra("MInst0", melodyInstList[0]);
                intent1.putExtra("MInst1", melodyInstList[1]);
                intent1.putExtra("MInst2", melodyInstList[2]);
                intent1.putExtra("MInst3", melodyInstList[3]);
                intent1.putExtra("BEAT", beat);
                int requestCode = RESULT;
                startActivityForResult(intent1,requestCode);
            }
        });

	    // Optionへ遷移するボタン
        Button sendButton2 = (Button)findViewById(R.id.send_button2);
        sendButton2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent2 = new Intent(getApplication(),Option.class);
	            intent2.putExtra("SongNo", songNo);
	            intent2.putExtra("A1Inst0", Arrange1InstList[0]);
	            intent2.putExtra("A1Inst1", Arrange1InstList[1]);
	            intent2.putExtra("A1Inst2", Arrange1InstList[2]);
	            intent2.putExtra("A2Inst0", Arrange2InstList[0]);
	            intent2.putExtra("A2Inst1", Arrange2InstList[1]);
	            intent2.putExtra("A2Inst2", Arrange2InstList[2]);
	            intent2.putExtra("MInst0", melodyInstList[0]);
	            intent2.putExtra("MInst1", melodyInstList[1]);
	            intent2.putExtra("MInst2", melodyInstList[2]);
	            intent2.putExtra("MInst3", melodyInstList[3]);
	            intent2.putExtra("BEAT", beat);
                int requestCode = RESULT;
                startActivityForResult(intent2,requestCode);
            }
        });
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent intent)
    {
        super.onActivityResult(requestCode,resultCode,intent);
        if( resultCode == RESULT_OK  && requestCode == RESULT && null != intent )
        {
	        songNo = intent.getIntExtra("RES_SongNo", 0);
	        Arrange1InstList[0] = intent.getIntExtra("RES_A1Inst0", 0);
	        Arrange1InstList[1] = intent.getIntExtra("RES_A1Inst1", 0);
	        Arrange1InstList[2] = intent.getIntExtra("RES_A1Inst2", 0);
	        Arrange2InstList[0] = intent.getIntExtra("RES_A2Inst0", 0);
	        Arrange2InstList[1] = intent.getIntExtra("RES_A2Inst1", 0);
	        Arrange2InstList[2] = intent.getIntExtra("RES_A2Inst2", 0);
	        melodyInstList[0] = intent.getIntExtra("RES_MInst0", 0);
	        melodyInstList[1] = intent.getIntExtra("RES_MInst1", 0);
	        melodyInstList[2] = intent.getIntExtra("RES_MInst2", 0);
	        melodyInstList[3] = intent.getIntExtra("RES_MInst3", 0);
	        beat = intent.getIntExtra("RES_BEAT", 0);
        }
    }
}