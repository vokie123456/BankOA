package com.meishipintu.bankoa.models.http;

import com.meishipintu.bankoa.models.entity.HttpResult;
import com.meishipintu.bankoa.models.entity.PaymentInfo;
import com.meishipintu.bankoa.models.entity.RemarkInfo;
import com.meishipintu.bankoa.models.entity.Task;
import com.meishipintu.bankoa.models.entity.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Administrator on 2017/3/1.
 */

public interface HttpService {

    //登录接口
    @FormUrlEncoded
    @POST("Home/Api/login")
    Observable<ResponseBody> loginService(@Field("mobile") String tel, @Field("password") String psw);

    //检查审核是否通过接口
    @FormUrlEncoded
    @POST("Home/Api/getRegistStatus")
    Observable<HttpResult<JSONObject>> getRegisterStatusService(@Field("mobile") String mobile);

    //获取验证码接口
    @FormUrlEncoded
    @POST("Home/Api/getVerifyCodeByMobile")
    Observable<ResponseBody> getVerifyCodeService(@Field("mobile") String mobile);

    //注册接口
    @FormUrlEncoded
    @POST("Home/Api/user_regist_step_finish")
    Observable<HttpResult<UserInfo>> registerService(@Field("mobile") String mobile
            , @Field("verify") String verifyCode, @Field("password") String psw);

    //发起任务
    @FormUrlEncoded
    @POST("Home/Api/startTaskOfStepOne")
    Observable<HttpResult<Task>> triggerTaskService(@Field("credit_name") String credit_name
            , @Field("apply_money") String apply_money, @Field("credit_center_branch") String credit_center_branch
            , @Field("credit_branch") String credit_branch, @Field("task_type") String task_type
            , @Field("task_name") String task_name, @Field("credit_manager") String credit_manager
            , @Field("sponsor_id") String sponsor_id, @Field("sponsor_level ") String sponsor_level);

    //获取当前任务信息
    @FormUrlEncoded
    @POST("Home/Api/getNowTaskAllInfo")
    Observable<ResponseBody> getTaskInfoNowService(@Field("task_id") String taskId);

    //添加备注接口
    @FormUrlEncoded
    @POST("Home/Api/doTaskRemark")
    Observable<ResponseBody> addRemarkService(@Field("task_id") String task_id, @Field("task_level") String task_level
            , @Field("remark_content") String remarkContent, @Field("remark_user_id") String remark_user_id);

    //添加评论接口
    @FormUrlEncoded
    @POST("Home/Api/doTaskComment")
    Observable<ResponseBody> addCommentService(@Field("comment_user_id") String userId, @Field("comment_user_level") String user_level
            , @Field("task_id") String taskId, @Field("task_level") String level, @Field("comment_content") String cotent
            , @Field("pid") String pid);

    //获取中心支行列表
    @POST("Home/Api/getCenterBranch")
    Observable<ResponseBody> getCenterBranchListService();

    //获取支行列表
    @POST("Home/Api/getBranch")
    Observable<ResponseBody> getBranchListService();

    //获取部门列表
    @POST("Home/Api/getDepartmentList")
    Observable<ResponseBody> getDepartmentListService();

    //获取任务类型列表
    @POST("Home/Api/getTaskType")
    Observable<ResponseBody> getTaskTypeListService();

    //获取任务节点数
    @POST("Home/Api/getLastTask")
    Observable<ResponseBody> getLastTaskService();

    //获取节点名称列表
    @POST("Home/Api/getAllTask")
    Observable<ResponseBody> getNodeListService();

    //获取用户的任务列表
    @FormUrlEncoded
    @POST("Home/Api/getUserTask")
    Observable<HttpResult<List<Task>>> getTaskList(@Field("user_id") String uid, @Field("type") String type);

    //完成当前节点
    @FormUrlEncoded
    @POST("Home/Api/startOtherStepTask")
    Observable<ResponseBody> finishNodeService(@Field("user_id") String uid, @Field("task_id") String taskID);

    //录入还款信息
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("Home/Api/addLoanInfo")
    Observable<ResponseBody> addLoanInfoService(@Body PaymentInfo paymentInfo);

    //获取还款详情
    @FormUrlEncoded
    @POST("Home/Api/getRepaymentList")
    Observable<ResponseBody> getRepaymentInfoService(@Field("task_id") String taskID);

    //完成节点还款
    @FormUrlEncoded
    @POST("Home/Api/doFinishRepayment")
    Observable<ResponseBody> finishPaymentNodeService(@Field("id") String nodeId, @Field("task_id") String taskId
            , @Field("real_money") String realMoney);

    //获取员工列表
    @FormUrlEncoded
    @POST("Home/Api/getUserListByLevel")
    Observable<HttpResult<List<UserInfo>>> getClerkListService(@Field("uid") String uid, @Field("level") String level);

    //获取用户信息
    @FormUrlEncoded
    @POST("Home/Api/getUserInfoById")
    Observable<ResponseBody> getUserInfoByIdService(@Field("uid") String uid);
}
