package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Ruifeng Shi on 9/7/2015.
 */
public class KPHTabItem extends LinearLayout {
	private OnTabSelectedListener			onTabSelectedListener = null;
	private View							parentView = null;

	private Context							parentContext = null;
	private ImageView						tabItemImageView = null;
	private TextView						tabItemTextView = null;
	private TextView						badgeTextView = null;
	private int								imageSource = 0;
	private String							labelString = "";
	private View							contentView = null;
	private LinearLayout					contentLayout = null;

	private final long						minimumInterval = 0;
	private static Map<View, Long>			lastClickMap = new WeakHashMap<View, Long>();



	public KPHTabItem(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}


	public KPHTabItem(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHTabItem);
		int imageSrc = typedArray.getResourceId(R.styleable.KPHTabItem_customTabItemImage, R.drawable.kph_drawable_more_icon);
		String label = typedArray.getString(R.styleable.KPHTabItem_customTabItemLabel);

		imageSource = imageSrc;
		if (label != null) {
			labelString = label;
		}

		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}


	public KPHTabItem(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHTabItem);
		int imageSrc = typedArray.getResourceId(R.styleable.KPHTabItem_customTabItemImage, R.drawable.kph_drawable_more_icon);
		String label = typedArray.getString(R.styleable.KPHTabItem_customTabItemLabel);

		imageSource = imageSrc;
		if (label != null) {
			labelString = label;
		}

		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}


	private void layoutViews() {
		contentView = LayoutInflater.from(parentContext).inflate(R.layout.item_tabbar, this);

		contentLayout = (LinearLayout) findViewById(R.id.layout_content);
		tabItemImageView = (ImageView) contentLayout.findViewById(R.id.ivTabItem);
		tabItemImageView.setImageResource(imageSource);

		tabItemTextView = (TextView) contentLayout.findViewById(R.id.txtTabItem);
		tabItemTextView.setText(labelString);

		badgeTextView = (TextView) contentLayout.findViewById(R.id.txt_badge);
		badgeTextView.setVisibility(INVISIBLE);

		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedSelf(v);
			}
		});
	}


	public void setHighlighted(boolean highlighted) {
		if (highlighted) {
			tabItemTextView.setTextColor(Color.WHITE);

			if (imageSource != 0) {
				tabItemImageView.setSelected(true);
			}
		} else {
			tabItemTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_dark_blue_30));

			if (imageSource != 0) {
				tabItemImageView.setSelected(false);
			}
		}
	}

	public void setBadgeValue(int badgeValue) {
		if (badgeValue == 0) {
			badgeTextView.setVisibility(INVISIBLE);
		} else {
			badgeTextView.setVisibility(VISIBLE);
			badgeTextView.setText(String.valueOf(badgeValue));
		}
	}

	public void setParentView(View view) {
		parentView = view;
	}

	public void onClickedSelf(View v) {
		if (parentView != null) {
			Long previousClickTimestamp = lastClickMap.get(parentView);
			long currentTimestamp = SystemClock.uptimeMillis();

			if (previousClickTimestamp == null || (currentTimestamp - previousClickTimestamp.longValue() > minimumInterval)) {
				clickHandler();
				lastClickMap.put(parentView, currentTimestamp);
			}
		} else {
			clickHandler();
		}
	}

	private void clickHandler() {
		if (onTabSelectedListener != null) {
			onTabSelectedListener.onTabSelected(contentView);
		}

		setHighlighted(true);
	}

	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		onTabSelectedListener = listener;
	}

	public interface OnTabSelectedListener {
		/**
		 * Called when a view has been clicked.
		 *
		 * @param v The view that was clicked.
		 */
		void onTabSelected(View v);
	}
}
