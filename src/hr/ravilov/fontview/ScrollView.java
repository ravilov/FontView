// original: http://www.coolwebproject.com/android-2d-scrolling-revised/
package hr.ravilov.fontview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Scroller;

public class ScrollView extends android.widget.FrameLayout {
	private static final boolean USE_FADING_EDGE = true;
	private static final int ANIMATED_SCROLL_GAP = 250;
	private static final Method _awakenScrollBars;
	static {
		Method m = null;
		try {
			m = View.class.getDeclaredMethod("awakenScrollBars", new Class<?>[] {
				int.class,
			});
			m.setAccessible(true);
		}
		catch (final Throwable ex) {
			m = null;
		}
		_awakenScrollBars = m;
	}

	private long mLastScroll;
	private Scroller mScroller;
	private float mLastMotionY;
	private float mLastMotionX;
	private boolean mIsBeingDragged = false;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private int mMinimumVelocity;
	//private int mMaximumVelocity;

	public ScrollView(final Context context) {
		super(context);
		init();
	}

	public ScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mScroller = new Scroller(getContext());
		setFocusable(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);
		if (USE_FADING_EDGE) {
			setVerticalFadingEdgeEnabled(true);
			setHorizontalFadingEdgeEnabled(true);
		}
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		//mMaximumVelocity = mMinimumVelocity * 5;//configuration.getScaledMaximumFlingVelocity();
	}

	private float getFadingEdgeStrength(final int pos, final int length) {
		if (getChildCount() <= 0) {
			return 0.0f;
		}
		return Math.min((float)pos / (float)length, 1.0f);
	}

	@Override
	protected float getTopFadingEdgeStrength() {
		return getFadingEdgeStrength(getScrollY(), getVerticalFadingEdgeLength());
	}

	@Override
	protected float getBottomFadingEdgeStrength() {
		return getFadingEdgeStrength((getChildCount() <= 0) ? -1 : getChildAt(0).getBottom() - getScrollY() - getHeight() + getPaddingBottom(), getVerticalFadingEdgeLength());
	}

	@Override
	protected float getLeftFadingEdgeStrength() {
		return getFadingEdgeStrength(getScrollX(), getHorizontalFadingEdgeLength());
	}

	@Override
	protected float getRightFadingEdgeStrength() {
		return getFadingEdgeStrength((getChildCount() <= 0) ? -1 : getChildAt(0).getRight() - getScrollX() - getWidth() + getPaddingRight(), getHorizontalFadingEdgeLength());
	}

	private void checkChildCount() {
		if (getChildCount() > 0) {
			throw new IllegalStateException(getClass().getName() + " can host only one direct child");
		}
	}

	@Override
	public void addView(final View child) {
		checkChildCount();
		super.addView(child);
	}

	@Override
	public void addView(final View child, final int index) {
		checkChildCount();
		super.addView(child, index);
	}

	@Override
	public void addView(final View child, final ViewGroup.LayoutParams params) {
		checkChildCount();
		super.addView(child, params);
	}

	@Override
	public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
		checkChildCount();
		super.addView(child, index, params);
	}

	private boolean canScroll() {
		final View child = getChildAt(0);
		if (child != null) {
			final int childHeight = child.getHeight();
			final int childWidth = child.getWidth();
			return (getHeight() < childHeight + getPaddingTop() + getPaddingBottom()) || (getWidth() < childWidth + getPaddingLeft() + getPaddingRight());
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
			return true;
		}
		if (!canScroll()) {
			mIsBeingDragged = false;
			return false;
		}
		final float y = ev.getY();
		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_MOVE: {
				final int yDiff = (int) Math.abs(y - mLastMotionY);
				final int xDiff = (int) Math.abs(x - mLastMotionX);
				if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
					mIsBeingDragged = true;
				}
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				mLastMotionY = y;
				mLastMotionX = x;
				mIsBeingDragged = mScroller.isFinished() ? false : true;
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				mIsBeingDragged = false;
				break;
			}
			default: {
				break;
			}
		}
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			return false;
		}
		if (!canScroll()) {
			return false;
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
		final int action = ev.getAction();
		final float y = ev.getY();
		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mLastMotionY = y;
				mLastMotionX = x;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				int deltaX = (int)(mLastMotionX - x);
				int deltaY = (int)(mLastMotionY - y);
				mLastMotionX = x;
				mLastMotionY = y;
				if (deltaX < 0) {
					if (getScrollX() < 0) {
						deltaX = 0;
					}
				} else if (deltaX > 0) {
					final int rightEdge = getWidth() - getPaddingRight();
					final int availableToScroll = getChildAt(0).getRight() - getScrollX() - rightEdge;
					if (availableToScroll > 0) {
						deltaX = Math.min(availableToScroll, deltaX);
					} else {
						deltaX = 0;
					}
				}
				if (deltaY < 0) {
					if (getScrollY() < 0) {
						deltaY = 0;
					}
				} else if (deltaY > 0) {
					final int bottomEdge = getHeight() - getPaddingBottom();
					final int availableToScroll = getChildAt(0).getBottom() - getScrollY() - bottomEdge;
					if (availableToScroll > 0) {
						deltaY = Math.min(availableToScroll, deltaY);
					} else {
						deltaY = 0;
					}
				}
				if (deltaY != 0 || deltaX != 0) {
					scrollBy(deltaX, deltaY);
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				//velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialXVelocity = (int) velocityTracker.getXVelocity();
				int initialYVelocity = (int) velocityTracker.getYVelocity();
				if ((Math.abs(initialXVelocity) + Math.abs(initialYVelocity) > mMinimumVelocity) && getChildCount() > 0) {
					fling(-initialXVelocity, -initialYVelocity);
				}
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				break;
			}
			default: {
				break;
			}
		}
		return true;
	}

	public boolean fullScroll(final int direction_vert, final int direction_horz) {
		int scrollAmountY = 0;
		int scrollAmountX = 0;
		switch (direction_vert) {
			case View.FOCUS_UP: {
				scrollAmountY = -getScrollY();
				break;
			}
			case View.FOCUS_DOWN: {
				int count = getChildCount();
				if (count > 0) {
					scrollAmountY = (getChildAt(count - 1).getBottom() - getHeight()) - getScrollY();
				}
				break;
			}
			default: {
				break;
			}
		}
		switch (direction_horz) {
			case View.FOCUS_LEFT: {
				scrollAmountX = -getScrollX();
				break;
			}
			case View.FOCUS_RIGHT: {
				int count = getChildCount();
				if (count > 0) {
					scrollAmountX = (getChildAt(count - 1).getRight() - getWidth()) - getScrollX();
				}
				break;
			}
		}
		if (scrollAmountX != 0 || scrollAmountY != 0) {
			doScroll(scrollAmountX, scrollAmountY);
			return true;
		}
		return false;
	}

	private void doScroll(final int deltaX, final int deltaY) {
		if (deltaX != 0 || deltaY != 0) {
			smoothScrollBy(deltaX, deltaY);
		}
	}

	public final void smoothScrollBy(final int dx, final int dy) {
		final long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
		if (duration > ANIMATED_SCROLL_GAP) {
			mScroller.startScroll(getScrollX(), getScrollY(), dx, dy);
			if (!awakenScrolls(mScroller.getDuration())) {
				invalidate();
			}
		} else {
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			scrollBy(dx, dy);
		}
		mLastScroll = AnimationUtils.currentAnimationTimeMillis();
	}

	public final void smoothScrollTo(final int x, final int y) {
		smoothScrollBy(x - getScrollX(), y - getScrollY());
	}

	@Override
	protected int computeVerticalScrollRange() {
		if (getChildCount() <= 0) {
			return getHeight();
		}
		return getChildAt(0).getBottom();
	}

	@Override
	protected int computeHorizontalScrollRange() {
		if (getChildCount() <= 0) {
			return getWidth();
		}
		return getChildAt(0).getRight();
	}

	@Override
	protected void measureChild(final View child, final int parentWidthMeasureSpec, final int parentHeightMeasureSpec) {
		final ViewGroup.LayoutParams lp = child.getLayoutParams();
		child.measure(
			getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
		);
	}

	@Override
	protected void measureChildWithMargins(final View child, final int parentWidthMeasureSpec, final int widthUsed, final int parentHeightMeasureSpec, final int heightUsed) {
		final MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
		child.measure(
			MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED)
		);
	}

	@Override
	public void computeScroll() {
		if (!mScroller.computeScrollOffset()) {
			return;
		}
		int oldX = getScrollX();
		int oldY = getScrollY();
		int x = mScroller.getCurrX();
		int y = mScroller.getCurrY();
		if (getChildCount() > 0) {
			final View child = getChildAt(0);
			scrollTo(
				clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth()),
				clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight())
			);
		} else {
			scrollTo(x, y);
		}
		if (oldX != getScrollX() || oldY != getScrollY()) {
			onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
		}
		postInvalidate();
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		super.onLayout(changed, l, t, r, b);
		scrollTo(getScrollX(), getScrollY());
	}

	public void fling(final int velocityX, final int velocityY) {
		if (getChildCount() <= 0) {
			return;
		}
		final int height = getHeight() - getPaddingBottom() - getPaddingTop();
		final int bottom = getChildAt(0).getHeight();
		final int width = getWidth() - getPaddingRight() - getPaddingLeft();
		final int right = getChildAt(0).getWidth();
		mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, right - width, 0, bottom - height);
		if (!awakenScrolls(mScroller.getDuration())) {
			invalidate();
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		if (getChildCount() <= 0) {
			return;
		}
		final View child = getChildAt(0);
		x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
		y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());
		if (x != getScrollX() || y != getScrollY()) {
			super.scrollTo(x, y);
		}
	}

	private static int clamp(final int n, final int my, final int child) {
		if (my >= child || n < 0) {
			return 0;
		}
		if (my + n > child) {
			return child - my;
		}
		return n;
	}

	private boolean awakenScrolls(final int duration) {
		if (_awakenScrollBars == null) {
			return false;
		}
		try {
			return (Boolean)_awakenScrollBars.invoke(this, new Object[] {
				duration,
			});
		}
		catch (final IllegalAccessException ignore) { }
		catch (final IllegalArgumentException ignore) { }
		catch (final InvocationTargetException ignore) { }
		catch (final NullPointerException ignore) { }
		return false;
	}
}
