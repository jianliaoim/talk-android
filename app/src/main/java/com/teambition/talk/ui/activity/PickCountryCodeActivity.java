package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.R;
import com.teambition.talk.adapter.PickCountryCodeAdapter;
import com.teambition.talk.entity.CountryModel;

import org.parceler.Parcels;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class PickCountryCodeActivity extends BaseActivity {

    public static final String COUNTRY_CODE_DATA = "country_code_data";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    StickyListHeadersListView countryListView;

    private PickCountryCodeAdapter adapter;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_country_code);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new PickCountryCodeAdapter(this);
        countryListView.setAdapter(adapter);
        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CountryModel model = adapter.getItem(position);
                if (model != null) {
                    Intent intent = new Intent();
                    intent.putExtra(COUNTRY_CODE_DATA, Parcels.wrap(model));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        initCountries();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.anim_fade_transition_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED, new Intent());
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initCountries() {
        Observable.from(PhoneNumberUtil.getInstance().getSupportedRegions())
                .map(new Func1<String, CountryModel>() {
                    @Override
                    public CountryModel call(String string) {
                        CountryModel model = new CountryModel();
                        Locale locale = new Locale("", string);
                        model.countryName = locale.getDisplayName();
                        model.callingCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(string);

                        Locale langLocale = Locale.getDefault();
                        if ("en".equals(langLocale.getLanguage())) {
                            model.pinyin = model.countryName;
                            model.groupName = model.pinyin.substring(0, 1).toUpperCase();
                        } else if ("zh".equals(langLocale.getLanguage())) {
                            model.pinyin = PinyinUtil.converterToSpell(model.countryName);
                            model.groupName = model.pinyin.substring(0, 1).toUpperCase();
                        } else {
                            model.pinyin = model.countryName;
                            model.groupName = model.pinyin.substring(0, 1).toUpperCase();
                        }
                        return model;
                    }
                })
                .toSortedList(new Func2<CountryModel, CountryModel, Integer>() {
                    @Override
                    public Integer call(CountryModel countryModel, CountryModel countryModel2) {
                        return countryModel.pinyin.compareTo(countryModel2.pinyin);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<CountryModel>>() {
                    @Override
                    public void call(List<CountryModel> countryModels) {
                        adapter.setCountries(countryModels);
                    }
                });
    }
}
