package com.teambition.talk.entity;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alfa7055 on 14-11-2.
 */
@Parcel(Parcel.Serialization.BEAN)
public class File {
    public static final int DEFAULT_THUMBNAIL_SIZE = 400;

    private static final String SIZE_URL_PATTERN = "/w/(\\d+)/h/(\\d+)$";
    static Map<String, String> fileSchemeColors = new HashMap<>();
    String _id;
    String fileName;
    String fileKey;
    int fileSize;
    int imageWidth;
    int imageHeight;
    String fileType;
    String thumbnailUrl;
    String downloadUrl;
    String _teamId;
    String _creatorId;
    String _messageId;
    boolean isSpeech;
    int duration;
    String fileCategory;
    String text;
    int width;
    int height;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl == null) {
            return null;
        }
        if (thumbnailUrl.startsWith("file:///")) {
            return thumbnailUrl;
        }
        String result = thumbnailUrl;
        Pattern pattern = Pattern.compile(SIZE_URL_PATTERN);
        Matcher matcher = pattern.matcher(result);
        if (imageWidth <= 0) imageWidth = DEFAULT_THUMBNAIL_SIZE;
        if (imageHeight <= 0) imageHeight = DEFAULT_THUMBNAIL_SIZE;
        int thumbnailWidth = imageWidth > DEFAULT_THUMBNAIL_SIZE ? DEFAULT_THUMBNAIL_SIZE : imageWidth;
        int thumbnailHeight = thumbnailWidth * imageHeight / imageWidth;
        if (matcher.find()) {
            result = result.replaceFirst(matcher.group(), "/w/" + thumbnailWidth + "/h/" + thumbnailHeight);
        }
        return result;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String get_creatorId() {
        return _creatorId;
    }

    public void set_creatorId(String _creatorId) {
        this._creatorId = _creatorId;
    }

    public String get_messageId() {
        return _messageId;
    }

    public void set_messageId(String _messageId) {
        this._messageId = _messageId;
    }

    public boolean isSpeech() {
        return isSpeech;
    }

    public void setSpeech(boolean isSpeech) {
        this.isSpeech = isSpeech;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSchemeColor(final String fileScheme) {
        return fileSchemeColors.get(fileScheme) != null ? fileSchemeColors.get(fileScheme) : fileSchemeColors.get("file");
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    static {
        fileSchemeColors.put("psd", "#6E78BF");
        fileSchemeColors.put("txt", "#4AB6EC");
        fileSchemeColors.put("rtf", "#4AB6EC");
        fileSchemeColors.put("pdf", "#ED6455");
        fileSchemeColors.put("html", "#3A3A3A");
        fileSchemeColors.put("a", "#48ACA1");
        fileSchemeColors.put("v", "#6E78BF");
        fileSchemeColors.put("doc", "#4AB6EC");
        fileSchemeColors.put("xls", "#91C45B");
        fileSchemeColors.put("ppt", "#ED6455");
        fileSchemeColors.put("ai", "#EFB922");
        fileSchemeColors.put("css", "#EFB922");
        fileSchemeColors.put("js", "#3A3A3A");
        fileSchemeColors.put("zip", "#8965C1");
        fileSchemeColors.put("rar", "#8965C1");
        fileSchemeColors.put("apk", "#3A3A3A");
        fileSchemeColors.put("s", "#EFB922");
        fileSchemeColors.put("file", "#6E78BF");
    }
}
