package com.surexu.sesame.data.modelFieldExt;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.surexu.sesame.R;
import com.surexu.sesame.data.ModelField;

public class StringModelField extends ModelField<String> {

    public StringModelField(String code, String name, String value) {
        super(code, name, value);
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public String getConfigValue() {
        return value;
    }

    @Override
    public void setConfigValue(String configValue) {
        value = configValue;
    }

    @Override
    public View getView(Context context) {
        return null;
    }

}
