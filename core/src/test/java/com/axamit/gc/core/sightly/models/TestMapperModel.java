/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.services.GCConfiguration;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Simple JUnit test verifying the HelloWorldModel
 * @author Axamit, gc.support@axamit.com
 */
public class TestMapperModel {

    //@Inject
    private MapperModel mapper;
    

    @Before
    public void setup() throws Exception {
        Resource resource = mock(Resource.class);
        GCContext gcContext =  mock(GCContext.class);
        GCConfiguration gcConfiguration = mock(GCConfiguration.class);

        when(gcConfiguration.getGCContext(resource)).thenReturn(gcContext);
        mapper = new MapperModel(resource);
        PrivateAccessor.setField(mapper, "gcConfiguration", gcConfiguration);
        mapper.init();
    }
    

}
