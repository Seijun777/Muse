package muse.muse_performance;

import java.io.*;
import java.util.ArrayList;
import android.content.Context;

public class MidiFileWriter
{
	//　MThd データ
	private byte[] HEADER_TAG = {0x4d,0x54,0x68,0x64};
	private byte[] HEADER_DATA_LEN = {0x00,0x00,0x00,0x06};
	private byte[] MIDI_FORMAT = {0x00,0x01};

	// MTrk データ
	private byte[] TRACK_TAG = {0x4d,0x54,0x72,0x6b};
	private byte[] TRACK_END = {0x00,(byte)0xff,0x2f,0x00};

	private FileOutputStream fos = null;
	private String FILE_ID = ".mid";
	private Context context = null;
	private int quarterNoteDeltaTime;
	// Trackデータ
	private ArrayList<TrackData> TrackDataList = new ArrayList<TrackData>();
	protected class TrackData
	{
		protected int length = 0;
		protected byte[] data;
	}

	// コンストラクタ
	public MidiFileWriter(Context context)
	{
		this.context = context;
	}

	// midiファイル作成
	public boolean CreateMidiFile( String fileName, int trackNo, int dTime )
	{
		// エラー処理
		if (fos != null) return false;

		String flname = fileName + FILE_ID;
		this.quarterNoteDeltaTime = dTime;

		try
		{
			// ファイル作成
			this.fos = context.openFileOutput(flname, Context.MODE_PRIVATE);
			// ヘッダ
			this.fos.write(HEADER_TAG);
			// データサイズ
			this.fos.write(HEADER_DATA_LEN);
			// データフォーマット
			this.fos.write(MIDI_FORMAT);

			// トラック数
			byte b = (byte)( ( trackNo & 0x0000ff00 ) >> 8 );
			this.fos.write(b);
			b = (byte)( trackNo & 0x000000ff );
			this.fos.write(b);

			// デルタタイム
			b = (byte)( ( dTime & 0x0000ff00 ) >> 8 );
			this.fos.write(b);
			b = (byte)( dTime & 0x000000ff );
			this.fos.write(b);
		}
		catch( Exception e )
		{
			e.printStackTrace();

			// 後処理
			this.Release();
			return false;
		}

		return true;
	}

	// テンポを設定
	public void setTempo( int bpm )
	{
		int tempoL = 60 * 1000000 / bpm; //[micro sec / beat]
		TrackData trackData = new TrackData();
		trackData.length = 7;
		trackData.data = new byte[7];

		// MetaMessage
		trackData.data[0] = (byte)0x00;
		trackData.data[1] = (byte)0xff;
		trackData.data[2] = (byte)0x51;
		trackData.data[3] = (byte)0x03;
		trackData.data[4] = (byte)( tempoL /256 /256 );
		trackData.data[5] = (byte)( tempoL /256 /256 );
		trackData.data[6] = (byte)( tempoL /256 /256 );

		// 追加
		TrackDataList.add(trackData);
	}

	// プログラムチェンジ
	public void setProgramChange( byte ch, byte no )
	{
		TrackData trackData = new TrackData();
		trackData.length = 3;

		// message
		trackData.data = new byte[4];
		trackData.data[0] = (byte)0x00;
		trackData.data[1] = (byte)(0xc0 | ch);
		trackData.data[2] = no;

		// 追加
		TrackDataList.add(trackData);
	}

	// Trackデータ追加
	public void addTrackData(byte[] data, int length)
	{
		// エラー処理
		if( length <= 0 ) return;

		TrackData trackData = new TrackData();
		trackData.length = length;
		trackData.data = new byte[length];

		// リストに追加
		for( int i = 0; i < length; i++ ) trackData.data[i] = data[i];
		TrackDataList.add(trackData);
	}

	// NOTE ON
	public void addNoteOn( byte chNo, int deltaTime, byte NoteTone, byte velocity )
	{
		byte[] dltime = this.deltaTime(deltaTime);
		int dataLen = dltime.length + 3;
		byte data[] = new byte[dataLen];

		for(int i = 0; i < dltime.length; i++)
		{
			data[i] = dltime[i];
		}

		// MidiMessage
		data[ dltime.length + 0 ] = (byte)(0x90 | chNo);
		data[ dltime.length + 1 ] = NoteTone;
		data[ dltime.length + 2 ] = velocity;

		// リストに追加
		addTrackData(data, dataLen);
	}

	// Trackデータクローズ
	public void closeTrackData()
	{
		// データ長　データ終端データサイズを初期値とする
		int dataLen = 4;

		for( int i = 0; i < TrackDataList.size(); i++ )
		{
			dataLen += TrackDataList.get(i).length;
		}

		try
		{
			// Trackヘッダ
			fos.write( TRACK_TAG );

			// データサイズ
			byte work = (byte)( (dataLen & 0xff000000) >> 24 );
			fos.write(work);
			work = (byte)( (dataLen & 0x00ff0000) >> 16 );
			fos.write(work);
			work = (byte)( (dataLen & 0x0000ff00) >> 8 );
			fos.write(work);
			work = (byte)( dataLen & 0x000000ff );
			fos.write(work);

			// Trackデータ展開
			for( int i = 0; i < TrackDataList.size(); i++ )
			{
				for( int c = 0; c < TrackDataList.get(i).length; c++ )
				{
					fos.write(TrackDataList.get(i).data[c]);
				}
			}

			// Track終端
			fos.write(TRACK_END);
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		// トラックデータ消去
		this.clearTrackData();
	}

	// MIDIファイル作成完了
	public void Release()
	{
		// エラー処理
		if( this.fos != null ) return;

		// ファイルクローズ
		try
		{
			this.fos.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		// 初期化
		this.fos = null;
	}

	// 音符のデルタタイムを計算
	public int getNoteDeltaTime( double noteTime )
	{
		return (int)( this.quarterNoteDeltaTime * noteTime );
	}

	// Trackデータクリア
	private void clearTrackData()
	{
		this.TrackDataList.clear();
	}

	// deltaTimeの計算
	private byte[] deltaTime(int deltaTime)
	{
		// 配列の大きさ計算
		int size = 1;
		if( deltaTime >= 0x00200000 ) size = 4;
		else if( deltaTime >= 0x00004000 ) size = 3;
		else if( deltaTime >= 0x00000080 ) size = 2;

		byte[] dltTime = new byte[size];
		// エラー処理
		if( dltTime == null ) return null;

		// 生成
		int dt = deltaTime;
		for( int i = ( size - 1 ); i >= 0; i-- )
		{
			byte work = (byte)( dt & 0x0000007f );
			dltTime[i] = (byte)(work | 0x80);
			dt -= work;
			dt >>= 7;
		}
		dltTime[ size-1 ] = (byte)(dltTime[ size-1 ] & 0x7f);

		return dltTime;
	}

	// 曲譜面選択
	public void SelectSong( int songNo, int dNo, int[] vel )
	{
		switch( songNo )
		{
			case 0:
				this.Song1ArrangeA((byte) 0x00, (byte) vel[0]);
				this.Song1ArrangeB((byte) 0x01, (byte) vel[1]);
				this.Song1Melody((byte) 0x02, (byte) vel[2]);
				this.Song1Percuss( dNo, (byte) vel[3]);
				break;

			case 1:
				this.Song2ArrangeA((byte) 0x00, (byte) vel[0]);
				this.Song2ArrangeB((byte) 0x01, (byte) vel[1]);
				this.Song2Melody((byte) 0x02, (byte) vel[2]);
				this.Song2Percuss( dNo, (byte) vel[3]);
				break;

			case 2:
				this.Song3ArrangeA((byte) 0x00, (byte) vel[0]);
				this.Song3ArrangeB((byte) 0x01, (byte) vel[1]);
				this.Song3Melody((byte) 0x02, (byte) vel[2]);
				this.Song3Percuss( dNo, (byte) vel[3]);
				break;

			case 3:
				this.Song4ArrangeA((byte) 0x00, (byte) vel[0]);
				this.Song4ArrangeB((byte) 0x01, (byte) vel[1]);
				this.Song4Melody((byte) 0x02, (byte) vel[2]);
				this.Song4Percuss( dNo, (byte) vel[3]);
				break;

			default:
				this.Song1ArrangeA((byte) 0x00, (byte) vel[0]);
				this.Song1ArrangeB((byte) 0x01, (byte) vel[1]);
				this.Song1Melody((byte) 0x02, (byte) vel[2]);
				this.Song1PercussionA((byte) 0x09, (byte) vel[3]);
				break;
		}
	}

	/* オリジナル1 */
	//主旋律1
	protected void Song1Melody( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.G5, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.FF5, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.G5, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_025), (byte)NoteTone.D5, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.B4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.G4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.G4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_025), (byte)NoteTone.A4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.B4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.D5, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_025), (byte)NoteTone.E5, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.D5, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムA1
	protected void Song1PercussionA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムB1
	protected void Song1PercussionB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);


			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//共通ドラムパート
	protected void SongPercussionC( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラム管理1
	protected void Song1Percuss( int no, byte vel )
	{
		switch( no )
		{
			case 0:
				this.SongPercussionC( (byte) 0x09, vel);
				break;

			case 1:
				this.Song1PercussionA( (byte) 0x09, vel);
				break;

			case 2:
				this.Song1PercussionB( (byte) 0x09, vel );
				break;

			default:
				this.SongPercussionC( (byte) 0x09, vel);
				break;
		}
	}

	// 伴奏A1
	protected void Song1ArrangeA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G2, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.G3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 伴奏B1
	protected void Song1ArrangeB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	/* オリジナル2 */
	//主旋律2
	protected void Song2Melody( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_025), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);


			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_025), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムA2
	protected void Song2PercussionA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);


			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムB2
	protected void Song2PercussionB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラム管理2
	protected void Song2Percuss( int no, byte vel )
	{
		switch( no )
		{
			case 0:
				this.SongPercussionC( (byte) 0x09, vel);
				break;

			case 1:
				this.Song2PercussionA( (byte) 0x09, vel);
				break;

			case 2:
				this.Song2PercussionB( (byte) 0x09, vel );
				break;

			default:
				this.SongPercussionC( (byte) 0x09, vel);
				break;
		}
	}

	// 伴奏A2
	protected void Song2ArrangeA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			// 小節またぐ　080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			//             080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			// 小節またぐ　080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			//             080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			// 小節またぐ　080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			//             080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			// 小節またぐ　080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			//             080
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 伴奏B2
	protected void Song2ArrangeB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);



			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	/* オリジナル3 */
	//主旋律3
	protected void Song3Melody( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//ドラムA3
	protected void Song3PercussionA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//ドラムB3
	protected void Song3PercussionB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);


			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//ドラム管理3
	protected void Song3Percuss( int no, byte vel )
	{
		switch( no )
		{
			case 0:
				this.SongPercussionC( (byte) 0x09, vel);
				break;

			case 1:
				this.Song3PercussionA( (byte) 0x09, vel);
				break;

			case 2:
				this.Song3PercussionB( (byte) 0x09, vel );
				break;

			default:
				this.SongPercussionC( (byte) 0x09, vel);
				break;
		}
	}

	// 伴奏A3
	protected void Song3ArrangeA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.C2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_045), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.D2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//伴奏B3
	protected void Song3ArrangeB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.C5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.G5, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.B5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)NoteTone.C3, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	/* オリジナル4 */
	//主旋律4
	protected void Song4Melody( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.GG4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.A4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C4, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_020), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)NoteTone.C4, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムA4
	protected void Song4PercussionA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)54, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラムB4
	protected void Song4PercussionB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_160), (byte)54, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)40, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_040), (byte)35, (byte)0x00);


			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ドラム管理4
	protected void Song4Percuss( int no, byte vel )
	{
		switch( no )
		{
			case 0:
				this.SongPercussionC( (byte) 0x09, vel);
				break;

			case 1:
				this.Song3PercussionA( (byte) 0x09, vel);
				break;

			case 2:
				this.Song3PercussionB( (byte) 0x09, vel );
				break;

			default:
				this.SongPercussionC( (byte) 0x09, vel);
				break;
		}
	}

	// 伴奏A4
	protected void Song4ArrangeA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.CC3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.CC3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.B2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.GG2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.E2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.CC3, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.A2, vel);
			this.addNoteOn( ch, 0, (byte)NoteTone.FF2, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_010), (byte)NoteTone.C3, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//　伴奏B4
	protected void Song4ArrangeB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E7, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(NoteTime.Note_080), (byte)NoteTone.C3, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

}


