package com.surexu.sesame.model.base;

import java.util.ArrayList;
import java.util.List;

import com.surexu.sesame.data.Model;
import com.surexu.sesame.model.extensions.ExtensionsHandle;
import com.surexu.sesame.model.normal.answerAI.AnswerAI;
import com.surexu.sesame.model.normal.base.BaseModel;
import com.surexu.sesame.model.task.antDodo.AntDodo;
import com.surexu.sesame.model.task.antFarm.AntFarm;
import com.surexu.sesame.model.task.antForest.AntForestV2;
import com.surexu.sesame.model.task.antMember.AntMember;
import com.surexu.sesame.model.task.antOcean.AntOcean;
import com.surexu.sesame.model.task.antOrchard.AntOrchard;
import com.surexu.sesame.model.task.antSports.AntSports;
import com.surexu.sesame.model.task.antStall.AntStall;
import com.surexu.sesame.model.task.greenFinance.GreenFinance;
import com.surexu.sesame.model.task.protectEcology.ProtectEcology;
import lombok.Getter;

public class ModelOrder {

    @Getter
    private static final List<Class<? extends Model>> clazzList = new ArrayList<>();

    static {
        clazzList.add(BaseModel.class);
        clazzList.add(AntForestV2.class);
        clazzList.add(AntFarm.class);
        clazzList.add(AntStall.class);
        clazzList.add(AntOrchard.class);
        clazzList.add(ProtectEcology.class);
        clazzList.add(AntDodo.class);
        clazzList.add(AntOcean.class);
        clazzList.add(AntSports.class);
        clazzList.add(AntMember.class);
        clazzList.add(GreenFinance.class);
        clazzList.add(AnswerAI.class);

        ExtensionsHandle.handleAlphaRequest("ModelOrder", "addExtensionsClass", clazzList);
    }
}