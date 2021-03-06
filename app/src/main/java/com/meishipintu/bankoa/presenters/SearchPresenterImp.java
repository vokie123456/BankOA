package com.meishipintu.bankoa.presenters;

import android.media.DrmInitData;
import android.util.Log;

import com.meishipintu.bankoa.OaApplication;
import com.meishipintu.bankoa.contracts.SearchContract;
import com.meishipintu.bankoa.models.PreferenceHelper;
import com.meishipintu.bankoa.models.entity.CenterBranch;
import com.meishipintu.bankoa.models.entity.Task;
import com.meishipintu.bankoa.models.http.HttpApi;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Administrator on 2017/3/15.
 * <p>
 * 主要功能：
 */

public class SearchPresenterImp implements SearchContract.IPresenter {

    private static final String TAG = "BankOA-searchPresenter";
    private SearchContract.IView iView;
    private HttpApi httpApi;
    private CompositeSubscription subscriptions;

    @Inject
    SearchPresenterImp(SearchContract.IView view) {
        this.iView = view;
        httpApi = HttpApi.getInstance();
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void search(String level, String department_id, String uid, String searchContent) {
        //调用search接口
        subscriptions.add(httpApi.getSearchInfo(level, department_id, uid, searchContent).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Task>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        iView.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(List<Task> taskList) {
                        iView.showResult(taskList);
                    }
                })
        );
    }

    @Override
    public void getCenterBranch() {
        List<CenterBranch> centerBranchList = OaApplication.centerBranchList;
        if (centerBranchList == null) {
            subscriptions.add(httpApi.getCenterBranchList().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<CenterBranch>>() {
                        @Override
                        public void call(List<CenterBranch> strings) {
                            PreferenceHelper.saveCenterBranch(strings);
                            iView.onCenterBranchGet(strings);
                        }
                    }));
        } else {
            iView.onCenterBranchGet(centerBranchList);
        }
    }

    @Override
    public void getBranchList(final List<CenterBranch> centerBranchList) {
        for(int i=0;i<centerBranchList.size();i++) {
            Map<Integer, String> branchList = OaApplication.branchList.get(centerBranchList.get(i).getId());
            if (branchList == null) {
                final int finalI = i;
                subscriptions.add(httpApi.getBranchList(centerBranchList.get(i).getId()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Map<Integer, String>>() {
                            @Override
                            public void call(Map<Integer, String> strings) {
                                PreferenceHelper.saveBranch(centerBranchList.get(finalI).getId(), strings);
                                OaApplication.branchList.put(centerBranchList.get(finalI).getId(), strings);
                                iView.onBranchListGet(centerBranchList.get(finalI).getId(), strings);
                            }
                        }));
            } else {
                iView.onBranchListGet(centerBranchList.get(i).getId(), branchList);
            }
        }
    }

    //from BasicPresenter
    @Override
    public void unSubscrib() {
        subscriptions.clear();
    }
}
