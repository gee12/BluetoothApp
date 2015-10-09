package com.icon.agnks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.icon.utils.MessageBox;

public class EditDeviceActivity extends Activity {

    EditText editDeviceName;
    EditText editMacAddress;
    EditText editCustomName;

    Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        editDeviceName = (EditText)findViewById(R.id.edittext_device_name);
        editMacAddress = (EditText)findViewById(R.id.edittext_mac_address);
        editCustomName = (EditText)findViewById(R.id.edittext_custom_name);

        device = (Device)getIntent().getSerializableExtra(Device.KEY_DEVICE_OBJECT);
        if (device == null) {
            MessageBox.shoter(this, "Устройство не определено");
            close(RESULT_CANCELED);
        }

        editDeviceName.setText(device.DeviceName);
        editMacAddress.setText(device.MacAddress);
        editCustomName.setText(device.CustomName);
    }

    public void apply(View view) {
        String customName =  editCustomName.getText().toString();
        if (customName.isEmpty()) {
            MessageBox.shoter(this, "Пользовательское имя устройства не заполнено");
            return;
        }
        device.CustomName = customName;

        close(RESULT_OK);
    }

    public void cancel(View view) {
        close(RESULT_CANCELED);
    }

    private void close(int result) {
        Intent returnIntent = new Intent();
        if (result == RESULT_OK) {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable(Device.KEY_DEVICE_OBJECT, device);
//            returnIntent.putExtras(bundle);

            returnIntent.putExtra(Device.KEY_DEVICE_CUSTOM_NAME, device.CustomName);
        }
        setResult(result, returnIntent);
        finish();
    }
}
