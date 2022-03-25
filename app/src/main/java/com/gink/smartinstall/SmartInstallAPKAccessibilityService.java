package com.gink.smartinstall;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: wang-gk
 * @date:   2018/3/22 20:38.
 * @desc:   智能安装 APK 服务
 */
public class SmartInstallAPKAccessibilityService extends AccessibilityService {

    private static final String TAG = "[SmartInstallAPKAccessibilityService]";
    private Map<Integer, Boolean> handleMap = new HashMap<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: ");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: " + event);
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo != null) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                // todo this strategy is too violence
                boolean handled = iterateNodesAndHandle(nodeInfo);

//                if (handleMap.get(event.getWindowId()) == null) {
//                    if (handled) {
//                        handleMap.put(event.getWindowId(), true);
//                    }
//                }
//                if ((event.getContentChangeTypes() & AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) != 0) {
//                    String nodeCotent = nodeInfo.getText().toString();
//                    Log.d(TAG, "=== onAccessibilityEvent: " + nodeCotent + "  " + installNode);
//                    if ("android.widget.TextView".equals(nodeInfo.getClassName()) && "未发现风险".equals(nodeCotent)) {
//                        if (installNode != null) {
//                            installNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        }
//                    }
//                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: ");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    private AccessibilityNodeInfo installNode;

    //遍历节点，模拟点击安装按钮
    private boolean iterateNodesAndHandle(final AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            int childCount = nodeInfo.getChildCount();
            if ("android.widget.Button".equals(nodeInfo.getClassName())) {
                String nodeCotent = nodeInfo.getText().toString();
                Log.d(TAG, "content is: " + nodeCotent);
                if ("继续安装".equals(nodeCotent)
                        || "完成".equals(nodeCotent)
                        || "确定".equals(nodeCotent)
                        || "打开".equals(nodeCotent)) {
                    Log.d(TAG, "iterateNodesAndHandle: clicked!");
                    installNode = nodeInfo;
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
            //遇到ScrollView的时候模拟滑动一下
            else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (iterateNodesAndHandle(childNodeInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测辅助功能是否开启
     *
     * @param mContext
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" +
                SmartInstallAPKAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.d(TAG, " accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "---ACCESSIBILITY IS DISABLED--");
        }

        return false;
    }
}
