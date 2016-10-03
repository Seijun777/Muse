package com.shun.prototype;

import java.io.*;
import java.util.ArrayList;
import android.content.Context;

public class MidiFileWriter
{
    private String FILE_ID = ".mid";

    //　MThd　データ
    private byte[] HEADER_TAG = {0x4d,0x54,0x68,0x64};
    private byte[] HEADER_DATA_LEN = {0x00,0x00,0x00,0x06};
    private byte[] MIDI_FORMAT = {0x00,0x01};

    // MTrk データ
    private byte[] TRACK_TAG = {0x4d,0x54,0x72,0x6b};
    private byte[] TRACK_END = {0x00,(byte)0xff,0x2f,0x00};

    private FileOutputStream fos = null;
    private Context context = null;
    private ArrayList<TrackData> TrackDataList = new ArrayList<TrackData>();
    private int quarterNoteDeltaTime;

    // コンストラクタ
    public MidiFileWriter(Context contxt)
    {
        this.context = contxt;
    }

    //オリジナル１曲目
    protected void Song1Melody( byte ch, byte vel )
    {
        try
        {
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_010), (byte)MidiFileWriter.NoteTone.G5, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.FF5, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.G5, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_025), (byte)MidiFileWriter.NoteTone.D5, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)MidiFileWriter.NoteTone.B4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_010), (byte)MidiFileWriter.NoteTone.G4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_010), (byte)MidiFileWriter.NoteTone.G4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_025), (byte)MidiFileWriter.NoteTone.A4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)MidiFileWriter.NoteTone.B4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_010), (byte)MidiFileWriter.NoteTone.D5, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_025), (byte)MidiFileWriter.NoteTone.E5, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)MidiFileWriter.NoteTone.D5, (byte)0x00);

            this.closeTrackData();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    // 打楽器
    protected void Song1PercussionA( byte ch, byte vel )
    {
        try
        {
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)35, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

            this.closeTrackData();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

	protected void Song1PercussionB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)35, (byte)0x00);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)35, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)35, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_040), (byte)54, (byte)0x00);

			this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected void Song1Percuss( int no, byte ch, byte vel )
	{
		switch( no )
		{
			case 0:
				this.Song1PercussionA( ch, vel);
				break;

			case 1:
				this.Song1PercussionB( ch, vel );
				break;

			default:
				this.Song1PercussionA( ch, vel);
				break;
		}
	}

	protected void Song1ArrangeA( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.C3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.E3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.C3, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G2, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G2, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D3, vel);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.FF3, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_020), (byte)MidiFileWriter.NoteTone.G3, (byte)0x00);

            this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected void Song1ArrangeB( byte ch, byte vel )
	{
		try
		{
			this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
			this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
			this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.A4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.G4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.D5, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);
            this.addNoteOn( ch, 0, (byte)MidiFileWriter.NoteTone.B4, vel);
            this.addNoteOn( ch, this.getNoteDeltaTime(MidiFileWriter.NoteTime.Note_080), (byte)MidiFileWriter.NoteTone.C4, (byte)0x00);

            this.closeTrackData();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

    // 2曲目

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

    /* 定数 */
    // Trackデータ
    protected class TrackData
    {
        protected int length = 0;
        protected byte[] data;
    }

    // NOTE LEN
    public class NoteTime
    {
        public final static double Note_010 = 4;    // 全音符
        public final static double Note_025 = 3;    // 付点2分音符
        public final static double Note_020 = 2;    // 2分音符
        public final static double Note_045 = 1.5;  // 付点4分音符
        public final static double Note_040 = 1;    // 4分音符
        public final static double Note_085 = 0.75; // 付点8分音符
        public final static double Note_080 = 0.5;  // 8分音符
        public final static double Note_160 = 0.25; // 16分音符
    }

    // NOTE TONE
    public class NoteTone
    {
        public final static int C0 = 12;
        public final static int CC0 = 13;
        public final static int D0 = 14;
        public final static int DD0 = 15;
        public final static int E0 = 16;
        public final static int F0 = 17;
        public final static int FF0 = 18;
        public final static int G0 = 19;
        public final static int GG0 = 20;
        public final static int A0 = 21;
        public final static int AA0 = 22;
        public final static int B0 = 23;

        public final static int C1 = 24;
        public final static int CC1 = 25;
        public final static int D1 = 26;
        public final static int DD1 = 27;
        public final static int E1 = 28;
        public final static int F1 = 29;
        public final static int FF1 = 30;
        public final static int G1 = 31;
        public final static int GG1 = 32;
        public final static int A1 = 33;
        public final static int AA1 = 34;
        public final static int B1 = 35;
        public final static int C2 = 36;
        public final static int CC2 = 37;
        public final static int D2 = 38;
        public final static int DD2 = 39;
        public final static int E2 = 40;
        public final static int F2 = 41;
        public final static int FF2 = 42;
        public final static int G2 = 43;
        public final static int GG2 = 44;
        public final static int A2 = 45;
        public final static int AA2 = 46;
        public final static int B2 = 47;
        public final static int C3 = 48;
        public final static int CC3 = 49;
        public final static int D3 = 50;
        public final static int DD3 = 51;
        public final static int E3 = 52;
        public final static int F3 = 53;
        public final static int FF3 = 54;
        public final static int G3 = 55;
        public final static int GG3 = 56;
        public final static int A3 = 57;
        public final static int AA3 = 58;
        public final static int B3 = 59;
        public final static int C4 = 60;
        public final static int CC4 = 61;
        public final static int D4 = 62;
        public final static int DD4 = 63;
        public final static int E4 = 64;
        public final static int F4 = 65;
        public final static int FF4 = 66;
        public final static int G4 = 67;
        public final static int GG4 = 68;
        public final static int A4 = 69;
        public final static int AA4 = 70;
        public final static int B4 = 71;

        public final static int C5 = 72;
        public final static int CC5 = 73;
        public final static int D5 = 74;
        public final static int DD5 = 75;
        public final static int E5 = 76;
        public final static int F5 = 77;
        public final static int FF5 = 78;
        public final static int G5 = 79;
        public final static int GG5 = 80;
        public final static int A5 = 81;
        public final static int AA5 = 82;
        public final static int B5 = 83;
        public final static int C6 = 84;
        public final static int CC6 = 85;
        public final static int D6 = 86;
        public final static int DD6 = 87;
        public final static int E6 = 88;
        public final static int F6 = 89;
        public final static int FF6 = 90;
        public final static int G6 = 91;
        public final static int GG6 = 92;
        public final static int A6 = 93;
        public final static int AA6 = 94;
        public final static int B6 = 95;
        public final static int C7 = 96;
        public final static int CC7 = 97;
        public final static int D7 = 98;
        public final static int DD7 = 99;
        public final static int E7 = 100;
        public final static int F7 = 101;
        public final static int FF7 = 102;
        public final static int G7 = 103;
        public final static int GG7 = 104;
        public final static int A7 = 105;
        public final static int AA7 = 106;
        public final static int B7 = 107;
        public final static int C8 = 108;
        public final static int CC8 = 109;
        public final static int D8 = 110;
        public final static int DD8 = 111;
        public final static int E8 = 112;
        public final static int F8 = 113;
        public final static int FF8 = 114;
        public final static int G8 = 115;
        public final static int GG8 = 116;
        public final static int A8 = 117;
        public final static int AA8 = 118;
        public final static int B8 = 119;
        public final static int C9 = 120;
        public final static int CC9 = 121;
        public final static int D9 = 122;
        public final static int DD9 = 123;
        public final static int E9 = 124;
        public final static int F9 = 125;
        public final static int FF9 = 126;
        public final static int G9 = 127;
}
}
