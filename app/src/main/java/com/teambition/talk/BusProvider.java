package com.teambition.talk;

import com.squareup.otto.Bus;

/**
 * Created by jgzhu on 11/4/14.
 */
public class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {}
}
