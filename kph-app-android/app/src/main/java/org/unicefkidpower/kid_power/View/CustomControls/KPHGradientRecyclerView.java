package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import org.unicefkidpower.kid_power.View.Adapters.MissionLogAdapter;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;

/**
 * Created by Ruifeng Shi on 7/29/2016.
 */
public class KPHGradientRecyclerView extends RecyclerView {
	private final int			WIDTH = 480;
	private final int			HEIGHT = 4500;
	private int					itemHeight = 0;
	private int					width = 0, height = 0, drawableHeight = 0;

	private boolean				isDrawingGradient = true;
	private final int[]			colors = new int[]{0xFF04AEEB, 0xFFB1CF51, 0xFFE87722, 0xFF04AEEB, 0xFFB1CF51, 0xFFE87722, 0xFF04AEEB};
	private final float[]		positions = new float[]{0, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f, 1};

	private View				headerView = null;


	public KPHGradientRecyclerView(Context context) {
		super(context);
		initScrollChangeListener();
	}

	public KPHGradientRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initScrollChangeListener();
	}

	public KPHGradientRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initScrollChangeListener();
	}


	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!isDrawingGradient)
			return;

		// Calculate positions
		if (width == 0) {
			width = getWidth();
			height = getHeight();
			drawableHeight = HEIGHT * width / WIDTH;
		}

		int totalHeight = getItemsHeight();
		if (headerView != null) {
			totalHeight += headerView.getHeight();
		}

		if (totalHeight < height)
			totalHeight = height;

		int scrollY = -headerView.getTop();

		int startPosY = totalHeight - drawableHeight - scrollY;
		/////////////////////////////////////////////////////////////////////

		LinearGradient backGradient = new LinearGradient(0, startPosY, 0, startPosY + drawableHeight, colors, positions, Shader.TileMode.REPEAT);
		Paint backPaint = new Paint();
		backPaint.setDither(true);
		backPaint.setShader(backGradient);
		backPaint.setStyle(Paint.Style.FILL);

		canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backPaint);
	}


	public void calcFirstItemHeight() {
		RecyclerView.Adapter adapter = getAdapter();

		if (adapter == null || adapter.getItemCount() == 0) {
			itemHeight = 0;
			return;
		}

		View itemView = adapter.createViewHolder(null, MissionLogAdapter.VIEW_TYPE_ITEM).itemView;
		itemView.measure(MeasureSpec.makeMeasureSpec(ResolutionSet.getScreenSize(getContext(), false).x, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		itemHeight = itemView.getMeasuredHeight();
	}


	private int getItemsHeight() {
		int result = 0;

		if (itemHeight == 0)
			calcFirstItemHeight();

		RecyclerView.Adapter adapter = getAdapter();
		if (adapter == null || adapter.getItemCount() == 0) {
			result = 0;
		} else {
			result = itemHeight * adapter.getItemCount();
		}

		return result;
	}


	public void setHeaderView(View v) {
		headerView = v;
	}

	public void setDrawingGradient(boolean isDrawingGradient) {
		this.isDrawingGradient = isDrawingGradient;
		invalidate();
	}

	private void initScrollChangeListener() {
		if (Build.VERSION.SDK_INT >= 23) {
			setOnScrollChangeListener(new OnScrollChangeListener() {
				@Override
				public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
					if (isDrawingGradient)
						invalidate();
				}
			});
		} else {
			setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					if (isDrawingGradient)
						invalidate();
				}
			});
		}
	}


	public void setAdapter(RecyclerView.Adapter adapter) {
		int scrollY = computeVerticalScrollOffset();
		if (scrollY > 0) {
			int topPadding = getPaddingTop();
			setPadding(getPaddingLeft(), topPadding - scrollY, getPaddingRight(), getPaddingBottom());
			setTag(topPadding);

			addOnLayoutChangeListener(new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					KPHGradientRecyclerView.this.removeOnLayoutChangeListener(this);

					int topPadding = (Integer) KPHGradientRecyclerView.this.getTag();
					KPHGradientRecyclerView.this.setPadding(
							KPHGradientRecyclerView.this.getPaddingLeft(),
							topPadding,
							KPHGradientRecyclerView.this.getPaddingRight(),
							KPHGradientRecyclerView.this.getPaddingBottom()
					);
				}
			});
		}

		super.setAdapter(adapter);
	}

}
