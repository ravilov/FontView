package hr.ravilov.fontview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.net.Uri;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {
	private enum MenuEntry {
		INFO,
		FOLLOW,
		;

		public int value() {
			return ordinal() + 1;
		}

		public static MenuEntry find(final int v) {
			for (final MenuEntry x : values()) {
				if (x.value() == v) {
					return x;
				}
			}
			return null;
		}
	}

	protected static final String INTENT_DIR = "dir";
	protected static final int REQUEST_EXIT = 10;
	protected static final String DEFAULT_DIR = File.separator + "system" + File.separator + "fonts";
	protected String dir = DEFAULT_DIR;
	protected ListView list = null;

	@Override
	protected void onCreate(final Bundle saved) {
		super.onCreate(saved);
		try {
			if (getIntent().getExtras().containsKey(INTENT_DIR)) {
				dir = getIntent().getExtras().getString(INTENT_DIR);
			}
		}
		catch (final Throwable ignore) { }
		try {
			dir = (new File(dir)).getCanonicalPath();
		}
		catch (final Throwable ignore) { }
		setContentView(R.layout.main);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_exit).setOnClickListener(this);
		((TextView)findViewById(R.id.dir)).setText(dir);
		list = (ListView)findViewById(android.R.id.list);
		list.setAdapter(new FontAdapter(this, null));
		list.setOnItemClickListener(this);
		list.setOnCreateContextMenuListener(this);
		findViewById(R.id.btn_refresh).setOnClickListener(this);
		(new Callbacks(this, null)).refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings: {
				(new Callbacks(this, null)).settings();
				break;
			}
			case R.id.menu_back: {
				(new Callbacks(this, null)).finish();
				break;
			}
			case R.id.menu_exit: {
				(new Callbacks(this, null)).exit();
				break;
			}
			default: {
				break;
			}
		}
		return true;
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.btn_back: {
				(new Callbacks(this, null)).finish();
				break;
			}
			case R.id.btn_exit: {
				(new Callbacks(this, null)).exit();
				break;
			}
			case R.id.btn_refresh: {
				(new Callbacks(this, null)).refresh();
				break;
			}
			default: {
				break;
			}
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final File file = (File)parent.getAdapter().getItem(position);
		if (file.isDirectory()) {
			(new Callbacks(this, file)).follow();
		} else {
			//(new Callbacks(this, file)).info();
			info(file);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo info) {
		final File file = (File)((FontAdapter)list.getAdapter()).getItem(((AdapterView.AdapterContextMenuInfo)info).position);
		if (file != null && file.isDirectory()) {
			menu.add(Menu.NONE, MenuEntry.FOLLOW.value(), Menu.NONE, getText(R.string.menu_follow));
		}
		if (!file.isDirectory() || !file.getName().equals("..")) {
			menu.add(Menu.NONE, MenuEntry.INFO.value(), Menu.NONE, getText(R.string.menu_info));
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final File file = (File)((FontAdapter)list.getAdapter()).getItem(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position);
		switch (MenuEntry.find(item.getItemId())) {
			case INFO: {
				//(new Callbacks(this, file)).info();
				info(file);
				break;
			}
			case FOLLOW: {
				(new Callbacks(this, file)).follow();
				break;
			}
			default: {
				break;
			}
		}
		return false;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_EXIT: {
				if (resultCode == BaseActivity.RESULT_OK) {
					(new Callbacks(this, null)).exit();
				}
				break;
			}
			default: {
				break;
			}
		}
	}

	/* package */ File[] getFonts(final String directory) {
		return getFonts(directory, true);
	}

	/* package */ File[] getFonts(final String directory, final boolean dirs) {
		try {
			final List<File> list = new ArrayList<File>();
			if (dirs && !directory.equals(File.separator)) {
				list.add(new File(directory + File.separatorChar + ".."));
			}
			for (final File file : (new File(directory)).listFiles()) {
				if (file.isDirectory()) {
					if (dirs) {
						list.add(file);
					}
				} else {
					final String name = file.getName().toLowerCase(Locale.US);
					if (name.endsWith(".ttf") || name.endsWith(".otf")) {
						list.add(file);
					}
				}
			}
			final File[] ret = list.toArray(new File[list.size()]);
			for (int i = 0; i < ret.length - 1; i++) {
				for (int j = i; j < ret.length; j++) {
					final boolean dir1 = ret[i].isDirectory();
					final boolean dir2 = ret[j].isDirectory();
					if ((!dir1 && dir2) || (dir1 == dir2 && ret[i].getAbsolutePath().compareToIgnoreCase(ret[j].getAbsolutePath()) > 0)) {
						final File tmp = ret[i];
						ret[i] = ret[j];
						ret[j] = tmp;
					}
				}
			}
			return ret;
		}
		catch (final Throwable ignore) { }
		return new File[0];
	}

	private void info(final File file) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setComponent(new ComponentName(this, ViewActivity.class));
		intent.setData(Uri.parse("file://" + file.getAbsolutePath()));
		startActivity(intent);
	}
}
