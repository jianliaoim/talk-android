package com.teambition.talk.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.UploadApi;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.jsbridge.BridgeHandler;
import com.teambition.talk.jsbridge.BridgeWebView;
import com.teambition.talk.jsbridge.CallBackFunction;
import com.teambition.talk.jsbridge.SimpleWebViewClientDelegate;
import com.teambition.talk.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.mime.TypedFile;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * Created by nlmartian on 8/21/15.
 */
public class WebContainerActivity extends BaseActivity {

    /**
     * 错误码：
     * 1 参数错误
     * 2 启动原生界面失败
     * 3 操作取消
     * 4 输入错误
     * 5 定位失败
     */

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private static final int REQUEST_CODE_SCAN_QRCODE = 1;

    private static final String HANDLER_DEVICE_VERSION = "device.version";
    private static final String HANDLER_CHAT_CHAT_PEOPLE = "biz.chat";
    private static final String HANDLER_CREATE_TOPIC = "biz.createTopic";
    private static final String HANDLER_UPLOAD_IMAGE = "biz.uploadImage";
    private static final String HANDLER_SCAN_QR = "biz.scanQrcode";
    private static final String HANDLER_GET_LOC = "biz.getLocation";
    private static final String HANDLER_GET_USERID = "biz.getUserId";

    @InjectView(R.id.webView)
    BridgeWebView bridgeWebView;
    @InjectView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private ProgressDialog progressDialog;
    private CallBackFunction uploadImageFunction;
    private CallBackFunction scanQRCodeFunction;
    private String url;
    private String title;

    public static Intent newIntent(Context context, String url, String title) {
        Intent intent = new Intent(context, WebContainerActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcontainer);
        ButterKnife.inject(this);
        url = getIntent().getStringExtra(EXTRA_URL);
        title = getIntent().getStringExtra(EXTRA_TITLE);

        handleTeambitionUrl(url);
        initWebView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web_container, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_forward: {
                Intent intent = new Intent(this, RepostAndShareActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clipData = new ClipData("url", new String[] {"text/plain"},
                            new ClipData.Item(bridgeWebView.getUrl()));
                    intent.setClipData(clipData);
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, bridgeWebView.getUrl());
                }
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_SEND);
                startActivity(intent);
                break;
            }
            case R.id.action_copy_link:
                ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cmb.setText(bridgeWebView.getUrl());
                Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_browser: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bridgeWebView.canGoBack()) {
                bridgeWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SelectImageActivity.SELECT_IMAGES: {
                if (resultCode == RESULT_OK) {
                    ArrayList<String> paths = new ArrayList<>();
                    paths.add(data.getStringExtra(SelectImageActivity.IMAGE_PATH));
                    uploadFiles(paths);
                } else {
                    if (uploadImageFunction != null) {
                        uploadImageFunction.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(3, "action cancel")));
                        uploadImageFunction = null;
                    }
                }
                break;
            }
            case REQUEST_CODE_SCAN_QRCODE: {
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra(ScannerActivity.SCAN_RESULT);
                    if (scanQRCodeFunction != null) {
                        scanQRCodeFunction.onCallBack(buildJavaCallbackData(true, result, null));
                    }
                } else {
                    if (scanQRCodeFunction != null) {
                        scanQRCodeFunction.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(3, "scan QR code error")));
                    }
                }
                scanQRCodeFunction = null;
                break;
            }
        }
    }

    private void initWebView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bridgeWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                getSupportActionBar().setTitle(title);
            }
        });

        bridgeWebView.setWebViewClientDelegate(new SimpleWebViewClientDelegate() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {
                    refreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(true);
                        }
                    });
                } else if (url.startsWith("tb-talk://team_invite")) {
                    // handle invitation url
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.setClass(WebContainerActivity.this, ChooseTeamActivity.class);
                    startActivity(intent);
                    finish();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        refreshLayout.setColorSchemeResources(R.color.talk_ocean, R.color.talk_grape,
                R.color.talk_mint, R.color.talk_yellow);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bridgeWebView.loadUrl(bridgeWebView.getUrl());
            }
        });

        bridgeWebView.loadUrl(url);
        registerHandlers();
    }

    private void registerHandlers() {
        bridgeWebView.registerHandler(HANDLER_DEVICE_VERSION, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                JSONObject resJson = new JSONObject();
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    resJson.put("success", true);
                    resJson.put("data", version);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                function.onCallBack(resJson.toString());
            }
        });

        bridgeWebView.registerHandler(HANDLER_CHAT_CHAT_PEOPLE, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent intent = new Intent(WebContainerActivity.this, ChatActivity.class);
                try {
                    JSONObject param = new JSONObject(data);
                    boolean isPrivate = param.optBoolean("isPrivate", true);
                    String targetId = param.optString("targetId");
                    if (isPrivate) {
                        Member member = MainApp.globalMembers.get(targetId);
                        if (member != null) {
                            if (BizLogic.isMe(targetId)) {
                                function.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(1, "cannot chat with yourself")));
                            } else {
                                intent.putExtra(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
                                startActivity(intent);
                                function.onCallBack(buildJavaCallbackData(true, null, null));
                            }
                        } else {
                            function.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(1, "params error")));
                        }
                    } else {
                        Room room = MainApp.globalRooms.get(targetId);
                        if (room != null) {
                            intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
                            startActivity(intent);
                            function.onCallBack(buildJavaCallbackData(true, null, null));
                        } else {
                            function.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(1, "params error")));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        bridgeWebView.registerHandler(HANDLER_CREATE_TOPIC, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    startActivity(new Intent(WebContainerActivity.this, AddGroupActivity.class));
                    function.onCallBack(buildJavaCallbackData(true, null, null));
                } catch (Exception e) {
                    function.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(2, "start fail")));
                }
            }
        });

        bridgeWebView.registerHandler(HANDLER_UPLOAD_IMAGE, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent intent = new Intent(WebContainerActivity.this, SelectImageActivity.class);
                startActivityForResult(intent, SelectImageActivity.SELECT_IMAGES);
                uploadImageFunction = function;
            }
        });

        bridgeWebView.registerHandler(HANDLER_SCAN_QR, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent intent = new Intent(WebContainerActivity.this, ScannerActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN_QRCODE);
                scanQRCodeFunction = function;
            }
        });

        bridgeWebView.registerHandler(HANDLER_GET_LOC, new BridgeHandler() {
            @Override
            public void handler(String data, final CallBackFunction function) {
                LocationClient locationClient = new LocationClient(getApplicationContext());
                locationClient.registerLocationListener(new BDLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation bdLocation) {
                        if (bdLocation != null) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("longitude", bdLocation.getLongitude());
                                jsonObject.put("latitude", bdLocation.getLatitude());
                                function.onCallBack(buildJavaCallbackData(true, jsonObject.toString(), null));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            function.onCallBack(buildJavaCallbackData(false, null, buildErrorJsonStr(5, "get location error")));
                        }
                    }
                });
                locationClient.start();
            }
        });

        bridgeWebView.registerHandler(HANDLER_GET_USERID, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                function.onCallBack(buildJavaCallbackData(true, BizLogic.getUserInfo().get_id(), null));
            }
        });
    }

    private String buildJavaCallbackData(boolean success, String data, String error) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("success", success);
            jsonObject.put("data", data);
            jsonObject.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String buildErrorJsonStr(int code, String msg) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
            jsonObject.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void uploadFiles(List<String> paths) {
        final ArrayList<FileUploadResponseData> fileResList = new ArrayList<>();
        final UploadApi uploadApi = TalkClient.getInstance().getUploadApi();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();
        Observable.from(paths)
                .map(new Func1<String, TypedFile>() {
                    @Override
                    public TypedFile call(String path) {
                        File originFile = new File(path);
                        TypedFile file = new TypedFile("image/*", originFile);
                        return file;
                    }
                }).flatMap(new Func1<TypedFile, Observable<FileUploadResponseData>>() {
            @Override
            public Observable<FileUploadResponseData> call(TypedFile typedFile) {
                return uploadApi.uploadFile(typedFile.fileName(), typedFile.mimeType(), typedFile.length(), typedFile);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FileUploadResponseData>() {
                    @Override
                    public void call(FileUploadResponseData fileUploadResponseData) {
                        fileResList.add(fileUploadResponseData);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        progressDialog.dismiss();
                        invokeUploadImageCallback(fileResList);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                        invokeUploadImageCallback(fileResList);
                    }
                });
    }

    private void invokeUploadImageCallback(ArrayList<FileUploadResponseData> fileResList) {
        String resFile = new GsonProvider.Builder().create().toJson(fileResList);
        if (uploadImageFunction != null) {
            uploadImageFunction.onCallBack(buildJavaCallbackData(true, resFile, null));
            uploadImageFunction = null;
        }
    }

    private void handleTeambitionUrl(String link) {
        String id;
        String[] itemNames = new String[]{"task", "event", "work", "post", "project"};
        for (String name : itemNames) {
            id = StringUtil.getIdInUrl(name, link);
            if (StringUtil.isNotBlank(id)) {
                goToTeambition(id, name);
                finish();
                return;
            }
        }
    }

    private void goToTeambition(String dataId, String type) {
        Intent intent = new Intent();
        String activityName = type;
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(String.format("teambition://%s?id=%s", activityName, dataId)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.app_not_installed, Toast.LENGTH_SHORT).show();
        } catch (SecurityException se) {
            Toast.makeText(this, R.string.tb_not_support, Toast.LENGTH_SHORT).show();
        }
    }
}

