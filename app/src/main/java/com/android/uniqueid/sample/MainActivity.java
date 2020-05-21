package com.android.uniqueid.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.uniqueid.AndroidUniqueId;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @Check This <a href="http://www.msa-alliance.cn/col.jsp?id=120">http://www.msa-alliance.cn/col.jsp?id=120</a>
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View permission = findViewById(R.id.permission);
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                if (checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                    Toast.makeText(context, "READ_PHONE_STATE is allowed", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 0x7);
                    }
                }
            }
        });


        final TextView unique = findViewById(R.id.unique);
        View load = findViewById(R.id.load);

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long start = SystemClock.elapsedRealtime();
                /*
                  onOAID or onResult
                  Just different code encapsulation
                 */
                AndroidUniqueId.getInstance().getIds(getApplicationContext(), new AndroidUniqueId.OnUniqueIdCallback() {

                    @Override
                    public void onOAID(String oaid) {
                        //empty
                        // Just different code encapsulation.
                        // we want show all ids.
                    }

                    @Override
                    public void onResult(JSONObject result) {
                        long end = SystemClock.elapsedRealtime();
                        StringBuilder builder;
                        try {
                            builder = new StringBuilder();
                            builder.append("cost:").append(end - start).append("ms").append("\n")
                                    .append(result.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            builder = new StringBuilder();
                            builder.append("cost:").append(end - start).append("ms").append("\n")
                                    .append(result.toString());
                        }
                        unique.setText(builder);
                    }
                });


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x7) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                AndroidUniqueId.getInstance().refreshIds(getApplicationContext());
            }
        }
    }

    public static boolean checkSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
