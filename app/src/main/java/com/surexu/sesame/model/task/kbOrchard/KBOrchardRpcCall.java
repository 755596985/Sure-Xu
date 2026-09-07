package com.surexu.sesame.model.task.kbOrchard;

import android.content.Context;

import com.surexu.sesame.hook.ApplicationHook;
import com.surexu.sesame.util.Log;
import com.surexu.sesame.util.XHelpers;

import org.json.JSONObject;

/**
 * 饿了么果园（营养果园）MTOP 主动调用通道。
 *
 * <p>饿了么果园的接口托管在 mtop.ele.me（customHost），走 MTOP 协议而非支付宝原生 RPC，
 * 本工程不引入 mtopsdk 依赖，因此运行时通过 XHelpers 反射宿主支付宝进程内已初始化好的
 * mtopsdk 单例（mPaaS 必有）：构造 MtopRequest → Mtop.build → 设置 customDomain=mtop.ele.me
 * → syncRequest。登录态 / wua / 签名均由宿主 SDK 自动处理（needWua 接口同官方）。
 *
 * <p>请求 payload 逐字取自线上抓包日志（shared_log_1788763967567.log），保持接口原样。
 */
public class KBOrchardRpcCall {

    private static final String TAG = KBOrchardRpcCall.class.getSimpleName();

    private static final String ELE_HOST = "mtop.ele.me";
    private static final String TTID = "wandoujia@alipay_android_12.12.8.8000";
    private static final String MINIAPP_ID = "2021001159679896";

    /* ---------------- 各接口定义（api | version | needWua） ---------------- */
    public static final String API_INDEX_QUERY = "mtop.alsc.playgame.orchard.index.batch.query"; // 1.0 false
    public static final String API_QUERY_SIGN_INFO = "mtop.koubei.interactioncenter.orchard.sign.querySignInfo"; // 1.0 false
    public static final String API_RECEIVE_SIGN_AWARD = "mtop.koubei.interactioncenter.orchard.sign.receiveSignInAward"; // 1.1 true
    public static final String API_FUTUREWATER_QUERY = "mtop.ele.playgame.orchard.futurewater.query"; // 1.0 false
    public static final String API_FUTUREWATER_RECEIVE = "mtop.ele.playgame.orchard.futurewater.receive"; // 1.0 false
    public static final String API_QUERY_TASK = "mtop.ele.biz.growth.task.core.querytask"; // 1.0 false
    public static final String API_RECEIVE_PRIZE = "mtop.ele.biz.growth.task.core.receiveprize"; // 1.0 true
    public static final String API_USE_PROP = "mtop.alsc.playgame.orchard.roleOperate.useProp"; // 1.0 true
    // 浏览任务曝光上报（纯 RPC 完成浏览，数据来源 shared_log_1788774358016.log）
    public static final String API_PAGEVIEW = "mtop.ele.biz.growth.task.event.pageview"; // 1.0 true

    /** 浏览上报固定安全码（抓包值，失效可改） */
    public static final String PAGEVIEW_ASAC = "2A20B11WIAXCI9QYYXRIR0";

    /* ---------------- index.batch.query 原始 payload（抓包原样，坐标动态替换） ---------------- */
    private static final String IDX_LOCATION = "[{\"latitude\":\"%s\",\"longitude\":\"%s\",\"lat\":\"%s\",\"lng\":\"%s\"}]";
    private static final String IDX_MAIN_TEMPLATE = "{\"bizCode\":\"main\",\"blockRequestList\":\"[{\\\"blockCode\\\":\\\"603040_6723057310\\\",\\\"status\\\":\\\"PUBLISH\\\",\\\"tagCallWay\\\":\\\"SYNC\\\",\\\"useRequestBlockTags\\\":false}]\",\"extData\":\"{\\\"orchardEntrance\\\":\\\"chinfo=#alscexsrc=#scene=H5#task=\\\",\\\"ORCHARD_ELE_MARK\\\":\\\"KB_ORCHARD\\\",\\\"orchardVersion\\\":\\\"20260525\\\"}\",\"locationInfos\":\"%s\",\"source\":\"KB_ORCHARD\"}";
    private static final String IDX_ASSETS_TEMPLATE = "{\"bizCode\":\"assets\",\"blockRequestList\":\"[{\\\"blockCode\\\":\\\"603040_6723057310\\\",\\\"status\\\":\\\"PUBLISH\\\",\\\"tagCallWay\\\":\\\"SYNC\\\",\\\"useRequestBlockTags\\\":true,\\\"blockTags\\\":[{\\\"extData\\\":{\\\"parent_tag_id\\\":\\\"202410301600123484352000000000091\\\"},\\\"tagCallWay\\\":\\\"SYNC\\\",\\\"tagCode\\\":\\\"assets\\\",\\\"tagId\\\":\\\"202410301600123476973000000000046\\\",\\\"tagName\\\":\\\"assets\\\",\\\"tagParam\\\":{\\\"options\\\":[{\\\"cpnCode\\\":\\\"PROPERTY\\\",\\\"cpnCollectionId\\\":\\\"20200629151931186912445875\\\",\\\"operate\\\":\\\"QUERY_SUM\\\",\\\"params\\\":{\\\"propertyType\\\":\\\"integral\\\"}}],\\\"tagSourceId\\\":\\\"20200629151859103125248022\\\"},\\\"tagRouteScene\\\":\\\"V2\\\",\\\"tagSource\\\":\\\"alsc-play\\\",\\\"tagSourceId\\\":\\\"20200629151859103125248022\\\",\\\"tagSourceType\\\":\\\"alsc-play\\\"}]}]\",\"extData\":\"{\\\"ORCHARD_ELE_MARK\\\":\\\"KB_ORCHARD\\\",\\\"orchardVersion\\\":\\\"20260525\\\"}\",\"locationInfos\":\"%s\",\"source\":\"KB_ORCHARD\"}";

    /* ============================ 通用 MTOP 请求 ============================ */

    /**
     * 反射调用宿主 mtopsdk 发送 MTOP 请求。
     *
     * @param apiName     接口名
     * @param apiVersion  接口版本
     * @param dataText    data 透传 JSON 字符串
     * @param needWua     是否需要 wua（与抓包一致）
     * @return data 部分 JSON 字符串；失败返回 null
     */
    public static String request(String apiName, String apiVersion, String dataText, boolean needWua) {
        try {
            ClassLoader cl = ApplicationHook.getClassLoader();
            Class<?> mtopCls = XHelpers.findClass("mtopsdk.mtop.intf.Mtop", cl);
            Object mtop = XHelpers.callStaticMethod(mtopCls, "instance", getContext());

            Object request = XHelpers.newInstance(XHelpers.findClass("mtopsdk.mtop.domain.MtopRequest", cl));
            trySet(request, "setApiName", apiName);
            trySetFirst(request, new String[]{"setVersion", "setApiVersion"}, apiVersion);
            trySetFirst(request, new String[]{"setData", "setJSONData"}, dataText);
            trySetFirst(request, new String[]{"setNeedWua"}, needWua);
            trySetFirst(request, new String[]{"setUseHttps"}, Boolean.TRUE);

            Object builder;
            try {
                builder = XHelpers.callMethod(mtop, "build", request);
            } catch (Throwable t) {
                builder = XHelpers.callMethod(mtop, "build", request, TTID);
            }
            trySetFirst(builder, new String[]{"setCustomDomain", "setCustomHost"}, ELE_HOST);
            trySetFirst(builder, new String[]{"setReqMethod", "reqMethod"}, "POST");

            tryHeader(builder, "x-miniapp-id-taobao", MINIAPP_ID);
            tryHeader(builder, "x-mini-appkey", "default_appkey");
            tryHeader(builder, "x-req-appkey", "default_appkey");
            tryHeader(builder, "x-ua", "AliApp(AP/12.12.8.8000) Ariver/12.12.8.8000");
            tryHeader(builder, "x-miniapp-env", "{\"nbsn\":\"ONLINE\",\"nbsource\":\"online\"}");
            tryHeader(builder, "appId", MINIAPP_ID);
            tryHeader(builder, "ext-url-ref", "r.ele.me");
            tryHeader(builder, "referer", "https://r.ele.me/orchard-h5/orchard/");

            Object response = XHelpers.callMethod(builder, "syncRequest");

            boolean success = false;
            try {
                Object v = XHelpers.callMethod(response, "isSuccess");
                success = v instanceof Boolean && (Boolean) v;
            } catch (Throwable ignored) {
            }
            if (!success) {
                String rc = "";
                String rm = "";
                try {
                    Object o = XHelpers.callMethod(response, "getRetCode");
                    if (o != null) rc = o.toString();
                } catch (Throwable ignored) {
                }
                try {
                    Object o = XHelpers.callMethod(response, "getRetMsg");
                    if (o != null) rm = o.toString();
                } catch (Throwable ignored) {
                }
                Log.i(TAG, apiName + " 请求失败 retCode=" + rc + " retMsg=" + rm);
                return null;
            }

            Object data = getData(response);
            if (data == null) {
                return null;
            }
            String json = data.toString();
            // 校验为合法 JSON，避免返回类型差异导致下游解析异常
            try {
                new JSONObject(json);
            } catch (Throwable t) {
                Log.i(TAG, apiName + " 响应非 JSON，已丢弃");
                return null;
            }
            return json;
        } catch (Throwable t) {
            Log.i(TAG, apiName + " MTOP 调用异常");
            Log.printStackTrace(TAG, t);
            return null;
        }
    }

    /* ============================ 业务接口封装 ============================ */

    /** 果园主页/资产查询（main 或 assets） */
    public static String index(String bizCode, String longitude, String latitude) {
        String loc = String.format(IDX_LOCATION, latitude, longitude, latitude, longitude);
        String data;
        if ("assets".equals(bizCode)) {
            data = String.format(IDX_ASSETS_TEMPLATE, loc);
        } else {
            data = String.format(IDX_MAIN_TEMPLATE, loc);
        }
        return request(API_INDEX_QUERY, "1.0", data, false);
    }

    /** 签到信息查询（响应含 signInPrizeList） */
    public static String querySignInfo(String latitude, String longitude) {
        JSONObject data = new JSONObject();
        try {
            data.put("bizScene", "orchard_signin");
            data.put("latitude", latitude);
            data.put("longitude", longitude);
        } catch (Throwable ignored) {
        }
        return request(API_QUERY_SIGN_INFO, "1.0", data.toString(), false);
    }

    /** 领取签到奖励 */
    public static String receiveSignInAward(String prizeNumId, String signInDate, String latitude, String longitude) {
        JSONObject data = new JSONObject();
        try {
            data.put("bizScene", "orchard_signin");
            data.put("extInfo", "{\"prizeNumId\":\"" + prizeNumId + "\"}");
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("signInDate", signInDate);
        } catch (Throwable ignored) {
        }
        return request(API_RECEIVE_SIGN_AWARD, "1.1", data.toString(), true);
    }

    /** 未来水（待收水滴）查询 */
    public static String futureWaterQuery() {
        JSONObject data = new JSONObject();
        try {
            data.put("bizScene", "KB_ORCHARD");
        } catch (Throwable ignored) {
        }
        return request(API_FUTUREWATER_QUERY, "1.0", data.toString(), false);
    }

    /** 领取未来水 */
    public static String futureWaterReceive() {
        JSONObject data = new JSONObject();
        try {
            data.put("bizScene", "KB_ORCHARD");
        } catch (Throwable ignored) {
        }
        return request(API_FUTUREWATER_RECEIVE, "1.0", data.toString(), false);
    }

    /** 任务列表查询（领奖前置） */
    public static String queryTask(String latitude, String longitude) {
        JSONObject data = new JSONObject();
        try {
            data.put("accountPlan", "HAVANA_COMMON");
            data.put("bizScene", "ORCHARD");
            data.put("locationInfos", "[{\"lng\":\"" + longitude + "\",\"lat\":\"" + latitude + "\"}]");
            data.put("missionCollectionId", "178");
        } catch (Throwable ignored) {
        }
        return request(API_QUERY_TASK, "1.0", data.toString(), false);
    }

    /** 领取任务奖励（missionId = missionDefId） */
    public static String receivePrize(String missionId, String latitude, String longitude) {
        JSONObject data = new JSONObject();
        try {
            data.put("accountPlan", "HAVANA_COMMON");
            data.put("bizScene", "ORCHARD");
            data.put("count", "1");
            data.put("locationInfos", "[{\"lng\":\"" + longitude + "\",\"lat\":\"" + latitude + "\"}]");
            data.put("missionCollectionId", "178");
            data.put("missionId", missionId);
        } catch (Throwable ignored) {
        }
        return request(API_RECEIVE_PRIZE, "1.0", data.toString(), true);
    }

    /** 使用道具（浇水，propertyTemplateId=462） */
    public static String useProp(String propertyTemplateId, String roleId, String latitude, String longitude) {
        JSONObject data = new JSONObject();
        try {
            data.put("actId", "20200629151859103125248022");
            data.put("bizScene", "KB_ORCHARD");
            data.put("collectionId", "20210812150109893985929183");
            data.put("extParams", "{\"orchardVersion\":\"20260525\",\"popWindowVersion\":\"V2\"}");
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("propertyTemplateId", propertyTemplateId);
            data.put("roleId", roleId);
            data.put("roleType", "KB_ORCHARD");
        } catch (Throwable ignored) {
        }
        return request(API_USE_PROP, "1.0", data.toString(), true);
    }

    /** 浏览任务曝光上报（纯 RPC 完成 PAGEVIEW 浏览任务）。参数取自 shared_log_1788774358016.log，asac 为抓包固定值。 */
    public static String pageView(String missionId, String missionXId, String pageFrom, String viewTime) {
        JSONObject data = new JSONObject();
        try {
            data.put("accountPlan", "HAVANA_COMMON");
            data.put("actionCode", "PAGEVIEW");
            data.put("asac", PAGEVIEW_ASAC);
            data.put("bizScene", "ORCHARD");
            data.put("collectionId", "178");
            data.put("missionId", missionId);
            data.put("missionXId", missionXId);
            data.put("pageFrom", pageFrom);
            data.put("sync", "false");
            data.put("viewTime", viewTime);
        } catch (Throwable ignored) {
        }
        return request(API_PAGEVIEW, "1.0", data.toString(), true);
    }

    /* ============================ 反射辅助 ============================ */

    private static Context getContext() throws Exception {
        return (Context) XHelpers.getStaticObjectField(ApplicationHook.class, "context");
    }

    private static void trySet(Object obj, String method, Object arg) {
        try {
            XHelpers.callMethod(obj, method, arg);
        } catch (Throwable ignored) {
        }
    }

    /** 依次尝试多个候选方法名（兼容不同 mtopsdk 版本差异） */
    private static void trySetFirst(Object obj, String[] names, Object arg) {
        for (String n : names) {
            try {
                XHelpers.callMethod(obj, n, arg);
                return;
            } catch (Throwable ignored) {
            }
        }
    }

    private static void tryHeader(Object builder, String key, String value) {
        String[] names = {"addReqHeader", "addHeader"};
        for (String n : names) {
            try {
                XHelpers.callMethod(builder, n, key, value);
                return;
            } catch (Throwable ignored) {
            }
        }
    }

    /** 尽量取 JSON 形态的 data（不同 SDK 版本返回类型不同） */
    private static Object getData(Object response) {
        String[] jsonMethods = {"getDataJsonObject", "getDataObject"};
        for (String m : jsonMethods) {
            try {
                Object o = XHelpers.callMethod(response, m);
                if (o != null) {
                    return o;
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            return XHelpers.callMethod(response, "getData");
        } catch (Throwable ignored) {
            return null;
        }
    }
}
