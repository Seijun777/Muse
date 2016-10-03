package muse.muse_performance;

import android.media.SoundPool;
import android.content.Context;

public class BackSE {
	//soundPool用
	int maxStreams;
	int streamType;
	int srcQuality;
	Context context;

	private static SoundPool soundPool;
	private static int[] soundID = new int[5];

	//コンストラクタ
	public BackSE(int max,int type,int src,Context ct){
		maxStreams=max;
		streamType=type;
		srcQuality=src;
		context=ct;
	}

	//音データ読み込み
	public void soundLoad(){
		soundPool = new SoundPool(maxStreams,streamType,srcQuality);
		soundID[0] = soundPool.load(context,R.raw.flog ,1);
		soundID[1] = soundPool.load(context,R.raw.hiyoko,1);
		soundID[2] = soundPool.load(context,R.raw.semi,1);
		soundID[3] = soundPool.load(context,R.raw.hand,1);
		soundID[4] = soundPool.load(context,R.raw.drop,1);
	}

	//鳴らす
	public void soundPlay(int i){
		soundPool.play(soundID[i],0.2f,0.2f,0,0,1);
	}
}
