package hr.ravilov.fontview;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class Utils {
	public static final boolean LOGGING = true;	//BuildConfig.DEBUG;

	private static String myVersion = null;
	private static String myBuild = null;

	public static final String myPackage() {
		try {
			return Utils.class.getPackage().getName();
		}
		catch (final Throwable ex) { }
		return null;
	}

	public static String getClassName(Class<?> c) {
		if (c == null) {
			return null;
		}
		final List<String> l = Arrays.asList(c.getName().split("\\."));
		return (l == null) ? null : l.get(l.size() - 1);
	}

	public static String getMyVersion(final Context context) {
		if (myVersion == null) {
			try {
				myVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			}
			catch (final Throwable ex) { }
		}
		return myVersion;
	}

	public static String getMyBuild(final Context context) {
		if (myBuild == null) {
			try {
				myBuild = context.getResources().getString(R.string.class.getField("auto_build").getInt(0));
			}
			catch (final Throwable ex) { }
		}
		return myBuild;
	}

	public static String getStackTrace(final Throwable ex) {
		final ByteArrayOutputStream s = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(s);
		ex.printStackTrace(pw);
		pw.close();
		return s.toString();
	}

	public static String getExceptionMessage(final Throwable ex) {
		if (ex == null) {
			return null;
		}
		Throwable t = ex;
		while (t != null) {
			if (ex.getMessage() != null) {
				return ex.getMessage();
			}
			t = t.getCause();
		}
		return getClassName(ex.getClass());
	}

	public static final String join(final String sep, final Object[] list) {
		if (list == null || list.length <= 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final Object s : list) {
			if (!first) {
				sb.append(sep);
			}
			sb.append((s == null) ? s : s.toString());
			first = false;
		}
		return sb.toString();
	}

	public static <T> T coalesce(T... list) {
		if (list == null) {
			return null;
		}
		for (final T item : list) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	public static Drawable resizeDrawable(final Context context, final int resId, final float widthPt, final float heightPt) {
		final Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId);
		try {
			final float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, widthPt, context.getResources().getDisplayMetrics());
			final float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, heightPt, context.getResources().getDisplayMetrics());
			if (bmp.getWidth() > width || bmp.getHeight() > height) {
				return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bmp, Math.round(width), Math.round(height), true));
			}
		}
		catch (final Throwable ex) { }
		return new BitmapDrawable(context.getResources(), bmp);
	}

	public static class Log {
		public static void i(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.i(tag, String.format(Locale.US, msg, args));
		}

		public static void w(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.w(tag, String.format(Locale.US, msg, args));
		}

		public static void d(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.d(tag, String.format(Locale.US, msg, args));
		}

		public static void v(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.v(tag, String.format(Locale.US, msg, args));
		}

		public static void e(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.e(tag, String.format(Locale.US, msg, args));
		}

		public static void x(final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.d("<>", String.format(Locale.US, msg, args));
		}
	}
}
