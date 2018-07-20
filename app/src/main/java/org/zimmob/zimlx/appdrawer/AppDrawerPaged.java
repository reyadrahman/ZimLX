package org.zimmob.zimlx.appdrawer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.pageindicator.PageIndicator;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.SmoothPagerAdapter;
import org.zimmob.zimlx.widget.AppItemView;
import org.zimmob.zimlx.widget.CellContainer;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.SmoothViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppDrawerPaged extends SmoothViewPager {
    private static List<App> apps;
    public List<ViewGroup> _pages = new ArrayList<>();
    private HomeActivity _home;
    private int _rowCellCount, _columnCellCount;
    private PageIndicator _appDrawerIndicator;
    private int _pageCount = 0;
    public static Companion Companion = null;
    public static Adapter gridAdapter;

    public AppDrawerPaged(Context c, AttributeSet attr) {
        super(c, attr);
        init(c);
    }

    public AppDrawerPaged(Context c) {
        super(c);
        init(c);
    }

    private void init(Context c) {
        if (isInEditMode()) return;
        setOverScrollMode(OVER_SCROLL_NEVER);
        boolean mPortrait = c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        loadApps();
        Companion = new Companion();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (apps == null) {
            super.onConfigurationChanged(newConfig);
            return;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeValue();
            calculatePage();
            setAdapter(new Adapter());

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitValue();
            calculatePage();
            setAdapter(new Adapter());
        }
        super.onConfigurationChanged(newConfig);
    }

    private void setPortraitValue() {
        _columnCellCount = Setup.appSettings().getDrawerColumnCount();
        _rowCellCount = Setup.appSettings().getDrawerRowCount();
    }

    private void setLandscapeValue() {
        _columnCellCount = Setup.appSettings().getDrawerRowCount();
        _rowCellCount = Setup.appSettings().getDrawerColumnCount();
    }

    private void calculatePage() {
        _pageCount = 0;
        int appsSize = apps.size();
        while ((appsSize = appsSize - (_rowCellCount * _columnCellCount)) >= (_rowCellCount * _columnCellCount) || (appsSize > -(_rowCellCount * _columnCellCount))) {
            _pageCount++;
        }
    }

    public void loadApps() {
        List<App> allApps = Setup.appLoader().getAllApps(getContext(), false);
        if (allApps.size() != 0) {
            AppDrawerPaged.this.apps = allApps;
            calculatePage();
            setAdapter(new Adapter());
            if (_appDrawerIndicator != null)
                _appDrawerIndicator.setViewPager(AppDrawerPaged.this);
        }
        Setup.appLoader().addUpdateListener(apps -> {
            AppDrawerPaged.this.apps = apps;
            calculatePage();
            setAdapter(new Adapter());
            if (_appDrawerIndicator != null)
                _appDrawerIndicator.setViewPager(AppDrawerPaged.this);
            return false;
        });

    }

    public void loadAppsFiltered(List<App> fApps) {
        List<App> allApps = fApps;
        if (allApps.size() != 0) {
            AppDrawerPaged.this.apps = allApps;
            calculatePage();
            setAdapter(new Adapter());
            if (_appDrawerIndicator != null)
                _appDrawerIndicator.setViewPager(AppDrawerPaged.this);
        }
    }

    public void Filter(CharSequence s) {
        List<App> tmpApps = AppDrawerPaged.apps;
        List<App> filteredApps = new ArrayList<>();
        String appName = s.toString().toLowerCase();
        if (s.length() > 0) {
            for (int i = 0; i < apps.size(); i++) {
                App filteredApp = apps.get(i);
                if (filteredApp.getLabel().toLowerCase().contains(appName)) {
                    filteredApps.add(filteredApp);
                }
            }
            loadAppsFiltered(filteredApps);

        } else {
            loadApps();
        }
    }

    public void sortApps() {
        Collections.sort(apps, new SortMostUsed());
        resetAdapter();
    }

    public void withHome(HomeActivity home, PageIndicator appDrawerIndicator) {
        _home = home;
        _appDrawerIndicator = appDrawerIndicator;
        appDrawerIndicator.setMode(Config.INDICATOR_DOTS);
        if (getAdapter() != null)
            appDrawerIndicator.setViewPager(AppDrawerPaged.this);
    }

    public void resetAdapter() {
        setAdapter(null);
        setAdapter(new Adapter());
    }

    public static class SortMostUsed implements Comparator<App> {
        @Override
        public int compare(App lhs, App rhs) {
            int item1 = HomeActivity.Companion.getDb().getAppCount(lhs.getPackageName());
            int item2 = HomeActivity.Companion.getDb().getAppCount(rhs.getPackageName());
            if (item1 < item2) {
                return 1;
            } else if (item2 < item1) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public class Adapter extends SmoothPagerAdapter {
        int iconSize = Setup.appSettings().getDrawerIconSize();

        protected Adapter() {
            _pages.clear();
            for (int i = 0; i < getCount(); i++) {
                ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_app_drawer_paged_inner, null);
                if (!Setup.appSettings().isDrawerShowCardView()) {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(Color.TRANSPARENT);
                    ((CardView) layout.getChildAt(0)).setCardElevation(0);
                } else {
                    ((CardView) layout.getChildAt(0)).setCardBackgroundColor(Setup.appSettings().getDrawerCardColor());
                    ((CardView) layout.getChildAt(0)).setCardElevation(Tool.dp2px(4, getContext()));
                }
                CellContainer cc = layout.findViewById(R.id.group);
                cc.setGridSize(_columnCellCount, _rowCellCount);
                for (int x = 0; x < _columnCellCount; x++) {
                    for (int y = 0; y < _rowCellCount; y++) {
                        View view = getItemView(i, x, y);
                        if (view != null) {
                            CellContainer.LayoutParams lp = new CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1);
                            view.setLayoutParams(lp);
                            cc.addViewToGrid(view);
                        }
                    }
                }
                _pages.add(layout);
            }
        }

        private View getItemView(int page, int x, int y) {
            int pagePos = y * _columnCellCount + x;
            final int pos = _rowCellCount * _columnCellCount * page + pagePos;
            if (pos >= apps.size())
                return null;
            final App app = apps.get(pos);
            return AppItemView.createDrawerAppItemView(getContext(), app, iconSize, new AppItemView.LongPressCallBack() {
                @Override
                public boolean readyForDrag(View view) {
                    return Setup.appSettings().getDesktopStyle() == Desktop.DesktopMode.INSTANCE.getSHOW_ALL_APPS();
                }

                @Override
                public void afterDrag(View view) {
                }
            });
        }

        @Override
        public int getCount() {
            return _pageCount;
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2) {
            return p1 == p2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            int index = _pages.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int pos) {
            ViewGroup layout = _pages.get(pos);
            container.addView(layout);
            return layout;
        }
    }

    public class Companion {
        public void FilterApps(CharSequence s) {
            Filter(s);
        }
    }
}

