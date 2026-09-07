package com.surexu.sesame.model.task.kbOrchard;

import com.surexu.sesame.data.ModelFields;
import com.surexu.sesame.data.ModelGroup;
import com.surexu.sesame.data.modelFieldExt.BooleanModelField;
import com.surexu.sesame.data.modelFieldExt.IntegerModelField;
import com.surexu.sesame.data.modelFieldExt.StringModelField;
import com.surexu.sesame.data.task.ModelTask;
import com.surexu.sesame.util.Log;
import com.surexu.sesame.util.Status;
import com.surexu.sesame.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/**
 * 饿了么果园（营养果园，KB_ORCHARD）自动化。
 *
 * <p>数据来源：抓包日志 shared_log_1788763967567.log。功能：
 * <ul>
 *   <li>收水滴 | 未来水：futurewater.query 可领时 futurewater.receive（+10 滴）</li>
 *   <li>收水滴 | 签到奖励：querySignInfo 定位今日 TODO_RECEIVE 奖励 → receiveSignInAward</li>
 *   <li>阳光卡 | 任务奖励：querytask 取 finishStatus=2 且存在可领 stage 的任务 → receiveprize</li>
 *   <li>浇水 | 自动浇水：查询 assets 水滴(462)，每次浇水消耗 10 滴、进度 +0.01，直至低于阈值或达次数上限</li>
 * </ul>
 * 所有接口经宿主 mtopsdk 反射调用（见 {@link KBOrchardRpcCall}）。
 */
public class KBOrchard extends ModelTask {

    private static final String TAG = KBOrchard.class.getSimpleName();
    private static final String NAME = "饿了么果园";
    private static final ModelGroup GROUP = ModelGroup.ORCHARD;

    /** 水源 templateId（抓包确认水滴为 462，每次浇水消耗 10） */
    private static final String WATER_PROP_ID = "462";
    private static final int WATER_PER_USE = 10;

    private IntegerModelField executeInterval;
    private BooleanModelField receiveFutureWater;
    private BooleanModelField signInReward;
    private BooleanModelField taskReward;
    private BooleanModelField autoWatering;
    private IntegerModelField waterTimes;
    private IntegerModelField waterThreshold;
    private StringModelField longitude;
    private StringModelField latitude;

    private String roleId = "";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ModelGroup getGroup() {
        return GROUP;
    }

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(executeInterval = new IntegerModelField("executeInterval", "执行间隔(毫秒)", 800));
        modelFields.addField(receiveFutureWater = new BooleanModelField("receiveFutureWater", "收水滴 | 未来水", true));
        modelFields.addField(signInReward = new BooleanModelField("signInReward", "收水滴 | 签到奖励", true));
        modelFields.addField(taskReward = new BooleanModelField("taskReward", "阳光卡 | 任务奖励", true));
        modelFields.addField(autoWatering = new BooleanModelField("autoWatering", "浇水 | 自动浇水", true));
        modelFields.addField(waterTimes = new IntegerModelField("waterTimes", "浇水 | 每次触发浇水次数(0=按水量自动)", 0));
        modelFields.addField(waterThreshold = new IntegerModelField("waterThreshold", "浇水 | 剩余水滴低于该值停止", 10));
        modelFields.addField(longitude = new StringModelField("longitude", "定位 | 经度", "113.75636"));
        modelFields.addField(latitude = new StringModelField("latitude", "定位 | 纬度", "22.792227"));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return true;
    }

    @Override
    public void run() {
        try {
            super.startTask();

            // 主页信息：解析出当前用户的 roleId（每个用户不同，浇水必填）
            JSONObject index = queryIndex("main");
            if (index == null) {
                Log.record("饿了么果园:主页信息获取失败，请确认已进入过饿了么果园(H5)");
                return;
            }
            roleId = findString(index, "roleId");
            if (roleId.isEmpty()) {
                Log.record("饿了么果园:未获取到 roleId");
                return;
            }

            if (receiveFutureWater.getValue()) {
                receiveFutureWater();
            }
            if (signInReward.getValue()) {
                signInReward();
            }
            if (taskReward.getValue()) {
                taskReward();
            }
            if (autoWatering.getValue()) {
                watering();
            }
            Log.i(TAG, "饿了么果园执行完成");
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* ============================ 收水滴：未来水 ============================ */

    private void receiveFutureWater() {
        try {
            String resp = KBOrchardRpcCall.futureWaterQuery();
            if (resp == null) {
                Log.i(TAG, "futureWater.query 失败");
                return;
            }
            if (!findBool(new JSONObject(resp), "canReceive")) {
                return;
            }
            if (KBOrchardRpcCall.futureWaterReceive() != null) {
                Log.record("饿了么果园:已领取未来水 +10滴");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    /* ============================ 收水滴：签到奖励 ============================ */

    private void signInReward() {
        try {
            String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            String resp = KBOrchardRpcCall.querySignInfo(latitude.getValue(), longitude.getValue());
            if (resp == null) {
                return;
            }
            JSONObject jo = new JSONObject(resp);
            JSONArray list = findArray(jo, "signInPrizeList");
            if (list == null) {
                return;
            }
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                if (item == null || !today.equals(item.optString("date"))) {
                    continue;
                }
                JSONObject ext = item.optJSONObject("ext");
                if (ext == null) {
                    continue;
                }
                JSONArray awards = ext.optJSONArray("awardInfo");
                if (awards == null) {
                    continue;
                }
                for (int j = 0; j < awards.length(); j++) {
                    JSONObject aw = awards.optJSONObject(j);
                    if (aw == null) {
                        continue;
                    }
                    if ("all".equals(aw.optString("channel")) && "TODO_RECEIVE".equals(aw.optString("status"))) {
                        String prizeNumId = aw.optString("prizeNumId");
                        String r = KBOrchardRpcCall.receiveSignInAward(prizeNumId, today, latitude.getValue(), longitude.getValue());
                        if (r != null) {
                            Log.record("饿了么果园:已领取签到水滴奖励(prizeNumId=" + prizeNumId + ")");
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    /* ============================ 阳光卡：任务奖励 ============================ */

    private void taskReward() {
        try {
            String resp = KBOrchardRpcCall.queryTask(latitude.getValue(), longitude.getValue());
            if (resp == null) {
                return;
            }
            JSONObject jo = new JSONObject(resp);
            JSONArray mlist = findArray(jo, "mlist");
            if (mlist == null) {
                return;
            }
            for (int i = 0; i < mlist.length(); i++) {
                JSONObject m = mlist.optJSONObject(i);
                if (m == null) {
                    continue;
                }
                long defId = m.optLong("missionDefId", 0);
                if (defId <= 0) {
                    continue;
                }
                String missionId = String.valueOf(defId);
                String flag = "KBOrchard::task::" + missionId;
                if (Status.hasFlagToday(flag)) {
                    continue;
                }
                // 完成任务判定：ext.extValue.finishStatus == 2
                int finishStatus = 0;
                JSONObject ext = m.optJSONObject("ext");
                if (ext != null) {
                    JSONObject ev = ext.optJSONObject("extValue");
                    if (ev != null) {
                        finishStatus = ev.optInt("finishStatus", 0);
                    }
                }
                if (finishStatus != 2) {
                    continue;
                }
                // 存在未领取 stage（rewardStatus != SUCCESS 表示可领）
                JSONArray stages = m.optJSONArray("missionStageDTOS");
                boolean canReceive = false;
                if (stages != null) {
                    for (int j = 0; j < stages.length(); j++) {
                        JSONObject s = stages.optJSONObject(j);
                        if (s != null && !"SUCCESS".equals(s.optString("rewardStatus"))) {
                            canReceive = true;
                            break;
                        }
                    }
                }
                if (!canReceive) {
                    continue;
                }
                String r = KBOrchardRpcCall.receivePrize(missionId, latitude.getValue(), longitude.getValue());
                if (r != null) {
                    Status.flagToday(flag);
                    Log.record("饿了么果园:已领取任务奖励 missionId=" + missionId);
                }
                TimeUtil.sleep(executeInterval.getValue());
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    /* ============================ 自动浇水 ============================ */

    private void watering() {
        try {
            int max = Math.max(0, waterTimes.getValue());
            int threshold = Math.max(1, waterThreshold.getValue());
            int done = 0;
            while (true) {
                if (max > 0 && done >= max) {
                    Log.record("饿了么果园:达到浇水次数上限(" + max + ")");
                    break;
                }
                JSONObject assets = queryIndex("assets");
                if (assets == null) {
                    Log.record("饿了么果园:资产查询失败，停止浇水");
                    break;
                }
                int water = findAssetValue(assets, WATER_PROP_ID);
                if (water < threshold) {
                    Log.record("饿了么果园:水滴不足(" + water + " < " + threshold + ")，停止浇水");
                    break;
                }
                String r = KBOrchardRpcCall.useProp(WATER_PROP_ID, roleId, latitude.getValue(), longitude.getValue());
                if (r == null) {
                    Log.record("饿了么果园:浇水失败，停止浇水");
                    break;
                }
                done++;
                Log.record("饿了么果园:浇水第" + done + "次，进度+0.01(约消耗10滴)");
                TimeUtil.sleep(executeInterval.getValue());
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    /* ============================ 内部工具 ============================ */

    private JSONObject queryIndex(String bizCode) {
        String resp = KBOrchardRpcCall.index(bizCode, longitude.getValue(), latitude.getValue());
        if (resp == null) {
            return null;
        }
        try {
            return new JSONObject(resp);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            return null;
        }
    }

    /** 递归查找第一个匹配的字符串字段 */
    private static String findString(Object o, String key) {
        if (o instanceof JSONObject) {
            JSONObject jo = (JSONObject) o;
            if (jo.has(key)) {
                Object v = jo.opt(key);
                if (v instanceof String) {
                    return (String) v;
                }
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                String r = findString(jo.opt(it.next()), key);
                if (r != null) {
                    return r;
                }
            }
        } else if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            for (int i = 0; i < arr.length(); i++) {
                String r = findString(arr.opt(i), key);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /** 递归查找第一个匹配的布尔字段 */
    private static Boolean findBool(Object o, String key) {
        if (o instanceof JSONObject) {
            JSONObject jo = (JSONObject) o;
            if (jo.has(key)) {
                Object v = jo.opt(key);
                if (v instanceof Boolean) {
                    return (Boolean) v;
                }
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                Boolean r = findBool(jo.opt(it.next()), key);
                if (r != null) {
                    return r;
                }
            }
        } else if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            for (int i = 0; i < arr.length(); i++) {
                Boolean r = findBool(arr.opt(i), key);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /** 递归查找第一个匹配的 JSONArray 字段 */
    private static JSONArray findArray(Object o, String key) {
        if (o instanceof JSONObject) {
            JSONObject jo = (JSONObject) o;
            if (jo.has(key)) {
                Object v = jo.opt(key);
                if (v instanceof JSONArray) {
                    return (JSONArray) v;
                }
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                JSONArray r = findArray(jo.opt(it.next()), key);
                if (r != null) {
                    return r;
                }
            }
        } else if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            for (int i = 0; i < arr.length(); i++) {
                JSONArray r = findArray(arr.opt(i), key);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /** 递归查找 templateId 对应资产的 value（整数值） */
    private static int findAssetValue(Object o, String templateId) {
        if (o instanceof JSONObject) {
            JSONObject jo = (JSONObject) o;
            String tid = jo.optString("templateId", "");
            if (templateId.equals(tid)) {
                Object v = jo.opt("value");
                if (v instanceof Number) {
                    return ((Number) v).intValue();
                }
                if (v instanceof String) {
                    try {
                        return Integer.parseInt((String) v);
                    } catch (Throwable ignored) {
                    }
                }
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                int r = findAssetValue(jo.opt(it.next()), templateId);
                if (r > 0) {
                    return r;
                }
            }
        } else if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            for (int i = 0; i < arr.length(); i++) {
                int r = findAssetValue(arr.opt(i), templateId);
                if (r > 0) {
                    return r;
                }
            }
        }
        return 0;
    }
}
