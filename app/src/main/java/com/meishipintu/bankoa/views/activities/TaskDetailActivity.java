package com.meishipintu.bankoa.views.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.meishipintu.bankoa.Constans;
import com.meishipintu.bankoa.OaApplication;
import com.meishipintu.bankoa.R;
import com.meishipintu.bankoa.components.DaggerTaskDetailComponent;
import com.meishipintu.bankoa.contracts.TaskDetailContract;
import com.meishipintu.bankoa.models.entity.CommentDetail;
import com.meishipintu.bankoa.models.entity.CommentInfo;
import com.meishipintu.bankoa.models.entity.NodeInfoNow;
import com.meishipintu.bankoa.models.entity.RemarkInfo;
import com.meishipintu.bankoa.models.entity.Task;
import com.meishipintu.bankoa.models.entity.UserInfo;
import com.meishipintu.bankoa.modules.TaskDetailModule;
import com.meishipintu.bankoa.presenters.TaskDetailPresenterImp;
import com.meishipintu.bankoa.views.adapter.CommentsAdapter;
import com.meishipintu.bankoa.views.adapter.RemarkAdapter;
import com.meishipintu.bankoa.views.adapter.ReplyClickListener;
import com.meishipintu.library.util.DateUtil;
import com.meishipintu.library.util.StringUtils;
import com.meishipintu.library.util.ToastUtils;
import com.meishipintu.library.view.CustomAlertDialog;
import com.meishipintu.library.view.CustomAlertDialog2;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/1.
 * <p>
 * 功能介绍：任务详情页面（小）
 */

public class TaskDetailActivity extends BasicActivity implements TaskDetailContract.IView {

    private static final String TAG = "BankOA-taskDetail";
    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.process_line_left1)
    View processLineLeft1;
    @BindView(R.id.process_line_left2)
    View processLineLeft2;
    @BindView(R.id.process_line_right1)
    View processLineRight1;
    @BindView(R.id.process_line_right2)
    View processLineRight2;
    @BindView(R.id.iv_process_left)
    ImageView ivProcessLeft;
    @BindView(R.id.tv_process_left)
    TextView tvProcessLeft;
    @BindView(R.id.iv_process_right)
    ImageView ivProcessRight;
    @BindView(R.id.tv_process_right)
    TextView tvProcessRight;
    @BindView(R.id.tv_process_now)
    TextView tvProcessNow;
    @BindView(R.id.tv_percentage)
    TextView tvPercentage;
    @BindView(R.id.tv_time_remain)
    TextView tvTimeRemain;
    @BindView(R.id.tv_isOutOfTime)
    TextView tvOutOfTime;
    @BindView(R.id.bt_finish)
    Button btFinish;

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_job_title)
    TextView tvJobTitle;

    @BindView(R.id.lv_remark)
    RecyclerView lvRemark;
    @BindView(R.id.rv_comment)
    RecyclerView rvComment;

    @BindView(R.id.tv_add_remark)
    TextView tvAddRemark;
    @BindView(R.id.et_remark)
    EditText etRemark;
    @BindView(R.id.scroll)
    ScrollView scrollView;
    @BindView(R.id.tv_subTitle)
    TextView tvSubTitle;

    private String sponsorId;       //发起人id
    private String supervisorId;    //监管人id
    private String supervisorLevel;    //监管人level


    private String taskId;                      //任务id
    private String taskLevelNow;                //当前节点level
    private String taskType;                    //任务type
    private RemarkAdapter remarkAdapter;        //备注Adapter
    private List<RemarkInfo> remarkList;        //备注数据
    private CommentsAdapter commentsAdapter;    //评论Adapter
    private List<CommentDetail> commentList;    //评论数据
    private String pid = "0";                   //默认评论父节点为0
    private InputMethodManager inputService;    //软键盘管理者
    private Dialog mDialog;


    @Inject
    TaskDetailPresenterImp mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        ButterKnife.bind(this);
        Task task = (Task) getIntent().getSerializableExtra("task");
        taskId = task.getId();
        sponsorId = task.getSponsor_id();
        supervisorId = getIntent().getStringExtra("supervisor_id");
        supervisorLevel = getIntent().getStringExtra("supervisor_level");
        inputService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //评论增加监听，在失焦时重置pid
        etRemark.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "focus changed:" + hasFocus);
                if (!hasFocus) {
                    etRemark.setText("");
                    pid = "0";
                    btFinish.requestFocus();
                }
            }
        });
        DaggerTaskDetailComponent.builder().taskDetailModule(new TaskDetailModule(this))
                .build().inject(this);
        initUI();
    }

    private void initUI() {
        mPresenter.getTaskInfo(taskId);
        if (supervisorId != null && !supervisorId.equals(sponsorId)) {
            tvAddRemark.setText(R.string.comment);
            etRemark.setHint(R.string.comment_please);
        }
        tvSubTitle.setText(R.string.del);
        tvSubTitle.setVisibility(View.VISIBLE);
        tvSubTitle.setTextColor(0xffff6c5d);
    }

    @OnClick({R.id.bt_back, R.id.bt_finish, R.id.bt_see_all, R.id.tv_add_remark, R.id.tv_subTitle})
    public void onClick(View view) {
        int totalLevel;
        switch (view.getId()) {
            case R.id.bt_finish:
                if (OaApplication.nodeNumber.get(taskType) != null) {
                    totalLevel = OaApplication.nodeNumber.get(taskType);
                    Log.d(TAG, "totalLEvel:" + totalLevel);
                    if (Integer.parseInt(taskLevelNow) < totalLevel) {
                        mPresenter.setTaskNodeFinished(sponsorId, taskId);
                    } else {
                        Intent intent = new Intent(this, PaymentEnterActivity.class);
                        intent.putExtra("task_id", taskId);
                        startActivityForResult(intent, Constans.FINISH_AND_INPUT);
                    }
                } else {
                    ToastUtils.show(this, R.string.err_net, true);
                }
                break;
            case R.id.bt_see_all:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("task_id", taskId);
                intent.putExtra("uid", sponsorId);
                startActivityForResult(intent,Constans.SEE_ALL);
                break;
            case R.id.tv_add_remark:
                String input = etRemark.getText().toString();
                if (StringUtils.isNullOrEmpty(input)) {
                    ToastUtils.show(this, R.string.err_empty_input, true);
                }
                if (supervisorId == null || supervisorId.equals(sponsorId)) {
                    mPresenter.addNodeRemarks(taskId, taskLevelNow, input, sponsorId);
                } else {
                    mPresenter.addNodeComment(new CommentInfo(sponsorId, supervisorId, supervisorLevel
                            , taskId, taskLevelNow, input, pid));
                }
                break;
            case R.id.bt_back:
                onBackPressed();
                break;
            case R.id.tv_subTitle:
                mDialog = new CustomAlertDialog(this, R.style.CustomDialog, new CustomAlertDialog.OnItemClickListener() {
                    @Override
                    public void onPositiveClick(Dialog dialog) {
                        dialog.dismiss();
                        mPresenter.deletTask(taskId);
                    }

                    @Override
                    public void onNegativeClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                mDialog.show();
                break;
        }
    }

    private void changSoftInputWindow(boolean close) {
        //如果软键盘在显示，则强制隐藏
        if (close && inputService.isActive()) {
            inputService.hideSoftInputFromWindow(etRemark.getWindowToken(), 0);
        }
        if (!close) {
            inputService.showSoftInput(etRemark, InputMethodManager.SHOW_FORCED);
        }
    }

    //from TaskDetailContract.IView
    @Override
    public void showError(String errMsg) {
        ToastUtils.show(this, errMsg, true);
    }

    //from TaskDetailContract.IView
    @Override
    public void showGraphic(NodeInfoNow nodeInfoNow) {
        taskLevelNow = nodeInfoNow.getNodeNowLevel();
        taskType = nodeInfoNow.getTaskType();
        if (OaApplication.nodeNumber.get(taskType) != null) {
            int level = Integer.parseInt(taskLevelNow);
            if (level == 1) {
                processLineLeft2.setVisibility(View.INVISIBLE);
                tvProcessLeft.setVisibility(View.INVISIBLE);
                ivProcessLeft.setVisibility(View.INVISIBLE);
                processLineLeft1.setVisibility(View.INVISIBLE);
            } else if (level == 2) {
                processLineLeft1.setVisibility(View.INVISIBLE);
                processLineLeft2.setVisibility(View.VISIBLE);
                tvProcessLeft.setVisibility(View.VISIBLE);
                ivProcessLeft.setVisibility(View.VISIBLE);
            } else if (level > OaApplication.nodeNumber.get(taskType) - 1) {
                processLineRight1.setVisibility(View.INVISIBLE);
                if (level == OaApplication.nodeNumber.get(taskType)) {
                    btFinish.setText(R.string.input);
                    processLineRight2.setVisibility(View.INVISIBLE);
                    tvProcessRight.setVisibility(View.INVISIBLE);
                    ivProcessRight.setVisibility(View.INVISIBLE);
                }
            } else {
                processLineLeft1.setVisibility(View.VISIBLE);
                processLineLeft2.setVisibility(View.VISIBLE);
                tvProcessLeft.setVisibility(View.VISIBLE);
                ivProcessLeft.setVisibility(View.VISIBLE);
                processLineRight1.setVisibility(View.VISIBLE);
                processLineRight2.setVisibility(View.VISIBLE);
                tvProcessRight.setVisibility(View.VISIBLE);
                ivProcessRight.setVisibility(View.VISIBLE);
            }

            if (nodeInfoNow.getNodeBeforeName() != null) {
                tvProcessLeft.setText(nodeInfoNow.getNodeBeforeName());
                ivProcessLeft.setImageResource(nodeInfoNow.isNodeBeforeCs() ? R.drawable.icon_overtime
                        : R.drawable.icon_finish_green);
                tvProcessLeft.setTextColor(nodeInfoNow.isNodeBeforeCs() ? 0xffff6c5d : 0xff15d5c8);
            }
            if (nodeInfoNow.getNodeAfterName() != null) {
                tvProcessRight.setText(nodeInfoNow.getNodeAfterName());
            }
            if (System.currentTimeMillis() / 1000 >= Long.parseLong(nodeInfoNow.getTimeRemain())) {
                tvOutOfTime.setText("超时 ");
                tvOutOfTime.setTextColor(0xffff6c5d);
            } else {
                tvOutOfTime.setText("距离此节点截止时间还有 ");
                tvOutOfTime.setTextColor(0xff9ca7b2);
            }
            tvProcessNow.setText(nodeInfoNow.getNodeNowName());
            tvTitle.setText(nodeInfoNow.getTaskname());

            Log.d(TAG, "taskType:" + taskType);
            Log.d(TAG, "totalLevel:" + OaApplication.nodeNumber.get(taskType));
            Log.d(TAG, "level Now:" + level);

            tvTimeRemain.setText(DateUtil.showTimeRemain(nodeInfoNow.getTimeRemain()));
            if (OaApplication.nodeNumber.get(taskType) != null) {
                int percentage = (level - 1) * 100 / OaApplication.nodeNumber.get(taskType);
                tvPercentage.setText(percentage + "");
            } else {
                tvPercentage.setText("--");
            }
        } else {
            ToastUtils.show(this, R.string.err_net, true);
        }
    }

    //from TaskDetailContract.IView
    @Override
    public void showUserInfo(UserInfo userInfo) {
        tvName.setText(userInfo.getUser_name() + " (工号： " + userInfo.getJob_number() + ")");
        String title;
        switch (Integer.parseInt(userInfo.getLevel())) {
            case 1:
                title = "行长";
                break;
            case 2:
                title = "主管";
                break;
            case 3:
                title = "员工";
                break;
            default:
                title = "员工";
                break;
        }
        if (Integer.parseInt(userInfo.getLevel()) == 1) {
            tvJobTitle.setText(title);
        } else {
            tvJobTitle.setText(userInfo.getDepartment_name() + " " + title);
        }

    }

    //from TaskDetailContract.IView
    @Override
    public void showRemarks(List<RemarkInfo> remarkInfoList) {
        Log.d(TAG, "this node remark size:" + remarkInfoList.size());
        if (remarkAdapter == null) {
            lvRemark.setItemAnimator(new DefaultItemAnimator());
            lvRemark.setLayoutManager(new LinearLayoutManager(this));
            remarkList = remarkInfoList;
            remarkAdapter = new RemarkAdapter(this, remarkList);
            lvRemark.setAdapter(remarkAdapter);
        } else {
            remarkList.clear();
            remarkList.addAll(remarkInfoList);
            remarkAdapter.notifyDataSetChanged();
        }
    }

    //from TaskDetailContract.IView
    @Override
    public void showComments(List<CommentDetail> commentDetailList) {
        Log.d(TAG, "this node remark size:" + commentDetailList.size());

        List<CommentDetail> commentDetailData = new ArrayList<>();
        //遍历数组后将线性结构转换为树状结构
        for (CommentDetail commentDetail : commentDetailList) {
            if ("0".equals(commentDetail.getPid())) {
                //一级评论
                commentDetailData.add(commentDetail);
            } else {
                for (CommentDetail firstLevelComment : commentDetailData) {
                    if (commentDetail.getPid().equals(firstLevelComment.getId())) {
                        firstLevelComment.setComment(commentDetail);
                    }
                }
            }
        }
        if (commentsAdapter == null) {
            rvComment.setItemAnimator(new DefaultItemAnimator());
            rvComment.setLayoutManager(new LinearLayoutManager(this));
            commentList = commentDetailData;
            commentsAdapter = new CommentsAdapter(this, commentList, new ReplyClickListener() {
                @Override
                public void onClick(String id) {
                    pid = id;
                    etRemark.requestFocus();
                    changSoftInputWindow(false);
                }
            });
            rvComment.setAdapter(commentsAdapter);
        } else {
            commentList.clear();
            commentList.addAll(commentDetailData);
            commentsAdapter.notifyDataSetChanged();
        }
    }

    //from TaskDetailContract.IView
    @Override
    public void onAddSuccess(int type) {
        ToastUtils.show(this, type == 1 ? R.string.add_comment_success : R.string.add_remark_success, true);
        etRemark.setText("");
        etRemark.clearFocus();
        changSoftInputWindow(true);
        //刷新界面
        mPresenter.getTaskInfo(taskId);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    //from TaskDetailContract.IView
    @Override
    public void onFinishNode() {
        if (OaApplication.nodeNumber.get(taskType) != null) {
            if (taskLevelNow.equals(OaApplication.nodeNumber.get(taskType) + "")) {
                setResult(RESULT_OK);
                ToastUtils.show(this, "本项目已完成", true);
                this.finish();
            } else {
                ToastUtils.show(this, "当前步骤已完成", true);
                mPresenter.getTaskInfo(taskId);
            }
        } else {
            ToastUtils.show(this, R.string.err_net, true);
        }
    }

    //from TaskDetailContract.IView
    @Override
    public void onDeletSuccess() {
        mDialog = new CustomAlertDialog2(this, R.style.CustomDialog, new CustomAlertDialog2.OnItemClickListener() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                TaskDetailActivity.this.finish();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        mDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constans.FINISH_AND_INPUT && resultCode == RESULT_OK) {
            //完成信息录入
            mPresenter.setTaskNodeFinished(sponsorId, taskId);
        }
        if (requestCode == Constans.SEE_ALL) {
            mPresenter.getTaskInfo(taskId);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //点击返回键取消输入框的焦点并重设pid
        if (keyCode == KeyEvent.KEYCODE_BACK && etRemark.hasFocus()) {
            etRemark.clearFocus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mPresenter.unSubscrib();
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = null;
        }
        super.onDestroy();
    }
}
