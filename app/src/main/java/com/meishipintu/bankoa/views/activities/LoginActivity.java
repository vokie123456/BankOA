package com.meishipintu.bankoa.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.meishipintu.bankoa.Constans;
import com.meishipintu.bankoa.OaApplication;
import com.meishipintu.bankoa.R;
import com.meishipintu.bankoa.components.DaggerLoginComponent;
import com.meishipintu.bankoa.contracts.LoginContract;
import com.meishipintu.bankoa.models.PreferenceHelper;
import com.meishipintu.bankoa.modules.LoginModule;
import com.meishipintu.bankoa.presenters.LoginPresenterImp;
import com.meishipintu.library.util.Encoder;
import com.meishipintu.library.util.StringUtils;
import com.meishipintu.library.util.ToastUtils;
import com.meishipintu.library.view.CircleImageView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by Administrator on 2017/3/1.
 * <p>
 * 登录页面
 */

public class LoginActivity extends BasicActivity implements LoginContract.IView {

    @BindView(R.id.civ_head)
    CircleImageView civHead;
    @BindView(R.id.et_tel)
    EditText etTel;
    @BindView(R.id.et_psw)
    EditText etPsw;
    @BindView(R.id.tv_save_psw)
    RadioButton tvSavePsw;

    @Inject
    LoginPresenterImp mPresenter;

    private boolean savePsw;
    private long exitTime = 0;              //记录退出时间


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        savePsw = true;
        //通过Dagger注入
        DaggerLoginComponent.builder().loginModule(new LoginModule(this))
                .build().inject(this);

        if (PreferenceHelper.getMobile() != null) {
            etTel.setText(PreferenceHelper.getMobile());
        }
    }


    @OnClick({R.id.tv_register, R.id.tv_forget_psw, R.id.tv_save_psw, R.id.bt_login, R.id.tv_branch_login})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.tv_register:
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("type", Constans.REGISTER_TYPE_NORMAL);
                startActivityForResult(intent, Constans.START_REGISTER);
                break;
            case R.id.tv_forget_psw:
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("type", Constans.REGISTER_TYPE_FORGET_PSW);
                startActivityForResult(intent, Constans.START_FORGET_PSW);
                break;
            case R.id.tv_save_psw:
                tvSavePsw.setChecked(!savePsw);
                savePsw = tvSavePsw.isChecked();
                break;
            case R.id.tv_branch_login:
                startActivity(new Intent(LoginActivity.this, BranchSupervisorActivity.class));
                break;
            case R.id.bt_login:
                String tel = etTel.getText().toString();
                String psw = etPsw.getText().toString().trim();
                if (StringUtils.isNullOrEmpty(tel) || StringUtils.isNullOrEmpty(psw)) {
                    ToastUtils.show(this, R.string.err_empty_input, true);
                } else {
                    mPresenter.login(tel, psw, savePsw);
                }
                break;
        }
        if (intent != null) {
        }
    }

    //from LoginContract.IView
    @Override
    public void startMain() {
        //Jpush 注册alias
        JPushInterface.setAlias(this, OaApplication.getUser().getUid(), null);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    //from BasicView.IView
    @Override
    public void showError(String errMsg) {
        ToastUtils.show(this, errMsg, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constans.START_REGISTER && requestCode == RESULT_OK) {
            startMain();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        long clickTime = System.currentTimeMillis();
        if ((clickTime - exitTime) > 1000) {
            //两次点击超过0.5秒则不视为退出
            ToastUtils.show(this, R.string.exit, true);
            exitTime = clickTime;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.unSubscrib();
        super.onDestroy();
    }
}
