package hr.ravilov.fontview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Callbacks {
	private static final String FILEEXT = ".ttf";

	private final BaseActivity activity;
	private final File file;

	private enum InfoField {
		TYPE (R.id.row_type, R.id.info_type),
		NAME (R.id.row_name, R.id.info_name),
		PATH (R.id.row_path, R.id.info_path),
		SIZE (R.id.row_size, R.id.info_size),
		KIND (R.id.row_kind, R.id.info_kind),
		COUNT (R.id.row_count, R.id.info_count),
		;

		private final int row_id;
		private final int cell_id;

		private InfoField(final int rid, final int cid) {
			row_id = rid;
			cell_id = cid;
		}

		public int getRowId() {
			return row_id;
		}

		public int getCellId() {
			return cell_id;
		}
	}

	private static class ItemCount {
		public int fonts = -1;
		public int dirs = -1;
	}

	public Callbacks(final BaseActivity a, final File f) {
		activity = a;
		file = f;
	}

	protected static boolean isSymlink(final File file) {
		try {
			final File canon = (file.getParent() == null) ? file : new File(file.getParentFile().getCanonicalFile(), file.getName());
			return canon.getCanonicalFile().equals(canon.getAbsoluteFile()) ? false : true;
		}
		catch (final Throwable ex) { }
		return false;
	}

	protected static ItemCount getCounts(final File dir) {
		final ItemCount ret = new ItemCount();
		ret.fonts = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String filename) {
				if (filename.length() <= FILEEXT.length()) {
					return false;
				}
				if (!filename.toLowerCase(Locale.US).endsWith(FILEEXT)) {
					return false;
				}
				final File f = new File(dir.getAbsolutePath() + File.separator + filename);
				return f.isDirectory() ? false : true;
			}
		}).length;
		final File[] list = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String filename) {
				if (filename.equals(".") || filename.equals("..")) {
					return false;
				}
				final File f = new File(dir.getAbsolutePath() + File.separator + filename);
				return f.isDirectory() ? true : false;
			}
		});
		ret.dirs = list.length;
		for (final File f : list) {
			if (isSymlink(f)) {
				continue;
			}
			final ItemCount c = getCounts(f);
			ret.fonts += c.fonts;
			ret.dirs += c.dirs;
		}
		return ret;
	}

	public void info() {
		if (file == null) {
			return;
		}
		final View info = activity.getLayoutInflater().inflate(R.layout.info, null);
		final String path = file.getAbsolutePath();
		final int start = Math.max(path.lastIndexOf(File.separatorChar) + 1, 0);
		Typeface tf = null;
		if (file.isDirectory()) {
			try {
				final ItemCount c = getCounts(file);
				setInfoProperty(info, InfoField.COUNT, String.format(Locale.getDefault(), activity.getText(R.string.item_counts).toString(),
					c.fonts,
					c.dirs
				));
			}
			catch (final Throwable ex) {
				setInfoProperty(info, InfoField.COUNT, "?");
			}
			setInfoProperty(info, InfoField.SIZE, null);
			setInfoProperty(info, InfoField.KIND, null);
		} else {
			final List<String> kind = new ArrayList<String>();
			String size = "";
			long filesize = 0;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(path);
				filesize = fis.getChannel().size();
				final String[] sfxs = { "B", "kB", "MB", "GB", "TB", "PB" };
				int idx = 0;
				while (filesize >= 1024 && idx < sfxs.length - 1) {
					filesize = Math.round(filesize / 1024f);
					idx++;
				}
				size = String.format(Locale.getDefault(), "%1$d %2$s", filesize, sfxs[idx]);
			}
			catch (final Throwable ex) {
				size = "?";
			}
			finally {
				try {
					if (fis != null) {
						fis.close();
					}
				}
				catch (final Throwable ignore) { }
			}
			try {
				tf = (filesize > 0) ? Typeface.createFromFile(path) : null;
				if (tf == null) {
					kind.add("?");
				} else {
					if (tf.isBold()) {
						kind.add(activity.getText(R.string.info_kind_bold).toString());
					}
					if (tf.isItalic()) {
						kind.add(activity.getText(R.string.info_kind_italic).toString());
					}
					if (kind.isEmpty()) {
						kind.add(activity.getText(R.string.info_kind_regular).toString());
					}
				}
			}
			catch (final Throwable ex) {
				tf = null;
				kind.add("?");
			}
			setInfoProperty(info, InfoField.SIZE, size);
			setInfoProperty(info, InfoField.KIND, Utils.join("|", kind.toArray(new String[kind.size()])));
			setInfoProperty(info, InfoField.COUNT, null);
		}
		setInfoProperty(info, InfoField.NAME, path.substring(start));
		setInfoProperty(info, InfoField.PATH, file.getParentFile().getAbsolutePath());
		setInfoProperty(info, InfoField.TYPE, file.isDirectory() ? activity.getText(R.string.info_type_directory).toString() : activity.getText(R.string.info_type_file).toString());
		final ViewGroup list = (ViewGroup)info.findViewById(R.id.list);
		if (list != null) {
			if (tf != null) {
				final String[] phrases = activity.getResources().getStringArray(R.array.sample_phrases);
				final String phrase = phrases[(int)Math.round(Math.random() * (phrases.length - 1))];
				for (int i = 0; i < list.getChildCount(); i++) {
					final View v = list.getChildAt(i);
					if (v instanceof TextView) {
						final TextView tv = (TextView)v;
						tv.setTypeface(tf);
						if (tv.getText() == null || tv.getText().length() <= 0) {
							tv.setText(phrase);
						}
					}
				}
			} else {
				list.setVisibility(View.GONE);
			}
		}
		alert(R.string.info_title, info);
	}

	public void follow() {
		if (file == null) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		final Intent intent = new Intent(activity, activity.getClass());
		intent.putExtra(MainActivity.INTENT_DIR, file.getAbsolutePath());
		activity.startActivityForResult(intent, MainActivity.REQUEST_EXIT);
	}

	public void refresh() {
		if (!(activity instanceof MainActivity)) {
			return;
		}
		final MainActivity a = (MainActivity)activity;
		if (a.list == null || a.list.getAdapter() == null) {
			return;
		}
		if (!(a.list.getAdapter() instanceof FontAdapter)) {
			return;
		}
		((FontAdapter)a.list.getAdapter()).setFonts(a.getFonts(a.dir));
	}

	public void finish() {
		activity.finish();
	}

	public void exit() {
		activity.setResult(MainActivity.RESULT_OK);
		activity.finish();
	}

	public void settings() {
		Toast.makeText(activity, activity.getText(R.string.no_settings), Toast.LENGTH_SHORT).show();
	}

	public Callbacks check() {
		if (file == null || !file.exists()) {
			throw new RuntimeException("File not found");
		}
		if (!file.canRead()) {
			throw new RuntimeException("Access denied");
		}
		return this;
	}

	private void setInfoProperty(final View view, final InfoField f, final String info) {
		if (view == null) {
			return;
		}
		final View row = view.findViewById(f.getRowId());
		if (info == null) {
			if (row != null) {
				row.setVisibility(View.GONE);
			}
		} else {
			if (row != null) {
				row.setVisibility(View.VISIBLE);
			}
			final TextView cell = (TextView)view.findViewById(f.getCellId());
			if (cell != null) {
				cell.setText(info);
			}
		}
	}

	private void alert(final int title, final View content) {
		(new AlertDialog.Builder(activity))
			.setTitle(activity.getText(title))
			.setIcon(Utils.resizeDrawable(activity, activity.getApplicationInfo().icon, 16, 16))
			.setView(content)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(final DialogInterface dialog) {
					activity.onDialogClosed(dialog, 0);
				}
			})
			.setPositiveButton(activity.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					activity.onDialogClosed(dialog, which);
				}
			})
			.create()
			.show();
		;
	}
}
