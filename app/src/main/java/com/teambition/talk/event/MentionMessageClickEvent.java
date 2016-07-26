package com.teambition.talk.event;

import java.util.List;

/**
 * Created by nlmartian on 2/23/16.
 */
public class MentionMessageClickEvent {
    public List<String> mentions;
    public List<String> receiptors;

    public MentionMessageClickEvent(List<String> mentions, List<String> receiptors) {
        this.mentions = mentions;
        this.receiptors = receiptors;
    }
}
