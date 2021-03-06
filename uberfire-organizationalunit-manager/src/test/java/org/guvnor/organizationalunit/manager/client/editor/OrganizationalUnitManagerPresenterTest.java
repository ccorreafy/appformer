/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.organizationalunit.manager.client.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.common.services.project.client.context.WorkspaceProjectContext;
import org.guvnor.organizationalunit.manager.client.editor.popups.AddOrganizationalUnitPopup;
import org.guvnor.organizationalunit.manager.client.editor.popups.EditOrganizationalUnitPopup;
import org.guvnor.structure.client.security.OrganizationalUnitController;
import org.guvnor.structure.events.AfterCreateOrganizationalUnitEvent;
import org.guvnor.structure.events.AfterDeleteOrganizationalUnitEvent;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.common.client.api.Caller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class OrganizationalUnitManagerPresenterTest {

    @GwtMock
    private AddOrganizationalUnitPopup addOrganizationalUnitPopup;

    @GwtMock
    private EditOrganizationalUnitPopup editOrganizationalUnitPopup;

    @Mock
    private EventSourceMock<AfterCreateOrganizationalUnitEvent> createOUEvent;

    @Mock
    private EventSourceMock<AfterDeleteOrganizationalUnitEvent> deleteOUEvent;

    @Mock
    private OrganizationalUnitController organizationalUnitController;

    @Mock
    private OrganizationalUnit organizationalUnitA;

    private OrganizationalUnitManagerView view = mock(OrganizationalUnitManagerView.class);
    private OrganizationalUnitService mockOUService = mock(OrganizationalUnitService.class);

    private Caller<OrganizationalUnitService> organizationalUnitService = new CallerMock<>(mockOUService);
    private RepositoryService mockRepositoryService = mock(RepositoryService.class);

    private Caller<RepositoryService> repositoryService = new CallerMock<>(mockRepositoryService);

    private OrganizationalUnitManagerPresenterImpl presenter;

    private OrganizationalUnit mockOU = mock(OrganizationalUnit.class);

    @Mock
    private WorkspaceProjectContext projContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        presenter = new OrganizationalUnitManagerPresenterImpl(view,
                                                               organizationalUnitService,
                                                               repositoryService,
                                                               organizationalUnitController,
                                                               addOrganizationalUnitPopup,
                                                               editOrganizationalUnitPopup,
                                                               createOUEvent,
                                                               deleteOUEvent,
                                                               projContext);

        when(mockOU.getName()).thenReturn("mock");
        when(mockOU.getOwner()).thenReturn("mock");
        when(mockOU.getDefaultGroupId()).thenReturn("mock");

        when(mockOUService.getOrganizationalUnits()).thenReturn(new ArrayList<OrganizationalUnit>());
        when(mockOUService.getOrganizationalUnit(anyString())).thenReturn(organizationalUnitA);

        when(mockOUService.createOrganizationalUnit(any(String.class),
                                                    any(String.class),
                                                    any(String.class),
                                                    any(Collection.class))).thenReturn(mockOU);

        when(organizationalUnitController.canCreateOrgUnits()).thenReturn(false);
        when(organizationalUnitController.canUpdateOrgUnit(organizationalUnitA)).thenReturn(true);
        when(organizationalUnitController.canDeleteOrgUnit(organizationalUnitA)).thenReturn(true);
        when(organizationalUnitA.getRepositories()).thenReturn(Collections.EMPTY_LIST);

        when(projContext.getActiveOrganizationalUnit()).thenReturn(Optional.of(mockOU));
    }

    @Test
    public void testOnStartup() {
        presenter.onStartup();

        // Called directly on startup
        verify(view).setAddOrganizationalUnitEnabled(false);
        verify(view).setDeleteOrganizationalUnitEnabled(false);
        verify(view).setEditOrganizationalUnitEnabled(false);

        // Called in remote callback after loading repos
        verify(view).setDeleteOrganizationalUnitEnabled(true);
        verify(view).setEditOrganizationalUnitEnabled(true);
    }

    @Test
    public void testSelectOrgUnit() {
        assertPrecondition("startup", () -> testOnStartup());
        presenter.organizationalUnitSelected(organizationalUnitA);

        // Called once in startup
        verify(view, times(2)).setDeleteOrganizationalUnitEnabled(true);
        verify(view, times(2)).setEditOrganizationalUnitEnabled(true);
    }

    @Test
    public void testCreateOUEvent() {
        assertPrecondition("startup", () -> testOnStartup());
        presenter.createNewOrganizationalUnit(mockOU.getName(),
                                              mockOU.getOwner(),
                                              mockOU.getDefaultGroupId());

        verify(createOUEvent,
               times(1)).fire(any(AfterCreateOrganizationalUnitEvent.class));
    }

    @Test
    public void testDeleteOUEvent() {
        assertPrecondition("startup", () -> testOnStartup());

        presenter.deleteOrganizationalUnit(mockOU);

        verify(deleteOUEvent,
               times(1)).fire(any(AfterDeleteOrganizationalUnitEvent.class));
    }

    private void assertPrecondition(String name, Runnable assertions) {
        try {
            assertions.run();
        }
        catch (AssertionError ae) {
            throw new AssertionError("Precondition failed: " + name, ae);
        }
    }
}
