package hr.ravilov.fontview;

import android.app.Activity;
import android.content.DialogInterface;

public abstract class BaseActivity extends Activity {
	// to be overridden
	public void onDialogClosed(final DialogInterface dialog, final int which) {
	}
}
