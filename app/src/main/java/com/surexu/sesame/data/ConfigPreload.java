package com.surexu.sesame.data;

import com.surexu.sesame.util.FileUtil;
import com.surexu.sesame.util.StringUtil;
import com.surexu.sesame.util.idMap.AnimalIdMap;
import com.surexu.sesame.util.idMap.AntDodoTaskListMap;
import com.surexu.sesame.util.idMap.AntFarmDoFarmTaskListMap;
import com.surexu.sesame.util.idMap.AntFarmDrawMachineTaskListMap;
import com.surexu.sesame.util.idMap.AntForestHuntTaskListMap;
import com.surexu.sesame.util.idMap.AntForestVitalityTaskListMap;
import com.surexu.sesame.util.idMap.AntMemberTaskListMap;
import com.surexu.sesame.util.idMap.AntOceanAntiepTaskListMap;
import com.surexu.sesame.util.idMap.AntOceanFishBlackListMap;
import com.surexu.sesame.util.idMap.AntOrchardTaskListMap;
import com.surexu.sesame.util.idMap.AntSportsTaskListMap;
import com.surexu.sesame.util.idMap.AntStallTaskListMap;
import com.surexu.sesame.util.idMap.BeachIdMap;
import com.surexu.sesame.util.idMap.CooperationIdMap;
import com.surexu.sesame.util.idMap.FarmOrnamentsIdMap;
import com.surexu.sesame.util.idMap.GameCenterMallItemMap;
import com.surexu.sesame.util.idMap.MarathonIdMap;
import com.surexu.sesame.util.idMap.MemberBenefitIdMap;
import com.surexu.sesame.util.idMap.MemberCreditSesameTaskListMap;
import com.surexu.sesame.util.idMap.NewAncientTreeIdMap;
import com.surexu.sesame.util.idMap.PathThemeMapListMap;
import com.surexu.sesame.util.idMap.PlantSceneIdMap;
import com.surexu.sesame.util.idMap.PromiseSimpleTemplateIdMap;
import com.surexu.sesame.util.idMap.ReserveIdMap;
import com.surexu.sesame.util.idMap.TreeIdMap;
import com.surexu.sesame.util.idMap.UserIdMap;
import com.surexu.sesame.util.idMap.VitalityBenefitIdMap;
import com.surexu.sesame.util.idMap.ForestHuntIdMap;
import com.surexu.sesame.util.idMap.rpcRequestMap;

/**
 * 配置相关的预加载逻辑（原 SettingsActivity / NewSettingsActivity 中的初始化）。
 * miuix Compose 界面复用同一套数据，必须在这里完成 IdMap 加载与 ConfigV2.load，
 * 否则 SELECT_ONE / SELECT 等字段的选项列表为空。
 */
public final class ConfigPreload {

    private ConfigPreload() {
    }

    public static void prepare(String userId) {
        UserIdMap.setCurrentUserId(userId);
        UserIdMap.load(userId);
        CooperationIdMap.load(userId);
        VitalityBenefitIdMap.load(userId);
        GameCenterMallItemMap.load(userId);
        FarmOrnamentsIdMap.load(userId);
        MemberBenefitIdMap.load(userId);
        PromiseSimpleTemplateIdMap.load(userId);
        TreeIdMap.load();
        ReserveIdMap.load();
        AnimalIdMap.load();
        MarathonIdMap.load();
        NewAncientTreeIdMap.load();
        BeachIdMap.load();
        PlantSceneIdMap.load();
        rpcRequestMap.load();
        ForestHuntIdMap.load();
        MemberCreditSesameTaskListMap.load();
        AntForestVitalityTaskListMap.load();
        AntForestHuntTaskListMap.load();
        AntFarmDoFarmTaskListMap.load();
        AntFarmDrawMachineTaskListMap.load();
        AntDodoTaskListMap.load();
        AntOceanAntiepTaskListMap.load();
        AntOceanFishBlackListMap.load();
        AntOrchardTaskListMap.load();
        AntStallTaskListMap.load();
        AntSportsTaskListMap.load();
        PathThemeMapListMap.load();
        AntMemberTaskListMap.load();
        ConfigV2.load(userId);
    }

    public static boolean isEmpty(String userId) {
        return StringUtil.isEmpty(userId);
    }

    public static java.io.File getConfigFile(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return FileUtil.getDefaultConfigV2File();
        }
        return FileUtil.getConfigV2File(userId);
    }
}
