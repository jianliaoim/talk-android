package com.teambition.talk.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nlmartian on 7/1/15.
 */
public class StringUtilTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetIdInUrl() throws Exception {
        String url = "http://project.ci/project/555ec3bfd9b233484f1b815a/files" +
                "/555ec3bfd9b233484f1b8160/work/55938772e95ce4b305ddb42a?utm_source=talk.ai";
        assertEquals("55938772e95ce4b305ddb42a", StringUtil.getIdInUrl("work", url));

        String url2 = "http://project.ci/project/555ec3bfd9b233484f1b815a/tasks" +
                "/scrum/555ec3bfd9b233484f1b8166/task/559380b30393e1d76df8eda0?utm_source=talk.ai";
        assertEquals("559380b30393e1d76df8eda0", StringUtil.getIdInUrl("task", url2));

        String url3 = "http://project.ci/project/555ec3bfd9b233484f1b815a/tasks" +
                "/scrum/555ec3bfd9b233484f1b8166/task/559380b30393e1d76df8eda0";
        assertEquals("559380b30393e1d76df8eda0", StringUtil.getIdInUrl("task", url3));

    }
}