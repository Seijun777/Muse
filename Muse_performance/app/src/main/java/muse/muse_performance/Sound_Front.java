package muse.muse_performance;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import java.io.*;

public class Sound_Front extends Activity
{
	static final int RESULT = 1000;

	//テンポ
	int beat = 100;
	int beattemp;

	// Event判別用変数
	float beforex[] = {-1,-1,-1,-1};
	float beforey[] = {-1,-1,-1,-1};
	int record = 0;
	double dist[]={0,0,0,0};			// 4エリアの楽器と中心の距離
	float pushx,pushy;

	// midi用変数
	int bpm = 100;				// beat per min
	int inst[] = { 0, 0, 0, 0 };// 音色
	int vel[] = { 0, 0, 0, 0 };	// 大きさ
	int songNo = 0;				//曲セレクト
	int DrumNo = 0;				// ドラムトラック用変数

	// 楽器リスト
	int Arrange1InstList[] = new int[3];
	int Arrange2InstList[] = new int[3];
	int melodyInstList[] = new int[4];

	int nowPos = 0;	// 再生位置

	// 画面サイズうんぬん
	static final int maxX = 1200;
	static final int maxY = 1824;
	static final int maxLen = (int)Math.sqrt( maxX*maxX/4 + maxY*maxY/4 );
	static final int buttonSize = 60;

	private GraphicView graphicView;
	public MediaPlayer mediaPlayer = null;
	private FileInputStream fis = null;

	public class Vec
	{
		// 配列に楽器データ入れて、これで指定させようと思ったが挫折
		// 全エリアのフリック4方向に音色種類を全対応
		final static int UP = 0;
		final static int RIGHT = 1;
		final static int LEFT = 2;
		final static int DOWN = 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_soundfront);

		//データ受け取り
		Intent intent = getIntent();
		songNo = intent.getIntExtra("SongNo", 0);
		Arrange1InstList[0] = intent.getIntExtra("A1Inst0", 0);
		Arrange1InstList[1] = intent.getIntExtra("A1Inst1", 0);
		Arrange1InstList[2] = intent.getIntExtra("A1Inst2", 0);
		Arrange2InstList[0] = intent.getIntExtra("A2Inst0", 0);
		Arrange2InstList[1] = intent.getIntExtra("A2Inst1", 0);
		Arrange2InstList[2] = intent.getIntExtra("A2Inst2", 0);
		melodyInstList[0] = intent.getIntExtra("MInst0", 0);
		melodyInstList[1] = intent.getIntExtra("MInst1", 0);
		melodyInstList[2] = intent.getIntExtra("MInst2", 0);
		melodyInstList[3] = intent.getIntExtra("MInst3", 0);
		beat = intent.getIntExtra("BEAT", 0);

		// MediaPlayer処理
		mediaPlayer = new MediaPlayer();
		try
		{
			// 読み込み
			fis = new FileInputStream("/data/data/muse.muse_performance/files/temp.mid");

			// set
			if (fis != null) mediaPlayer.setDataSource(fis.getFD());

			// 再生待機
			mediaPlayer.prepare();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// ループ再生の設定
		mediaPlayer.setLooping(true);

		// GraphicViewのオブジェクト生成
		graphicView = new GraphicView(this);
		setContentView(graphicView);
		graphicView.setBpm(beat/2);
		graphicView.setScene(true);
		graphicView.onResume();
	}

	// midiファイル作るメソッド
	private void createMidiFile()
	{
		//音の大きさ更新
		for( int i = 0; i < vel.length; i++ )
		{
			int fixDist = 300;   // 音量微調整(中心付近は全部最大)

			// 音量計算
			if( dist[i] == 0 ) vel[i] = 0;
			else if( dist[ i ] < fixDist ) vel[i] = 127;
			else vel[i] = (int)( ( maxLen - dist[i] ) * 127 / ( maxLen - fixDist ) );
		}

		// テンポの更新
		bpm = beat;

		// midi作成
		MidiFileWriter midFile = new MidiFileWriter(getBaseContext());
		try
		{
			// MIDIファイル作成
			midFile.CreateMidiFile("temp", 5, 480);

			// トラックデータ作成
			// テンポ設定
			midFile.setTempo(bpm);

			// 音色設定
			midFile.setProgramChange((byte) 0x00, (byte) inst[0] ); // アレンジA (左上)
			midFile.setProgramChange((byte) 0x01, (byte) inst[1] ); // アレンジA (右上)
			midFile.setProgramChange((byte) 0x02, (byte) inst[2] );	// 	主旋律   (左下)
			midFile.closeTrackData();

			// トラックデータ作成
			midFile.SelectSong( songNo, DrumNo, vel );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// midi書き換え
	public void changeMidiFile()
	{
		// MediaPlayer再生中
		if ( mediaPlayer.isPlaying() )
		{
			// 現在の再生位置
			nowPos = mediaPlayer.getCurrentPosition();
			//停止
			mediaPlayer.pause();
			// テンポ操作で動くときの処理
			if( beat != bpm ) nowPos = nowPos * beat / bpm;

			// midi作成
			this.createMidiFile();

			//再生
			try
			{
				// 消去
				mediaPlayer.reset();
				// 再取得
				mediaPlayer.setDataSource(fis.getFD());
				// 待機
				mediaPlayer.prepare();
				// ループ再生の設定
				mediaPlayer.setLooping(true);
			}
			catch( IOException e)
			{
				e.printStackTrace();
			}
			// シーク
			mediaPlayer.seekTo( nowPos );
			// 再生
			mediaPlayer.start();
		}
		// MediaPlayer停止中
		else
		{
			// midi作成
			this.createMidiFile();

			//再生
			mediaPlayer.seekTo( nowPos );
			mediaPlayer.start();
		}
	}

	// フリック方向識別
	private int frickVec( float nx, float ny )
	{
		// 右側の判定
		if( pushx<nx )
		{
			// x軸移動が大きい時
			if(Math.abs(pushx-nx)>Math.abs(pushy-ny))
			{
				return Vec.RIGHT;
			}
			// y軸移動が大きい時
			else
			{
				// 上移動時
				if(pushy>ny)
				{
					return Vec.UP;
				}
				// 下移動時
				else
				{
					return Vec.DOWN;
				}
			}
		}
		// 左側の判定
		else
		{
			// x軸移動が大きい時
			if(Math.abs(pushx-nx)>Math.abs(pushy-ny))
			{
				return Vec.LEFT;
			}
			// y軸移動が大きい時
			else
			{
				//上移動時
				if(pushy>ny)
				{
					return Vec.UP;
				}
				//下移動時
				else
				{
					return Vec.DOWN;
				}
			}
		}
	}

	protected void onActivityResult(int requestCode,int resultCode,Intent intent)
	{
		super.onActivityResult(requestCode,resultCode,intent);
		if( resultCode == RESULT_OK  && requestCode == RESULT && intent != null )
		{
			//データの受取と反映
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

			// テンポ処理
			beat = intent.getIntExtra("RES_BEAT", 0);
			if( beat < 20 ) beat = 20;
			else if( beat > 240 ) beat = 240;

			if( this.songNo != intent.getIntExtra("RES_SongNo",0) )
			{
				nowPos = 0;
			}
			songNo = intent.getIntExtra("RES_SongNo",0);

			// エフェクト
			graphicView.setBpm(beat/2);

			//再生
			mediaPlayer.start();

			// midi更新
			this.changeMidiFile();
		}
	}

	//タッチイベントを拾う
	public boolean onTouchEvent(MotionEvent motionEvent)
	{
		//タッチ座標取得
		float getx = motionEvent.getX();
		float gety = motionEvent.getY();

		// 各イベント処理
		switch( motionEvent.getAction() )
		{
			//押した時
			case MotionEvent.ACTION_DOWN:
				Log.d("", "ACTION_DOWN");
				//前回のタッチ記録を消去
				for( int i = 0 ; i < 4; i++ )
				{
					beforex[i]=-1;
					beforey[i]=-1;
				}

				//タッチ開始位置を保存
				pushx=getx;
				pushy=gety;

				//フラグリセット
				record = 0;
				beattemp = 0;
				break;

			//離した時
			case MotionEvent.ACTION_UP:
				Log.d("", "ACTION_UP");
				// テンポ処理
				if( beattemp != 0 )
				{
					beat += beattemp;

					if( beat < 20 ) beat = 20;
					else if( beat > 240 ) beat = 240;

					// テンポセット
					graphicView.setBpm(beat / 2);
				}

				// テンポが変わった時
				if( beat != bpm ) this.changeMidiFile();	// midi更新

				if(Math.abs(pushx-getx)>40 || Math.abs(pushy-gety)>40){//移動距離が長いとき
					// フリック方向で各エリアごとの操作
					//左上
					if( pushx < maxX/2 && pushy < maxY/2 )
					{
						// 下フリックで消す
						if( this.frickVec( getx, gety ) == Vec.DOWN )
						{
							graphicView.setFxpoint( 0, -1 );
							graphicView.setFypoint( 0, -1 );
							graphicView.setFlagPoint( 0, 0);
							dist[ 0 ] = 0;
						}
						else this.inst[0] = Arrange1InstList[ this.frickVec( getx, gety ) ];

						graphicView.setFlick( 0, this.frickVec( getx, gety ), this.inst[0]);
					}
					// 右上
					else if( pushx > maxX/2 && pushy < maxY/2 )
					{
						// 下フリックで消す
						if( this.frickVec( getx, gety ) == Vec.DOWN )
						{
							graphicView.setFxpoint( 1, -1 );
							graphicView.setFypoint( 1, -1 );
							graphicView.setFlagPoint( 1, 0);
							dist[ 1 ] = 0;
						}
						else this.inst[1] = Arrange2InstList[ this.frickVec( getx, gety ) ];

						graphicView.setFlick( 1, this.frickVec( getx, gety ), this.inst[1]);
					}
					// 左下
					else if( pushx < maxX/2 && pushy > maxY/2 )
					{
						this.inst[2] = melodyInstList[ this.frickVec( getx, gety ) ];
						graphicView.setFlick( 2, this.frickVec( getx, gety ), this.inst[2]);
					}
					// 右下
					else if( pushx > maxX/2 && pushy > maxY/2 )
					{
						// 下フリックで消す
						if( this.frickVec( getx, gety ) == Vec.DOWN )
						{
							graphicView.setFxpoint( 3, -1 );
							graphicView.setFypoint( 3, -1 );
							graphicView.setFlagPoint( 3, 0 );
							dist[ 3 ] = 0;
						}
						else
						{
							DrumNo = this.frickVec( getx, gety );
							this.inst[3] = this.frickVec( getx, gety );
						}

						graphicView.setFlick( 3, this.frickVec( getx, gety ), this.inst[3]);
					}

					// midi更新
					this.changeMidiFile();

					// フラグ
					this.record = 1;
				}

				else {//移動距離が短いとき
					//座標判定
					// オプションへの遷移
					if (getx < buttonSize && gety < buttonSize * 2) {
						//各データの転送
						Intent intent1 = new Intent(getApplication(), Option.class);
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

						//停止
						mediaPlayer.pause();

						// 飛ぶ
						startActivityForResult(intent1, requestCode);
					}
					// 裏画面への遷移
					else if (getx > (maxX - buttonSize) && gety < buttonSize * 2) {
						//各データの転送
						Intent intent2 = new Intent(getApplication(), Sound_Back.class);
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

						//裏に飛ぶ
						startActivityForResult(intent2, requestCode);
					}
					// エリア1( 左上 )
					else if (getx < maxX / 2 && gety < maxY / 2) {
						// エフェクト
						graphicView.setFxpoint(0, getx - 10);
						graphicView.setFypoint(0, gety - 40);
						graphicView.setFlagPoint(0, 1);

						//距離
						dist[0] = Math.sqrt((getx - maxX / 2) * (getx - maxX / 2) + (gety - maxY / 2) * (gety - maxY / 2));

						// MediaPlayer処理
						this.changeMidiFile();
					}
					// エリア2( 右上 )
					else if (getx > maxX / 2 && gety < maxY / 2) {
						// 画像配置
						graphicView.setFxpoint(1, getx - 10);
						graphicView.setFypoint(1, gety - 40);
						graphicView.setFlagPoint(1, 1);

						//距離
						dist[1] = Math.sqrt((getx - maxX / 2) * (getx - maxX / 2) + (gety - maxY / 2) * (gety - maxY / 2));

						// MediaPlayer処理
						this.changeMidiFile();
					}
					// エリア3( 左下 )
					else if (getx < maxX / 2 && gety > maxY / 2) {
						// 画像配置
						// エフェクト
						graphicView.setFxpoint(2, getx - 10);
						graphicView.setFypoint(2, gety - 40);
						graphicView.setFlagPoint(2, 1);

						// 距離
						dist[2] = Math.sqrt((getx - maxX / 2) * (getx - maxX / 2) + (gety - maxY / 2) * (gety - maxY / 2));

						// MediaPlayer処理
						this.changeMidiFile();
					}
					// エリア4( 右下 )
					else if (getx > maxX / 2 && gety > maxY / 2) {
						// 画像配置
						// エフェクト
						graphicView.setFxpoint(3, getx - 10);
						graphicView.setFypoint(3, gety - 40);
						graphicView.setFlagPoint(3, 1);

						// 距離
						dist[3] = Math.sqrt((getx - maxX / 2) * (getx - maxX / 2) + (gety - maxY / 2) * (gety - maxY / 2));

						// MediaPlayer処理
						this.changeMidiFile();
					}
				}
				break;

			case MotionEvent.ACTION_MOVE:
				//スライド座標を保存
				beforex[3]=beforex[2];
				beforex[2]=beforex[1];
				beforex[1]=beforex[0];
				beforex[0]=getx;
				beforey[3]=beforey[2];
				beforey[2]=beforey[1];
				beforey[1]=beforey[0];
				beforey[0]=gety;

				// テンポ操作
				if(getx>400 && getx<800 && gety>750 && getx<1150)
				{
					//座標が記録されてない時
					if (record == 0 && beforex[3] != -1)
					{
						//右移動時
						if (getx > beforex[1] && beforex[1] > beforex[3])
						{
							record = 1;//フラグ管理
						}
						//左移動時
						else
						{
							record = 2;//フラグ管理
						}
					}
					//右移動から始めた時
					else if (record == 1)
					{
						beattemp += 1;//bpm加算
					}
					//左移動から始めた時
					else
					{
						beattemp -= 1;//bpm減算
					}
				}
				break;

			case MotionEvent.ACTION_CANCEL:
				// デバッグ表示
				Log.d("", "ACTION_CANCEL");
				break;
		}

		return false;
	}
}
