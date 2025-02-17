package io.dcvz.rnzendesk;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import zendesk.configurations.Configuration;
import zendesk.core.PushRegistrationProvider;
import zendesk.core.Zendesk;
import zendesk.core.Identity;
import zendesk.core.JwtIdentity;
import zendesk.core.AnonymousIdentity;
import zendesk.support.CustomField;
import zendesk.support.Support;
import zendesk.support.guide.HelpCenterActivity;
import zendesk.support.request.RequestActivity;
import zendesk.support.request.RequestConfiguration;
import zendesk.support.requestlist.RequestListActivity;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import java.util.ArrayList;

public class RNZendeskBridge extends ReactContextBaseJavaModule {

    public RNZendeskBridge(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNZendesk";
    }

    // MARK: - Initialization

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void initialize(ReadableMap config) {
        String appId = config.getString("appId");
        String zendeskUrl = config.getString("zendeskUrl");
        String clientId = config.getString("clientId");
        Zendesk.INSTANCE.init(getReactApplicationContext(), zendeskUrl, appId, clientId);
        Support.INSTANCE.init(Zendesk.INSTANCE);
    }

    @ReactMethod
    public void registerPushToken(String token) {
        Log.d("TAG", token);
        PushRegistrationProvider provider = Zendesk.INSTANCE.provider().pushRegistrationProvider();
        provider.registerWithDeviceIdentifier(token, new ZendeskCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("TAG", "Successfully registered device");
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Log.d("TAG","Couldn't register device");
            }
        });
    }

    // MARK: - Indentification

    @ReactMethod
    public void identifyJWT(String token) {
        JwtIdentity identity = new JwtIdentity(token);
        Zendesk.INSTANCE.setIdentity(identity);
    }

    @ReactMethod
    public void identifyAnonymous(String name, String email) {
        Identity identity = new AnonymousIdentity.Builder()
            .withNameIdentifier(name)
            .withEmailIdentifier(email)
            .build();

        Zendesk.INSTANCE.setIdentity(identity);
    }

    // MARK: - UI Methods

    @ReactMethod
    public void showHelpCenter(ReadableMap options) {
        Boolean hideContact = options.getBoolean("hideContactUs") || false;
        Configuration hcConfig = HelpCenterActivity.builder()
                .withContactUsButtonVisible(!(options.hasKey("hideContactSupport") && options.getBoolean("hideContactSupport")))
                .config();

        Intent intent = HelpCenterActivity.builder()
                .withContactUsButtonVisible(true)
                .intent(getReactApplicationContext(), hcConfig);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void showNewTicket(ReadableMap options) {
        ArrayList tags = options.getArray("tags").toArrayList();
        ArrayList customList = new ArrayList();

        if(options.hasKey("custom_fields")){
            ReadableArray customFields = options.getArray("custom_fields");
            for (int i = 0; i < customFields.size(); i++) {
                ReadableMap field = customFields.getMap(i);
                CustomField customField = new CustomField((long) (field.getDouble("fieldId")), field.getString("value"));
                customList.add(customField);
            }
        }

        Intent intent = RequestActivity.builder()
                .withTags(tags)
                .withCustomFields(customList)
                .intent(getReactApplicationContext());

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void showTicketList() {
        Intent intent = RequestListActivity.builder()
                .intent(getReactApplicationContext());

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void showSpecificTicket(String request_id) {
        Intent intent = new RequestConfiguration.Builder()
                .withRequestId(request_id)
                .intent(getReactApplicationContext());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }
}
