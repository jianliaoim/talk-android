package com.teambition.talk.ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.View;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.FileDownloader;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Quote;
import com.teambition.talk.entity.RTF;
import com.teambition.talk.entity.Snippet;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.activity.AudioDetailActivity;
import com.teambition.talk.ui.activity.CodePreviewActivity;
import com.teambition.talk.ui.activity.RichContentActivity;
import com.teambition.talk.ui.activity.VideoActivity;
import com.teambition.talk.ui.activity.WebContainerActivity;
import com.teambition.talk.util.FileUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import org.parceler.Parcels;

import rx.functions.Action1;

/**
 * Created by zeatual on 15/8/19.
 */
public class OnMessageClickExecutor {

    private Context context;
    private Message message;

    public OnMessageClickExecutor(Context context, Message message) {
        this.context = context;
        this.message = message;
    }

    public void execute() {
        switch (MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode())) {
            case MESSAGE:
                onMessageClick(context, message);
                break;
            case IMAGE:
                onImageClick(context, message);
                break;
            case FILE:
                onFileClick(context, message);
                break;
            case SPEECH:
                onSpeechClick(context, message);
                break;
            case RTF:
                onRTFClick(context, message);
                break;
            case SNIPPET:
                onSnippetClick(context, message);
                break;
            case INTEGRATION:
                onIntegrationClick(context, message);
                break;
            case VIDEO:
                onVideoClick(context, message);
                break;
        }
    }

    public void onMessageClick(Context context, Message message) {

    }

    public void onImageClick(Context context, Message message) {

    }

    public void onFileClick(final Context context, Message message) {
        final File file = MessageDataProcess.getInstance().getFile(message);
        if (file != null) {
            new TalkDialog.Builder(context)
                    .title(R.string.download_file_title)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(R.color.colorPrimary)
                    .content(String.format(context.getString(R.string.download_file_message),
                            Formatter.formatFileSize(context, file.getFileSize())))
                    .positiveText(R.string.confirm)
                    .negativeColorRes(R.color.material_grey_700)
                    .negativeText(R.string.cancel)
                    .callback(new TalkDialog.ButtonCallback() {
                        @Override
                        public void onPositive(TalkDialog dialog, View v) {
                            final String path = FileDownloader.getDownloadPath(file.getFileName());
                            FileDownloader.getInstance().startDownload(file.getDownloadUrl(), path,
                                    new Action1<Integer>() {
                                        @Override
                                        public void call(Integer integer) {
                                            if (integer == FileDownloader.FINISH) {
                                                FileUtil.openFileByType(context, file.getFileType(),
                                                        new java.io.File(path));
                                            }
                                        }
                                    }, null);
                        }
                    }).show();
        }
    }

    public void onSpeechClick(Context context, Message message) {
        Intent intent = new Intent(context, AudioDetailActivity.class);
        intent.putExtra(AudioDetailActivity.EXTRA_MESSAGE, Parcels.wrap(message));
        context.startActivity(intent);
    }

    public void onRTFClick(Context context, Message message) {
        RTF rtf = MessageDataProcess.getInstance().getRTF(message);
        if (rtf != null) {
            Intent intentRTF = new Intent(context, RichContentActivity.class);
            intentRTF.putExtra("text", rtf.getText());
            intentRTF.putExtra("title", rtf.getTitle());
            context.startActivity(intentRTF);
        }
    }

    public void onSnippetClick(Context context, Message message) {
        Snippet snippet = MessageDataProcess.getInstance().getSnippet(message);
        if (snippet != null) {
            Intent intentSnippet = CodePreviewActivity.startIntent(context, snippet.getTitle(),
                    snippet.getText(), snippet.getCodeType());
            context.startActivity(intentSnippet);
        }
    }

    public void onIntegrationClick(Context context, Message message) {
        Quote quote = MessageDataProcess.getInstance().getQuote(message);
        if (quote != null) {
            if (StringUtil.isNotBlank(quote.getRedirectUrl())) {
                Intent intent = WebContainerActivity.newIntent(context, quote.getRedirectUrl(), quote.getTitle());
                context.startActivity(intent);
            } else {
                Intent intentUrl = new Intent(context, RichContentActivity.class);
                intentUrl.putExtra("text", quote.getText());
                intentUrl.putExtra("title", quote.getTitle());
                context.startActivity(intentUrl);
            }
        }
    }

    public void onVideoClick(Context context, Message message) {
        File file = MessageDataProcess.getInstance().getFile(message);
        if (file != null) {
            Intent intent = new Intent(context, VideoActivity.class);
            intent.putExtra(VideoActivity.VIDEO_PATH, file.getDownloadUrl());
            intent.putExtra(VideoActivity.DURATION, file.getDuration());
            intent.putExtra(VideoActivity.FILE_NAME, file.getFileName());
            context.startActivity(intent);
        }
    }

}
