package com.teambition.talk.event;

import com.teambition.talk.entity.Invitation;

/**
 * Created by nlmartian on 1/29/16.
 */
public class NewInvitationEvent {
    Invitation invitation;

    public NewInvitationEvent(Invitation invitation) {
        this.invitation = invitation;
    }
}
