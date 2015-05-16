package hr.ravilov.fontview;

import java.io.File;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FontAdapter extends ArrayAdapter<File> {
	public FontAdapter(final Context context) {
		this(context, null);
	}

	public FontAdapter(final Context context, final File[] fonts) {
		super(context, 0);
		setFonts(fonts);
	}

	public void setFonts(final File[] fonts) {
		clear();
		if (fonts != null) {
			for (final File font : fonts) {
				add(font);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView view = (TextView)LayoutInflater.from(getContext()).inflate(R.layout.item, null);
		final File file = getItem(position);
		final String path = file.getAbsolutePath();
		final int start = Math.max(path.lastIndexOf('/') + 1, 0);
		final String pfx, sfx;
		if (file.isDirectory()) {
			pfx = "[";
			sfx = "]";
			if (!file.getName().equals("..") && getContext() instanceof MainActivity) {
				try {
					final File[] fonts = ((MainActivity)getContext()).getFonts(file.getAbsolutePath(), false);
					if (fonts != null && fonts.length > 0) {
						view.setTypeface(Typeface.createFromFile(fonts[fonts.length - 1].getAbsoluteFile()));
					}
				}
				catch (final Throwable ignore) { }
			}
		} else {
			try {
				final Typeface tf = Typeface.createFromFile(path);
				if (tf != null) {
					view.setTypeface(tf);
				}
			}
			catch (final Throwable ignore) { }
			pfx = "";
			sfx = "";
		}
		view.setText(pfx + path.substring(start) + sfx);
		return view;
	}
}
