package com.teambition.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.teambition.talk.R;
import com.teambition.talk.entity.CountryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class PickCountryCodeAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private List<CountryModel> list;

    public PickCountryCodeAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void setCountries(List<CountryModel> datas) {
        list.clear();
        list.addAll(makeMajorCountries(context));
        list.addAll(datas);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CountryModel getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_country_content, parent, false);
            holder = new ViewHolderItem(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }

        CountryModel model = list.get(position);
        holder.countryTv.setText(model.countryName);

        return convertView;
    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        ViewHolderHeader holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_selected_country_title, viewGroup, false);
            holder = new ViewHolderHeader(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolderHeader) view.getTag();
        }

        CountryModel model = getItem(i);
        holder.groupTv.setText(model.groupName);
        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return getItem(i).groupName.hashCode();
    }

    private List<CountryModel> makeMajorCountries(Context context) {


        final String groupName = context.getString(R.string.major_country);

        List<CountryModel> models = new ArrayList<>();

        CountryModel model;
        Locale locale;

        model = new CountryModel();
        locale = new Locale("", CountryModel.COUNTRY_CODE_CN);
        model.countryName = locale.getDisplayName();
        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(CountryModel.COUNTRY_CODE_CN);
        model.groupName = groupName;
        models.add(model);

        model = new CountryModel();
        locale = new Locale("", CountryModel.COUNTRY_CODE_HK);
        model.countryName = locale.getDisplayName();
        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(CountryModel.COUNTRY_CODE_HK);
        model.groupName = groupName;
        models.add(model);

        model = new CountryModel();
        locale = new Locale("", CountryModel.COUNTRY_CODE_TW);
        model.countryName = locale.getDisplayName();
        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(CountryModel.COUNTRY_CODE_TW);
        model.groupName = groupName;
        models.add(model);

        model = new CountryModel();
        locale = new Locale("", CountryModel.COUNTRY_CODE_US);
        model.countryName = locale.getDisplayName();
        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(CountryModel.COUNTRY_CODE_US);
        model.groupName = groupName;
        models.add(model);

        model = new CountryModel();
        locale = new Locale("", CountryModel.COUNTRY_CODE_JP);
        model.countryName = locale.getDisplayName();
        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(CountryModel.COUNTRY_CODE_JP);
        model.groupName = groupName;
        models.add(model);

        return models;
    }

    static class ViewHolderItem {
        @InjectView(R.id.country_name)
        TextView countryTv;

        public ViewHolderItem(View itemView) {
            ButterKnife.inject(this, itemView);
        }
    }

    static class ViewHolderHeader {
        @InjectView(R.id.country_group)
        TextView groupTv;

        public ViewHolderHeader(View itemView) {
            ButterKnife.inject(this, itemView);
        }
    }
}
