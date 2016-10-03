package muse.muse_performance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Starting extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting);

		Button sendbutton1=(Button)findViewById(R.id.send_button);//MainMenu遷移のボタン
		sendbutton1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent1=new Intent(getApplication(),MainMenu.class);
				startActivity(intent1);
			}
		});
	}
}
