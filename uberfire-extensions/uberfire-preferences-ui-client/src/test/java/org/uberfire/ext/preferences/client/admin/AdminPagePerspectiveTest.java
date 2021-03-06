/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.preferences.client.admin;

import org.junit.Before;
import org.junit.Test;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AdminPagePerspectiveTest {

    private AdminPagePerspective perspective;

    @Before
    public void setup() {
        perspective = spy(new AdminPagePerspective());
        doNothing().when(perspective).configurePerspective(any(PlaceRequest.class));
    }

    @Test
    public void getPerspectiveWithoutStartupTest() {
        final PerspectiveDefinition perspectiveDefinition = perspective.getPerspective();

        verify(perspective).createPerspectiveDefinition();
        verify(perspective,
               never()).configurePerspective(any(PlaceRequest.class));

        assertNotNull(perspectiveDefinition);
        assertNotNull(perspectiveDefinition.getName());
    }

    @Test
    public void getPerspectiveWithStartupTest() {
        this.perspective.onStartup(mock(PlaceRequest.class));

        verify(perspective,
               times(1)).createPerspectiveDefinition();
        verify(perspective,
               times(1)).configurePerspective(any(PlaceRequest.class));

        final PerspectiveDefinition perspectiveDefinition = perspective.getPerspective();

        verify(perspective,
               times(1)).createPerspectiveDefinition();
        verify(perspective,
               times(1)).configurePerspective(any(PlaceRequest.class));

        assertNotNull(perspectiveDefinition);
        assertNotNull(perspectiveDefinition.getName());
    }
}
