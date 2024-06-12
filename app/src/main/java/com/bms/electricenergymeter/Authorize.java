package com.bms.electricenergymeter;
import java.security.MessageDigest;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public class Authorize {

    private SharedPreferences sharedPreferences;
    private Context context;

    public Authorize(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
    }

    // 获取设备的 Android ID
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // 根据设备 ID 生成授权码
    public String generateAuthorizationCode(String deviceId) {

        // 使用哈希算法生成授权码
        return sha256(deviceId);
    }

    // 检查授权码是否有效
    public boolean isAuthorized(String deviceId,String authorizationCode) {
        String generatedCode = generateAuthorizationCode(deviceId);
        return generatedCode != null && generatedCode.equals(authorizationCode);
    }

    // 实现 SHA-256 哈希函数
    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // 将授权状态写入配置文件
    public void saveAuthorizationStatus(boolean isAuthorized) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("AuthorizationStatus", isAuthorized);
        editor.apply();
    }

    // 从配置文件读取授权状态
    public boolean readAuthorizationStatus() {
        return sharedPreferences.getBoolean("AuthorizationStatus", false);
    }
}
