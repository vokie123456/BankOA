package com.meishipintu.bankoa.presenters;

import android.util.Log;

import com.meishipintu.bankoa.Constans;
import com.meishipintu.bankoa.OaApplication;
import com.meishipintu.bankoa.contracts.MainContract;
import com.meishipintu.bankoa.models.entity.UserInfo;

import javax.inject.Inject;

/**
 * Created by Administrator on 2017/3/16.
 * <p>
 * 主要功能：
 */

public class MainPresenterImp implements MainContract.IPresenter {

    private MainContract.IView iView;

    @Inject
    public MainPresenterImp(MainContract.IView view) {
        this.iView = view;
    }

    @Override
    public void getUserInfo() {
        UserInfo userInfo = OaApplication.getUser();
        if (userInfo != null && userInfo.getId() != null) {
            iView.refreshUI(userInfo);
        }else {
            iView.showError("获取信息失败，请重新登录");
        }
    }

    //from BasicPresenter.IPresenter
    @Override
    public void unSubscrib() {
    }
}
