package com.wonikrobotics.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

/**
 * Created by Notebook on 2016-08-04.
 */
public class RobotModifyDialog extends Activity implements View.OnClickListener {
    Button register, cancel;
    EditText name, uri;
    Switch master;
    String master_checked = "false";
    int idx = -1;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.addrobot);
        register = (Button) findViewById(R.id.btn_register_register);
        cancel = (Button) findViewById(R.id.btn_register_cancel);
        name = (EditText) findViewById(R.id.register_robotname);
        uri = (EditText) findViewById(R.id.register_roboturi);
        master = (Switch) findViewById(R.id.master_switch);
        master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                master_checked = String.valueOf(isChecked);
            }
        });
        register.setText("Modify");
        register.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Intent info = getIntent();
        idx = info.getIntExtra("ROBOTIDX", -1);
        name.setText(info.getStringExtra("ROBOTNAME"));
        uri.setText(info.getStringExtra("ROBOTURI"));
        master.setChecked(info.getBooleanExtra("ROBOTISMASTER", false));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register_register:
                Intent data = new Intent();
                data.putExtra("idx", idx);
                data.putExtra("name", name.getText().toString());
                data.putExtra("uri", uri.getText().toString());
                data.putExtra("master", master_checked);
                setResult(2, data);
                finish();
                break;
            case R.id.btn_register_cancel:
                setResult(-1);
                finish();
                break;
        }
    }
}
