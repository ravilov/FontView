package hr.ravilov.fontview;

import java.io.File;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class ViewActivity extends BaseActivity {
	@Override
	protected void onCreate(final Bundle saved) {
		super.onCreate(saved);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(new View(this));
		try {
			(new Callbacks(this, new File(getIntent().getData().getPath()))).check().info();
		}
		catch (final Throwable ex) {
			Toast.makeText(this, String.format(getText(R.string.error_info).toString(), Utils.getExceptionMessage(ex)), Toast.LENGTH_LONG).show();
			System.err.println(Utils.getStackTrace(ex));
			finish();
		}
	}

	@Override
	public void onDialogClosed(final DialogInterface dialog, final int which) {
		finish();
	}
}
