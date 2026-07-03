package com.getcapacitor.community.admob.banner;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.common.util.BiConsumer;

public class BannerExecutor extends Executor {

    private final JSObject emptyObject = new JSObject();
    private AdView mAdView;
    private ViewGroup mViewGroup;

    public BannerExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "BannerExecutor");
    }

    public void initialize() {
        ViewGroup content = (ViewGroup) activitySupplier.get().findViewById(android.R.id.content);
        android.view.View firstChild = content.getChildCount() > 0 ? content.getChildAt(0) : null;
        if (firstChild instanceof CoordinatorLayout) {
            mViewGroup = (CoordinatorLayout) firstChild;
            Log.d(logTag, "INIT: parent=CoordinatorLayout");
        } else {
            mViewGroup = content;
            Log.d(logTag, "INIT: parent=FrameLayout firstChild=" + (firstChild == null ? "null" : firstChild.getClass().getSimpleName()));
        }
    }

    public void showBanner(final PluginCall call) {
        final AdOptions adOptions = AdOptions.getFactory().createBannerOptions(call);
        float widthPixels = (int) contextSupplier.get().getResources().getDisplayMetrics().widthPixels;
        float density = contextSupplier.get().getResources().getDisplayMetrics().density;

        if (mAdView != null) {
            updateExistingAdView(adOptions);
            return;
        }

        try {
            mAdView = new AdView(contextSupplier.get());

            if (!adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                mAdView.setAdSize(adOptions.adSize.getSize());
            } else {
                mAdView.setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(contextSupplier.get(), (int) (widthPixels / density))
                );
            }

            int densityMargin = (int) (adOptions.margin * density);

            // 부모 타입에 맞는 LayoutParams 직접 설정
            ViewGroup.LayoutParams layoutParams = buildLayoutParams(adOptions.position, densityMargin);
            mAdView.setLayoutParams(layoutParams);

            createNewAdView(adOptions);

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    private ViewGroup.LayoutParams buildLayoutParams(String position, int margin) {
        if (mViewGroup instanceof CoordinatorLayout) {
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );
            switch (position) {
                case "TOP_CENTER":
                    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    params.setMargins(0, margin, 0, 0);
                    break;
                case "CENTER":
                    params.gravity = Gravity.CENTER;
                    break;
                default:
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    params.setMargins(0, 0, 0, margin);
                    break;
            }
            Log.d(logTag, "LayoutParams: CoordinatorLayout gravity=" + params.gravity);
            return params;
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            switch (position) {
                case "TOP_CENTER":
                    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    params.setMargins(0, margin, 0, 0);
                    break;
                case "CENTER":
                    params.gravity = Gravity.CENTER;
                    break;
                default:
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    params.setMargins(0, 0, 0, margin);
                    break;
            }
            Log.d(logTag, "LayoutParams: FrameLayout gravity=" + params.gravity);
            return params;
        }
    }

    public void hideBanner(final PluginCall call) {
        if (mAdView == null) {
            call.reject("You tried to hide a banner that was never shown");
            return;
        }

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        if (mAdView != null) {
                            mAdView.setVisibility(View.GONE);
                            mAdView.pause();

                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                            call.resolve();
                        }
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void resumeBanner(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        if (mAdView != null) {
                            mAdView.setVisibility(View.VISIBLE);
                            mAdView.resume();

                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(mAdView);
                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                            Log.d(logTag, "Banner AD Resumed");
                        }
                    }
                );

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void removeBanner(final PluginCall call) {
        try {
            if (mAdView != null) {
                activitySupplier
                    .get()
                    .runOnUiThread(
                        () -> {
                            if (mAdView != null) {
                                mViewGroup.removeView(mAdView);
                                mAdView.destroy();
                                mAdView = null;
                                Log.d(logTag, "Banner AD Removed");
                                final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                                notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                            }
                        }
                    );
            }

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    private void updateExistingAdView(AdOptions adOptions) {
        activitySupplier
            .get()
            .runOnUiThread(
                () -> {
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                    mAdView.loadAd(adRequest);
                }
            );
    }

    private void createNewAdView(AdOptions adOptions) {
        activitySupplier
            .get()
            .runOnUiThread(
                () -> {
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                    AdViewIdHelper.assignIdToAdView(mAdView, adOptions, adRequest, logTag, contextSupplier.get());
                    mAdView.loadAd(adRequest);
                    mAdView.setAdListener(
                        new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                notifyListeners(BannerAdPluginEvents.Loaded.getWebEventName(), emptyObject);

                                mAdView.post(() -> {
                                    // 광고 새로고침 시에도 정확한 위치 계산을 위해 먼저 리셋
                                    mAdView.setTranslationY(0);

                                    android.graphics.Rect frame = new android.graphics.Rect();
                                    activitySupplier.get().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

                                    int[] loc = new int[2];
                                    mAdView.getLocationInWindow(loc);
                                    int currentTop = loc[1];

                                    // getHeight()는 1600 등 잘못된 값 반환 — adSize 기준으로 계산
                                    float density = contextSupplier.get().getResources().getDisplayMetrics().density;
                                    com.google.android.gms.ads.AdSize sz = mAdView.getAdSize();
                                    int adH = (sz != null && sz.getHeight() > 0) ? (int)(sz.getHeight() * density) : (int)(60 * density);

                                    int targetTop = frame.bottom - adH;
                                    mAdView.setTranslationY(targetTop - currentTop);

                                    // 레이아웃 완료 후 실제 dp 높이로 JS에 전달 → --ad-banner-height CSS 변수 설정
                                    int adHeightDp = (sz != null && sz.getHeight() > 0) ? sz.getHeight() : 60;
                                    final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(adHeightDp, adHeightDp);
                                    notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                                    Log.d(logTag, "visibleBottom=" + frame.bottom + " adH=" + adH + " currentTop=" + currentTop + " ty=" + (targetTop - currentTop) + " adHeightDp=" + adHeightDp);
                                });

                                super.onAdLoaded();
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                if (mAdView != null) {
                                    mViewGroup.removeView(mAdView);
                                    mAdView.destroy();
                                    mAdView = null;
                                }

                                final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                                notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                                final AdMobPluginError adMobPluginError = new AdMobPluginError(adError);
                                notifyListeners(BannerAdPluginEvents.FailedToLoad.getWebEventName(), adMobPluginError);

                                super.onAdFailedToLoad(adError);
                            }

                            @Override
                            public void onAdOpened() {
                                notifyListeners(BannerAdPluginEvents.Opened.getWebEventName(), emptyObject);
                                super.onAdOpened();
                            }

                            @Override
                            public void onAdClosed() {
                                notifyListeners(BannerAdPluginEvents.Closed.getWebEventName(), emptyObject);
                                super.onAdClosed();
                            }

                            @Override
                            public void onAdImpression() {
                                notifyListeners(BannerAdPluginEvents.AdImpression.getWebEventName(), emptyObject);
                                super.onAdImpression();
                            }
                        }
                    );

                    mViewGroup.addView(mAdView);
                }
            );
    }
}
