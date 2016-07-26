package com.teambition.talk.view;

/**
 * Created by zeatual on 14/10/27.
 */
public interface BaseView {

    /**
     * 显示ProgressDialog
     *
     * @param message 对话框消息
     */
    void showProgressDialog(int message);

    /**
     * 取消显示ProgressDialog
     */
    void dismissProgressDialog();

    /**
     * 显示ProgressBar
     */
    void showProgressBar();

    /**
     * 取消显示ProgressBar
     */
    void dismissProgressBar();

}
