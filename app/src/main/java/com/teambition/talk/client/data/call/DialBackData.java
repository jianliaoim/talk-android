package com.teambition.talk.client.data.call;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by wlanjie on 15/9/12.
 */
@Root(name = "CallBack")
public class DialBackData {

    @Element(name = "from", required = false)
    private String from;
    @Element(name = "to", required = false)
    private String to;
    @Element(name = "customerSerNum", required = false)
    private String customerSerNum;
    @Element(name = "fromSerNum", required = false)
    private String fromSerNum;
    @Element(name = "promptTone", required = false)
    private String promptTone;
    @Element(name = "alwaysPlay", required = false)
    private String alwaysPlay;
    @Element(name = "terminalDtmf", required = false)
    private String terminalDtmf;
    @Element(name = "userData", required = false)
    private String userData;
    @Element(name = "maxCallTime", required = false)
    private String maxCallTime;
    @Element(name = "hangupCdrUrl", required = false)
    private String hangupCdrUrl;
    @Element(name = "needBothCdr", required = false)
    private String needBothCdr;
    @Element(name = "needRecord", required = false)
    private String needRecord;
    @Element(name = "countDownTime", required = false)
    private String countDownTime;
    @Element(name = "countDownPrompt", required = false)
    private String countDownPrompt;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCustomerSerNum() {
        return customerSerNum;
    }

    public void setCustomerSerNum(String customerSerNum) {
        this.customerSerNum = customerSerNum;
    }

    public String getFromSerNum() {
        return fromSerNum;
    }

    public void setFromSerNum(String fromSerNum) {
        this.fromSerNum = fromSerNum;
    }

    public String getPromptTone() {
        return promptTone;
    }

    public void setPromptTone(String promptTone) {
        this.promptTone = promptTone;
    }

    public String getAlwaysPlay() {
        return alwaysPlay;
    }

    public void setAlwaysPlay(String alwaysPlay) {
        this.alwaysPlay = alwaysPlay;
    }

    public String getTerminalDtmf() {
        return terminalDtmf;
    }

    public void setTerminalDtmf(String terminalDtmf) {
        this.terminalDtmf = terminalDtmf;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getMaxCallTime() {
        return maxCallTime;
    }

    public void setMaxCallTime(String maxCallTime) {
        this.maxCallTime = maxCallTime;
    }

    public String getHangupCdrUrl() {
        return hangupCdrUrl;
    }

    public void setHangupCdrUrl(String hangupCdrUrl) {
        this.hangupCdrUrl = hangupCdrUrl;
    }

    public String getNeedBothCdr() {
        return needBothCdr;
    }

    public void setNeedBothCdr(String needBothCdr) {
        this.needBothCdr = needBothCdr;
    }

    public String getNeedRecord() {
        return needRecord;
    }

    public void setNeedRecord(String needRecord) {
        this.needRecord = needRecord;
    }

    public String getCountDownTime() {
        return countDownTime;
    }

    public void setCountDownTime(String countDownTime) {
        this.countDownTime = countDownTime;
    }

    public String getCountDownPrompt() {
        return countDownPrompt;
    }

    public void setCountDownPrompt(String countDownPrompt) {
        this.countDownPrompt = countDownPrompt;
    }
}
