package com.wonikrobotics.pathfinder.mc.mobilecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

/**
 * AddRobotDialog
 *
 * @author Weonwoo Joo
 * @date 1. 8. 2016
 *
 * @desription Dialog for registering new robot
 */
public class AddRobotDialog extends Activity implements View.OnClickListener {
    private Button register, cancel, QRCode;
    private EditText name, uri;
    private Switch master;
    private String master_checked = "false";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.addrobot);
        register = (Button) findViewById(R.id.btn_register_register);
        cancel = (Button) findViewById(R.id.btn_register_cancel);
        name = (EditText) findViewById(R.id.register_robotname);
        uri = (EditText) findViewById(R.id.register_roboturi);
        master = (Switch) findViewById(R.id.master_switch);
        QRCode = (Button) findViewById(R.id.btn_QRCode);

        // master switch change listener

        master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                master_checked = String.valueOf(isChecked);
            }
        });
        register.setOnClickListener(this);
        cancel.setOnClickListener(this);
        QRCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register_register:

                // on click add button

                Intent data = new Intent();
                data.putExtra("name", name.getText().toString());
                data.putExtra("uri", uri.getText().toString());
                data.putExtra("master", master_checked);
                setResult(1, data);
                finish();
                break;
            case R.id.btn_register_cancel:

                // on cancel. setResult(-1) make callback doesn't work

                setResult(-1);
                finish();
                break;
            case R.id.btn_QRCode:
                Intent qrintent = new Intent("com.google.zxing.client.android.SCAN");
                qrintent.putExtra("SCAN_MODE", "ALL");
                startActivityForResult(qrintent, 2);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                String content = intent.getStringExtra("SCAN_RESULT");
                uri.setText(content);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}