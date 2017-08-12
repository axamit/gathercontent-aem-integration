package com.axamit.gc.core.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.services.GCContentApiImpl;
import com.axamit.gc.core.exception.GCException;
import org.junit.Before;
import org.junit.Test;

//! Useless test
public class GCconnectTest {

    @Before
    public void beforeRun() throws GCException {
        GCContext.build("", "");
        new GCContentApiImpl();
    }

    @Test
    public void testCreateItem() throws GCException {

    }

    @Test
    public void testApplyTemplate() throws GCException {

    }

    @Test
    public void testUpdateItem() throws GCException {

    }

    @Test
    public void createProjectTest() throws GCException {

    }

    @Test
    public void LargeContentLoadTest() throws GCException {

    }

    @Test
    public void fillLargeContentProject() throws GCException {

    }


}
