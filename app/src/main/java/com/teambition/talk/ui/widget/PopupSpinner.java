package com.teambition.talk.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.teambition.talk.R;
import com.teambition.talk.util.DensityUtil;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 14-10-17.
 */
public class PopupSpinner {

    public interface OnItemClickListener {
        void onClick(int position, String value);
    }

    public static void showPopupSpinner(Context context, View anchor, final List<String> data,
                                        final OnItemClickListener listener, int selection) {
        final int statusActionBarHeight = DensityUtil.dip2px(context, 56 + 24);
        final int maxHeight = DensityUtil.dip2px(context, 256); //(48 * 5 + 8*2); itemHeight(48) * 4 + padding(8) *2
        final int itemHeight = DensityUtil.dip2px(context, 48);
        final PopupWindow popupWindow = new PopupWindow(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_spinner,
                null);
        ListView listView = (ListView) contentView.findViewById(R.id.listView);
        listView.setAdapter(new PopupSpinnerAdapter(context, data, selection));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(position, data.get(position));
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new PaintDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(contentView);

        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // calculate position.
        int[] location = new int[2];
        int xPos, yPos;
        int length = data.size();
        anchor.getLocationOnScreen(location);
        if (length > 5) {
            popupWindow.setHeight(maxHeight);
        } else {
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        }
        xPos = location[0] - DensityUtil.dip2px(context, 18);
        if (length > 5) {
            if (selection > 5) {
                yPos = location[1] - DensityUtil.dip2px(context, 8) - itemHeight * (selection - 5);
            } else {
                yPos = location[1] - DensityUtil.dip2px(context, 8) - itemHeight * 0;
            }

            listView.setSelection(selection);
        } else {
            yPos = location[1] - DensityUtil.dip2px(context, 14) - itemHeight * selection;
        }
        if (yPos < statusActionBarHeight) {
            yPos = statusActionBarHeight;
        }

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

    }

    public static void showPopupSpinner(Context context, View v, final String[] data,
                                        final OnItemClickListener listener, int xOffsetDp,
                                        int yOffsetDp) {
        showPopupSpinner(context, v, Arrays.asList(data), listener, xOffsetDp, yOffsetDp);

    }

    public static void showPopupSpinner(Context context, View v, final List<String> data,
                                        final OnItemClickListener listener, int xOffsetDp,
                                        int yOffsetDp) {
        final PopupWindow popupWindow = new PopupWindow(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_spinner,
                null);
        ListView listView = (ListView) contentView.findViewById(R.id.listView);
        listView.setAdapter(new PopupSpinnerAdapter(context, data));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(position, data.get(position));
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new PaintDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(contentView);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAsDropDown(v, DensityUtil.dip2px(context, xOffsetDp),
                DensityUtil.dip2px(context, yOffsetDp));
    }

    public static class PopupSpinnerAdapter extends BaseAdapter {

        private Context context;
        private List<String> items;
        private int selection = -1;

        private PopupSpinnerAdapter(Context context, List<String> items) {
            this(context, items, -1);
        }

        private PopupSpinnerAdapter(Context context, List<String> items, int selection) {
            this.context = context;
            this.items = items;
            this.selection = selection;
        }

        public void setSelection(int selection) {
            this.selection = selection;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_text_base, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(items.get(position));
            if (selection == position) {
                holder.textView.setChecked(true);
            } else {
                holder.textView.setChecked(false);
            }
            return convertView;
        }

        static class ViewHolder {

            @InjectView(R.id.text)
            CheckedTextView textView;

            ViewHolder(View v) {
                ButterKnife.inject(this, v);
            }
        }
    }
}
