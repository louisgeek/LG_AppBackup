package com.louisgeek.appbackup;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ListView id_lv;
    private MyBaseAdapter myBaseAdapter;
    private AlertDialog mAlertDialog;
    private List<AppBean> mAppBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        id_lv = findViewById(R.id.id_lv);
        RadioGroup id_rg = findViewById(R.id.id_rg);
        id_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int nowType = AppBean.TYPE_USER;
                switch (checkedId) {
                    case R.id.id_rb_user:
                        nowType = AppBean.TYPE_USER;
                        break;
                    case R.id.id_rb_updated_sys:
                        nowType = AppBean.TYPE_UPDATED_SYSTEM;
                        break;
                    case R.id.id_rb_sys:
                        nowType = AppBean.TYPE_SYSTEM;
                        break;
                    default:
                }
                //筛选
                List<AppBean> appBeanList = new ArrayList<>();
                for (int i = 0; i < mAppBeanList.size(); i++) {
                    AppBean appBean = mAppBeanList.get(i);
                    if (appBean.type == nowType) {
                        appBeanList.add(appBean);
                    }
                }
                myBaseAdapter.refreshData(appBeanList);
            }
        });
        myBaseAdapter = new MyBaseAdapter(new ArrayList<AppBean>());
        id_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "长按备份", Toast.LENGTH_SHORT).show();
            }
        });
        id_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                backupApp(position);

                return true;
            }
        });
        id_lv.setAdapter(myBaseAdapter);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setPadding(0, 10, 0, 40);
        mAlertDialog = new AlertDialog.Builder(this).setMessage("正在加载").setView(progressBar).create();
        mAlertDialog.show();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadApkData();
            }
        });

    }

    private void backupApp(int position) {
        String path = myBaseAdapter.getItem(position).mPath;
        String packageName = myBaseAdapter.getItem(position).mPackageName;
        String versionName = myBaseAdapter.getItem(position).versionName;
        int versionCode = myBaseAdapter.getItem(position).versionCode;

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "sd卡未识别", Toast.LENGTH_SHORT).show();
            return;
        }
        //
        File formFile = new File(path);
        if (!formFile.exists()) {
            Toast.makeText(this, "app路径不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        String appBackupPath = Environment.getExternalStorageDirectory() + File.separator + "AppBackup" + File.separator;
        String toFileName = packageName + "_" + versionName + ".apk";
//        String toFileName = packageName + "_" + versionName + "_" + versionCode + ".apk";
        File toFilePath = new File(appBackupPath);
        if (!toFilePath.exists()) {
            boolean result = toFilePath.mkdirs();
            if (!result) {
                Toast.makeText(this, "创建文件夹失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        File toFile = new File(toFilePath, toFileName);
        if (toFile.exists()) {
            boolean result = toFile.delete();
            if (!result) {
                Toast.makeText(this, "删除文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            boolean result = toFile.createNewFile();
            if (!result) {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!toFile.exists()) {
            Toast.makeText(this, "目标文件生成失败", Toast.LENGTH_SHORT).show();
            return;
        }
        javaNioTransfer(formFile, toFile);
        Toast.makeText(this, "Apk已保存到\n" + toFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    private void javaNioTransfer(File source, File target) {

        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(in.position(), in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                in.close();
                outStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadApkData() {

        PackageManager packageManager = getPackageManager();
        List<PackageInfo> allPackageList = packageManager.getInstalledPackages(0);
        mAppBeanList = new ArrayList<>();
        for (int i = 0; i < allPackageList.size(); i++) {
            PackageInfo packageInfo = allPackageList.get(i);
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            Drawable iconDrawable = packageInfo.applicationInfo.loadIcon(packageManager);
            int type = AppBean.TYPE_USER;
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                // IS A SYSTEM APP
                type = AppBean.TYPE_SYSTEM;
            }
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                // APP WAS INSTALL AS AN UPDATE TO A BUILD-IN SYSTEM APP
                type = AppBean.TYPE_UPDATED_SYSTEM;
            }
            AppBean appBean = new AppBean();
            appBean.mName = appName;
            appBean.mPackageName = packageName;
            appBean.versionName = versionName;
            appBean.versionCode = versionCode;
            appBean.mPath = sourceDir;
            appBean.mDrawable = iconDrawable;
            appBean.type = type;
            mAppBeanList.add(appBean);
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myBaseAdapter.refreshData(mAppBeanList);
                mAlertDialog.dismiss();
            }
        });
    }
}
