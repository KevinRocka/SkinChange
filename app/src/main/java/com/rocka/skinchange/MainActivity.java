package com.rocka.skinchange;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rocka.skinchange.fragment.FragmentHome;
import com.rocka.skinchange.fragment.FragmentMine;
import com.rocka.skinchange.fragment.FragmentResources;
import com.rocka.skinchange.utils.ToastUtil;
import com.rocka.skinlibrary.base.SkinBaseActivity;
import com.rocka.skinlibrary.core.SkinManager;
import com.rocka.skinlibrary.listener.SkinLoadListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: 换肤功能
 * @time:2018/5/9
 */
public class MainActivity extends SkinBaseActivity {

    public static final int POSITION_HOME = 0;
    public static final int POSITION_RESOURCES = 1;
    public static final int POSITION_MYSELF = 2;

    private static SparseIntArray mRadioButtonIdMaps;

    private Context mContext;

    private Fragment mCurrentFragment;

    private Unbinder mUnBinder;

    @BindView(R.id.main_content)
    FrameLayout mainContent;
    @BindView(R.id.main_index_home)
    RadioButton mainIndexHome;
    @BindView(R.id.main_index_resources)
    RadioButton mainIndexResources;
    @BindView(R.id.main_index_myself)
    RadioButton mainIndexMyself;
    @BindView(R.id.tab_menu)
    RadioGroup mRadioGroup;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FragmentHome mFragmentHome;

    private FragmentMine mFragmentMine;

    private FragmentResources mFragmentResources;

    static {
        mRadioButtonIdMaps = new SparseIntArray();
        mRadioButtonIdMaps.put(POSITION_HOME, R.id.main_index_home);
        mRadioButtonIdMaps.put(POSITION_RESOURCES, R.id.main_index_resources);
        mRadioButtonIdMaps.put(POSITION_MYSELF, R.id.main_index_myself);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnBinder = ButterKnife.bind(this);
        mContext = this;
        bindRadioGroup();
        switchFragment(POSITION_HOME);
        setToolbar();
    }

    private void setToolbar(){
        toolbar.setTitle("Skin换肤");
        setSupportActionBar(toolbar);
        dynamicAddView(toolbar, "background", R.color.colorPrimaryDark);
    }

    private void bindRadioGroup() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                int position;
                switch (checkID) {
                    case R.id.main_index_home:
                        position = POSITION_HOME;
                        break;
                    case R.id.main_index_resources:
                        position = POSITION_RESOURCES;
                        break;
                    case R.id.main_index_myself:
                        position = POSITION_MYSELF;
                        break;
                    default:
                        position = POSITION_HOME;
                        break;
                }
                if (position != -1) {
                    switchFragment(position);
                }
            }
        });
    }

    private void switchFragment(int position) {
        Bundle args = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        switch (position) {
            case POSITION_HOME:
                if (mFragmentHome == null) {
                    mFragmentHome = new FragmentHome();
                    mFragmentHome.setArguments(args);
                }
                if (mFragmentHome.isAdded()) {
                    transaction.show(mFragmentHome);
                } else {
                    String tag = FragmentHome.class.getSimpleName();
                    Fragment fragment = fragmentManager.findFragmentByTag(tag);
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                    transaction.add(R.id.main_content, mFragmentHome, tag);
                }
                mCurrentFragment = mFragmentHome;
                break;
            case POSITION_MYSELF:
                if (mFragmentMine == null) {
                    mFragmentMine = new FragmentMine();
                }
                if (mFragmentMine.isAdded()) {
                    transaction.show(mFragmentMine);
                } else {
                    String tag = FragmentMine.class.getSimpleName();
                    Fragment fragment = fragmentManager.findFragmentByTag(tag);
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                    transaction.add(R.id.main_content, mFragmentMine, tag);
                }
                mCurrentFragment = mFragmentMine;
                break;
            case POSITION_RESOURCES:
                if (mFragmentResources == null) {
                    mFragmentResources = new FragmentResources();
                }
                if (mFragmentResources.isAdded()) {
                    transaction.show(mFragmentResources);
                } else {
                    String tag = FragmentResources.class.getSimpleName();
                    Fragment fragment = fragmentManager.findFragmentByTag(tag);
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                    transaction.add(R.id.main_content, mFragmentResources, tag);
                }
                mCurrentFragment = mFragmentResources;
                break;
            default:
                if (mFragmentHome == null) {
                    mFragmentHome = new FragmentHome();
                    mFragmentHome.setArguments(args);
                }
                if (mFragmentHome.isAdded()) {
                    transaction.show(mFragmentHome);
                } else {
                    String tag = FragmentHome.class.getSimpleName();
                    Fragment fragment = fragmentManager.findFragmentByTag(tag);
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                    transaction.add(R.id.main_content, mFragmentHome, tag);
                }
                mCurrentFragment = mFragmentHome;
                break;
        }
        transaction.commit();
        fragmentManager.executePendingTransactions();
        updateRadioButtonStatus(position);
    }

    public boolean updateRadioButtonStatus(int selectPosition) {
        RadioButton radioButton = getChildRButton(selectPosition);
        if (!radioButton.isChecked()) {
            radioButton.setChecked(true);
            return true;
        }
        return false;
    }

    private RadioButton getChildRButton(int selectPosition) {
        int viewId = mRadioButtonIdMaps.get(selectPosition);
        final int count = mRadioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mRadioGroup.getChildAt(i);
            if (child.getId() == viewId) {
                return (RadioButton) child;
            }
        }
        return (RadioButton) mRadioGroup.getChildAt(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_local1:
                loadSkinRed();
                break;
            case R.id.action_load_local2:
                loadSkinBlue();
                break;
            case R.id.action_load_default:
                SkinManager.getInstance().loadFont(null);
                SkinManager.getInstance().restoreDefaultTheme();
                break;
            case R.id.action_night_mode:
                SkinManager.getInstance().nightMode();
                break;
            case R.id.action_font_change:
                SkinManager.getInstance().loadFont("DLTZT.ttf");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadSkinRed(){
        SkinManager.getInstance().loadSkin("theme-red-20180514.skin",
                new SkinLoadListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess() {
                        ToastUtil.getInstance(mContext).makeText("切换成功");
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        ToastUtil.getInstance(mContext).makeText("切换失败:" + errMsg);
                    }
                }

        );
    }


    private void loadSkinBlue(){
        SkinManager.getInstance().loadSkin("theme-blue-20180514.skin",
                new SkinLoadListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess() {
                        ToastUtil.getInstance(mContext).makeText("切换成功");
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        ToastUtil.getInstance(mContext).makeText("切换失败:" + errMsg);
                    }
                }

        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }
}
