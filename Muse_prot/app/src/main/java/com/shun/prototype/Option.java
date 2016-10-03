package com.shun.prototype;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Button;
import android.content.Intent;

public class Option extends Activity
{
	/* メニューのための変数共 */
    // 曲セレクト
	private TextView songTextView;
    private Spinner selectSong;
    String songItems[] = { "Song1", "Song2","Song3","Song4","Song5" };
	private int songNo = 0;     // 曲番号(配列添え字)

	// 左上フリックに対応する楽器変更
	private TextView arrange1UpFlickTextView;
	private Spinner selectArrange1UpFlick;
	private TextView arrange1RightFlickTextView;
	private Spinner selectArrange1RightFlick;
	private TextView arrange1LeftFlickTextView;
	private Spinner selectArrange1LeftFlick;

	// 右上フリックに対応する楽器変更
	private TextView arrange2UpFlickTextView;
	private Spinner selectArrange2UpFlick;
	private TextView arrange2RightFlickTextView;
	private Spinner selectArrange2RightFlick;
	private TextView arrange2LeftFlickTextView;
	private Spinner selectArrange2LeftFlick;

	// 左下フリックに対応する楽器変更
	private TextView melodyUpFlickTextView;
	private Spinner selectMelodyUpFlick;
	private TextView melodyRightFlickTextView;
	private Spinner selectMelodyRightFlick;
	private TextView melodyLeftFlickTextView;
	private Spinner selectMelodyLeftFlick;
	private TextView melodyDownFlickTextView;
	private Spinner selectMelodyDownFlick;

	// Tempo変更のText
	private TextView tempoTextView;
	// Tempo変更シークバー
	private SeekBar seekbar;

	//データ保存用変数
    private int beat = 0;
	private int arrange1Inst[] = new int[3];
	private int arrange2Inst[] = new int[3];
	private int melodyInst[] = new int[4];

	/* 楽器リスト */
	// 楽器クラス
	private class Instrument
	{
		String name;    // 名前
		int pm;         // プログラムチェンジ番号

		// コンストラクタ
		Instrument( String name, int pm )
		{
			this.name = name;
			this.pm = pm;
		}
	}
	// リスト生成メソッド
	public void makeNameList(){ for( int i = 0; i < nameList.length; i++ ) nameList[i] = InstList[i].name; }
	public void makeInstList(){ for( int i = 0; i < instList.length; i++ ) instList[i] = InstList[i].pm; }

	// 本体
	private Instrument[] InstList = {
			new Instrument( "ピアノ", 0 ),
			new Instrument( "オルゴール", 11 ),
			new Instrument( "マリンバ", 13 ),
			new Instrument( "ドローパーオルガン", 17 ),
			new Instrument( "アコーディオン", 22 ),
			new Instrument( "ハーモニカ", 23 ),
			new Instrument( "ナイロン弦アコギ", 25 ),
			new Instrument( "スラップベース", 37 ),
			new Instrument( "シンセベース2", 40 ),
			new Instrument( "ヴァイオリン", 41 ),
			new Instrument( "トランペット", 57 ),
			new Instrument( "ソプラノサックス", 65 ),
			new Instrument( "三味線", 107 ),
			new Instrument( "太鼓", 117 )
	};
	public String[] nameList = new String[ InstList.length ];
	public int[] instList = new int[ InstList.length ];

	/* 選択肢の添字判断 */
	// プログラムチェンジリスト用
	public int indexOfInt( int array[], int num )
	{
		for( int i = 0; i < array.length; i++) if( num == array[i] ) return i;

		return -1;
	}
	// 楽器名用
	public int indexOfString( String array[], String s )
	{
		for( int i = 0; i < array.length; i++) if( array[i].equals( s ) ) return i;

		return -1;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        //テキスト定義
	    songTextView = (TextView)findViewById(R.id.songTextView);
	    songTextView.setText("Select Song");

		arrange1UpFlickTextView = (TextView)findViewById(R.id.arrange1UpFlickTextView);
		arrange1UpFlickTextView.setText("Select Arrange1 Up Flick Instrument");
		arrange1RightFlickTextView = (TextView)findViewById(R.id.arrange1RightFlickTextView);
		arrange1RightFlickTextView.setText("Select Arrange1 Right Flick Instrument");
		arrange1LeftFlickTextView = (TextView)findViewById(R.id.arrange1LeftFlickTextView);
		arrange1LeftFlickTextView.setText("Select Arrange1 Left Flick Instrument");

		arrange2UpFlickTextView = (TextView)findViewById(R.id.arrange2UpFlickTextView);
		arrange2UpFlickTextView.setText("Select Arrange2 Up Flick Instrument");
		arrange2RightFlickTextView = (TextView)findViewById(R.id.arrange2RightFlickTextView);
		arrange2RightFlickTextView.setText("Select Arrange2 Right Flick Instrument");
		arrange2LeftFlickTextView = (TextView)findViewById(R.id.arrange2LeftFlickTextView);
		arrange2LeftFlickTextView.setText("Select Arrange2 Left Flick Instrument");

		melodyUpFlickTextView = (TextView)findViewById(R.id.melodyUpFlickTextView);
		melodyUpFlickTextView.setText("Select Melody Up Flick Instrument");
		melodyRightFlickTextView = (TextView)findViewById(R.id.melodyRightFlickTextView);
		melodyRightFlickTextView.setText("Select Melody Right Flick Instrument");
		melodyLeftFlickTextView = (TextView)findViewById(R.id.melodyLeftFlickTextView);
		melodyLeftFlickTextView.setText("Select Melody Left Flick Instrument");
		melodyDownFlickTextView = (TextView)findViewById(R.id.melodyDownFlickTextView);
		melodyDownFlickTextView.setText("Select Melody Down Flick Instrument");

		// リスト生成
		this.makeNameList();
		this.makeInstList();

		//データの受け取り
        Intent intent = getIntent();
		songNo = intent.getIntExtra("SongNo", 0);
		arrange1Inst[0] = intent.getIntExtra("A1Inst0", 0);
		arrange1Inst[1] = intent.getIntExtra("A1Inst1", 0);
		arrange1Inst[2] = intent.getIntExtra("A1Inst2", 0);
		arrange2Inst[0] = intent.getIntExtra("A2Inst0", 0);
		arrange2Inst[1] = intent.getIntExtra("A2Inst1", 0);
		arrange2Inst[2] = intent.getIntExtra("A2Inst2", 0);
		melodyInst[0] = intent.getIntExtra("MInst0", 0);
		melodyInst[1] = intent.getIntExtra("MInst1", 0);
		melodyInst[2] = intent.getIntExtra("MInst2", 0);
		melodyInst[3] = intent.getIntExtra("MInst3", 0);
		beat = intent.getIntExtra("BEAT", 0);

	    //曲のドロップダウンメニュー
	    selectSong=(Spinner)findViewById(R.id.songSpinner);
	    ArrayAdapter<String> adapterSong = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,songItems);
	    adapterSong.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    selectSong.setAdapter(adapterSong);
	    selectSong.setSelection(songNo);
	    selectSong.setOnItemSelectedListener(new OnItemSelectedListener(){
		    public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
			    Spinner spinner=(Spinner)parent;
			    String item=(String)spinner.getSelectedItem();
			    songNo = indexOfString( songItems, item );
		}
		    public void onNothingSelected(AdapterView<?> parent){}
	    });

		// 左上の上フリックのドロップダウンメニュー
		selectArrange1UpFlick = (Spinner)findViewById(R.id.selectArrange1UpFlickSpinner);
		ArrayAdapter<String> adapterArrange1UpFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange1UpFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange1UpFlick.setAdapter(adapterArrange1UpFlickItems);
		selectArrange1UpFlick.setSelection( this.indexOfInt( instList, arrange1Inst[0] ) );
		selectArrange1UpFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange1Inst[ 0 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左上の右フリックのドロップダウンメニュー
		selectArrange1RightFlick = (Spinner)findViewById(R.id.selectArrange1RightFlickSpinner);
		ArrayAdapter<String> adapterArrange1RightFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange1RightFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange1RightFlick.setAdapter(adapterArrange1RightFlickItems);
		selectArrange1RightFlick.setSelection( this.indexOfInt( instList, arrange1Inst[1] ) );
		selectArrange1RightFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange1Inst[ 1 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左上の左フリックのドロップダウンメニュー
		selectArrange1LeftFlick = (Spinner)findViewById(R.id.selectArrange1LeftFlickSpinner);
		ArrayAdapter<String> adapterArrange1LeftFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange1LeftFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange1LeftFlick.setAdapter(adapterArrange1LeftFlickItems);
		selectArrange1LeftFlick.setSelection( this.indexOfInt( instList, arrange1Inst[2] ) );
		selectArrange1LeftFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange1Inst[ 2 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 右上の上フリックのドロップダウンメニュー
		selectArrange2UpFlick = (Spinner)findViewById(R.id.selectArrange2UpFlickSpinner);
		ArrayAdapter<String> adapterArrange2UpFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange2UpFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange2UpFlick.setAdapter(adapterArrange2UpFlickItems);
		selectArrange2UpFlick.setSelection( this.indexOfInt( instList, arrange2Inst[0] ) );
		selectArrange2UpFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange2Inst[ 0 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 右上の右フリックのドロップダウンメニュー
		selectArrange2RightFlick = (Spinner)findViewById(R.id.selectArrange2RightFlickSpinner);
		ArrayAdapter<String> adapterArrange2RightFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange2RightFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange2RightFlick.setAdapter(adapterArrange2RightFlickItems);
		selectArrange2RightFlick.setSelection( this.indexOfInt( instList, arrange2Inst[1] ) );
		selectArrange2RightFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange2Inst[ 1 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 右上の左フリックのドロップダウンメニュー
		selectArrange2LeftFlick = (Spinner)findViewById(R.id.selectArrange2LeftFlickSpinner);
		ArrayAdapter<String> adapterArrange2LeftFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterArrange2LeftFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectArrange2LeftFlick.setAdapter(adapterArrange2LeftFlickItems);
		selectArrange2LeftFlick.setSelection( this.indexOfInt( instList, arrange2Inst[2] ) );
		selectArrange2LeftFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				arrange2Inst[ 2 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左下の上フリックのドロップダウンメニュー
		selectMelodyUpFlick = (Spinner)findViewById(R.id.selectMelodyUpFlickSpinner);
		ArrayAdapter<String> adapterMelodyUpFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterMelodyUpFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectMelodyUpFlick.setAdapter(adapterMelodyUpFlickItems);
		selectMelodyUpFlick.setSelection( this.indexOfInt( instList, melodyInst[0] ) );
		selectMelodyUpFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				melodyInst[ 0 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左下の右フリックのドロップダウンメニュー
		selectMelodyRightFlick = (Spinner)findViewById(R.id.selectMelodyRightFlickSpinner);
		ArrayAdapter<String> adapterMelodyRightFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterMelodyRightFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectMelodyRightFlick.setAdapter(adapterMelodyRightFlickItems);
		selectMelodyRightFlick.setSelection( this.indexOfInt( instList, melodyInst[1] ) );
		selectMelodyRightFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				melodyInst[ 1 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左下の左フリックのドロップダウンメニュー
		selectMelodyLeftFlick = (Spinner)findViewById(R.id.selectMelodyLeftFlickSpinner);
		ArrayAdapter<String> adapterMelodyLeftFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterMelodyLeftFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectMelodyLeftFlick.setAdapter(adapterMelodyLeftFlickItems);
		selectMelodyLeftFlick.setSelection( this.indexOfInt( instList, melodyInst[2] ) );
		selectMelodyLeftFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				melodyInst[ 2 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		// 左下の下フリックのドロップダウンメニュー
		selectMelodyDownFlick = (Spinner)findViewById(R.id.selectMelodyDownFlickSpinner);
		ArrayAdapter<String> adapterMelodyDownFlickItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, nameList);
		adapterMelodyDownFlickItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectMelodyDownFlick.setAdapter(adapterMelodyDownFlickItems);
		selectMelodyDownFlick.setSelection( this.indexOfInt( instList, melodyInst[3] ) );
		selectMelodyDownFlick.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View viw,int arg2,long arg3){
				Spinner spinner=(Spinner)parent;
				String item=(String)spinner.getSelectedItem();
				melodyInst[ 3 ] = InstList[ indexOfString( nameList, item ) ].pm;
			}
			public void onNothingSelected(AdapterView<?> parent){}
		});

		//テンポのシークバー
		tempoTextView=(TextView)findViewById(R.id.text_view5);
		tempoTextView.setText("bpm"+beat);
        seekbar=(SeekBar)findViewById(R.id.tempo);
        seekbar.setMax(255);
        seekbar.setProgress(beat);
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    @Override
                    public void onProgressChanged(SeekBar seekbar,int progress,boolean fromUser){
                        beat=seekbar.getProgress();
	                    tempoTextView.setText("bpm"+beat);
                    }
                    public void onStartTrackingTouch(SeekBar seekbar){}
                    public void onStopTrackingTouch(SeekBar seekbar){                    }
                });

        Button sendButton = (Button) findViewById(R.id.return_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
	            intent.putExtra("RES_SongNo", songNo);
	            intent.putExtra("RES_A1Inst0", arrange2Inst[0]);
	            intent.putExtra("RES_A1Inst1", arrange2Inst[1]);
	            intent.putExtra("RES_A1Inst2", arrange2Inst[2]);
	            intent.putExtra("RES_A2Inst0", arrange2Inst[0]);
	            intent.putExtra("RES_A2Inst1", arrange2Inst[1]);
	            intent.putExtra("RES_A2Inst2", arrange2Inst[2]);
	            intent.putExtra("RES_MInst0", melodyInst[0]);
	            intent.putExtra("RES_MInst1", melodyInst[1]);
	            intent.putExtra("RES_MInst2", melodyInst[2]);
	            intent.putExtra("RES_MInst3", melodyInst[3]);
	            intent.putExtra("RES_BEAT", beat);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }
}
