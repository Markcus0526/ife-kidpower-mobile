package org.caloriecloud.android.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.caloriecloud.android.R;
import org.caloriecloud.android.model.Team;

import java.util.ArrayList;

public class CCSpinnerTeamAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Context activity;
    private ArrayList<Team> asr;

    public CCSpinnerTeamAdapter(Context context, ArrayList<Team> asr) {
        this.asr=asr;
        activity = context;
    }

    public int getCount()
    {
        return asr.size();
    }

    public Team getItem(int i)
    {
        return asr.get(i);
    }

    public long getItemId(int i)
    {
        return (long)i;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView txt = new TextView(activity);
        Typeface font = Typeface.createFromAsset(activity.getAssets(), "fonts/SourceSansPro_Regular.otf");
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(18);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(asr.get(position).getName());
        txt.setTypeface(font);
        txt.setTextColor(activity.getResources().getColor(R.color.colorTextBlack));
        return txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(activity);
        Typeface font = Typeface.createFromAsset(activity.getAssets(), "fonts/SourceSansPro_It.otf");
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(18);
        txt.setTypeface(font);
        txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner_icon, 0);
        txt.setText(asr.get(i).getName());
        txt.setTextColor(activity.getResources().getColor(R.color.colorTextGrey));
        return txt;
    }
}
