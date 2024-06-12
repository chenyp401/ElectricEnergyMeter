package com.bms.electricenergymeter;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

public class AuthorizationActivity extends DialogFragment {

    public boolean  isAuthorize;
    Button btnAuthroize;
    Button btnCancel;
    EditText etDeviceId;
    EditText etCode;
    Authorize authorize;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 使用自定义布局文件
        View view = inflater.inflate(R.layout.activity_authorization, container, false);
        authorize=new Authorize(this.getContext());

//        if (authorize.readAuthorizationStatus())
//        {
//            return null;
//        }
        // 配置按钮的点击事件
        btnAuthroize = view.findViewById(R.id.btnAuthorize);
        btnCancel=view.findViewById(R.id.btnCancelAuthorize);
        etDeviceId=view.findViewById(R.id.etDeviceId);
        etCode=view.findViewById(R.id.etAuthorCode);
        String deviceId=authorize.getDeviceId().trim();
        etDeviceId.setText(deviceId);
        btnAuthroize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code=etCode.getText().toString().trim();
                if (authorize.isAuthorized(code))
                {
                    isAuthorize=true;
                    authorize.saveAuthorizationStatus(true);

                }
                else
                {
                    isAuthorize=false;
                    authorize.saveAuthorizationStatus(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("警告");
                    builder.setMessage("注册码不正确！");
                    builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理"是"按钮点击事件
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理"是"按钮点击事件
                            dialog.dismiss();
                            finishAffinity(getActivity()); // 结束当前Activity以及所有父Activity，适用于API 16及以上版本
                            System.exit(0); // 如果你需要立即终止进程
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
        return view;
    }

    // 对话框创建时设置样式，使其成为模态
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
    }
}
