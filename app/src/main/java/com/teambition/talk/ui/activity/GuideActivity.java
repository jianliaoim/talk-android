package com.teambition.talk.ui.activity;

import android.animation.ArgbEvaluator;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.util.TransactionUtil;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 1/18/16.
 */
public class GuideActivity extends BaseActivity implements ViewPager.PageTransformer {

    @InjectView(R.id.pager)
    ViewPager pager;
    @InjectView(R.id.background)
    View background;
    @InjectView(R.id.text)
    TextView textView;
    @InjectView(R.id.btn_go)
    Button btnGo;
    @InjectView(R.id.indicator1)
    View indicator1;
    @InjectView(R.id.indicator2)
    View indicator2;

    private ArgbEvaluator evaluator = new ArgbEvaluator();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(R.layout.activity_guide);
        ButterKnife.inject(this);
        pager.setAdapter(new PageAdapter());
        pager.setPageTransformer(true, this);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainApp.PREF_UTIL.putBoolean(Constant.FIRST_OPEN_3_0, false);
                if (BizLogic.isLogin()) {
                    if (BizLogic.hasChosenTeam()) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(PushMessageHelper.KEY_MESSAGE, getIntent().getSerializableExtra(PushMessageHelper.KEY_MESSAGE));
                        TransactionUtil.goTo(GuideActivity.this, HomeActivity.class, bundle, true);
                        overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                    } else {
                        TransactionUtil.goTo(GuideActivity.this, ChooseTeamActivity.class, true);
                        overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                    }
                } else {
                    TransactionUtil.goTo(GuideActivity.this, Oauth2Activity.class, true);
                    overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
                }
            }
        });
    }

    @Override
    public void transformPage(View page, float position) {
        int index = (int) page.getTag();
        if (position >= 0 && position <= 1) {
            if (index == 1) {
                int color = (Integer) evaluator.evaluate(position, 0xFFFFE2D8, 0xFFFBDCA6);
                background.setBackgroundColor(color);

                if (position > 0.5) {
                    textView.setText(R.string.guide_text1);
                    btnGo.setVisibility(View.INVISIBLE);
                    indicator1.setVisibility(View.VISIBLE);
                    indicator2.setVisibility(View.GONE);
                } else {
                    textView.setText(R.string.guide_text2);
                    btnGo.setVisibility(View.VISIBLE);
                    indicator1.setVisibility(View.GONE);
                    indicator2.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private class PageAdapter extends PagerAdapter {

        private List<View>viewList = new ArrayList<>();

        public PageAdapter() {
            int[] res = {R.drawable.img_user_guide1, R.drawable.img_user_guide2};
            for (int i = 0; i < 2; i++) {
                View view = getLayoutInflater().inflate(R.layout.pager_guide, null);
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                imageView.setImageResource(res[i]);
                view.setTag(i);
                viewList.add(view);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object != null && object == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

}
