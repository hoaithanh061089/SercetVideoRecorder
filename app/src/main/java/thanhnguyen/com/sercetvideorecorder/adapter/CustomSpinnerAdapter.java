package thanhnguyen.com.sercetvideorecorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import thanhnguyen.com.sercetvideorecorder.R;

/**
 * Created by THANHNGUYEN on 12/24/17.
 */

public class CustomSpinnerAdapter extends BaseAdapter {
    Context context;
    int icon[];
    String[] name;
    LayoutInflater inflter;

    public CustomSpinnerAdapter(Context applicationContext, int[] icon, String[] name) {
        this.context = applicationContext;
        this.icon = icon;
        this.name = name;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return name.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner_items, null);
        ImageView iconview = (ImageView) view.findViewById(R.id.imageView);
        TextView names = (TextView) view.findViewById(R.id.textView);
        iconview.setImageResource(icon[i]);
        names.setText(name[i]);
        return view;
    }
}
