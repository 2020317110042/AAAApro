package org.apromore.manager.service;

import org.apromore.canoniser.exception.CanoniserException;
import org.apromore.exception.ImportException;
import org.apromore.manager.ManagerPortalEndpoint;
import org.apromore.service.*;
import org.apromore.service.helper.UIHelper;
import org.apromore.service.helper.UserInterfaceHelper;
import org.apromore.service.impl.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.powermock.api.easymock.PowerMock.createMock;

public class ImportProcessEndpointUnitTest {

    private ManagerPortalEndpoint endpoint;

    private DeploymentService deploymentService;
    private CanoniserService canoniserService;
    private PluginService pluginService;
    private FragmentService fragmentSrv;
    private ProcessService procSrv;
    private ClusterService clusterService;
    private FormatService frmSrv;
    private DomainService domSrv;
    private UserService userSrv;
    private SimilarityService simSrv;
    private MergeService merSrv;
    private SecurityService secSrv;
    private WorkspaceService wrkSrv;
    private UserInterfaceHelper uiHelper;
    private ChangePropagationService changePropagationService;

    @Before
    public void setUp() throws Exception {
        deploymentService = createMock(DeploymentServiceImpl.class);
        pluginService = createMock(PluginServiceImpl.class);
        fragmentSrv = createMock(FragmentServiceImpl.class);
        canoniserService = createMock(CanoniserServiceImpl.class);
        procSrv = createMock(ProcessServiceImpl.class);
        clusterService = createMock(ClusterServiceImpl.class);
        frmSrv = createMock(FormatServiceImpl.class);
        domSrv = createMock(DomainServiceImpl.class);
        userSrv = createMock(UserServiceImpl.class);
        simSrv = createMock(SimilarityServiceImpl.class);
        merSrv = createMock(MergeServiceImpl.class);
        secSrv = createMock(SecurityServiceImpl.class);
        wrkSrv = createMock(WorkspaceServiceImpl.class);
        uiHelper = createMock(UIHelper.class);
        changePropagationService = createMock(ChangePropagationServiceImpl.class);

        endpoint = new ManagerPortalEndpoint(deploymentService, pluginService, fragmentSrv, canoniserService, procSrv,
                clusterService, frmSrv, domSrv, userSrv, simSrv, merSrv, secSrv, wrkSrv, uiHelper, changePropagationService);
    }



    @Test
    public void testImportProcess() throws ImportException, IOException, CanoniserException {
//        ImportProcessInputMsgType msg = new ImportProcessInputMsgType();
//        EditSessionType edit = new EditSessionType();
//        edit.setAnnotation("test");
//        edit.setCreationDate("");
//        edit.setDomain("");
//        edit.setLastUpdate("");
//        edit.setNativeType("EPML 2.0");
//        edit.setProcessId(12143);
//        edit.setProcessName("test");
//        edit.setUsername("test");
//        edit.setVersionName("1.0");
//        edit.setWithAnnotation(true);
//        msg.setEditSession(edit);
//        PluginParameters properties = new PluginParameters();
//        PluginParameter property = new PluginParameter();
//        property.setId("test");
//        property.setClazz("java.lang.String");
//        property.setName("test");
//        property.setValue("");
//        properties.getParameter().add(property);
//        msg.setCanoniserParameters(properties);
//        DataHandler nativeXml = new DataHandler(TestData.EPML, "text/xml");
//        msg.setProcessDescription(nativeXml);
//        JAXBElement<ImportProcessInputMsgType> request = new ObjectFactory().createImportProcessRequest(msg);
//
//        CanonisedProcess cp = new CanonisedProcess();
//        ArrayList<PluginMessage> pluginMsg = new ArrayList<PluginMessage>();
//        pluginMsg.add(new PluginMessageImpl("test"));
//        cp.setMessages(pluginMsg);
//        expect(canoniserService.canonise(eq(edit.getNativeType()), anyObject(InputStream.class), anyObject(java.util.Set.class))).andReturn(cp);
//
//        ProcessModelVersion procVersion = new ProcessModelVersion();
//        ProcessSummaryType procSummary = new ProcessSummaryType();
//        expect(procSrv.importProcess(eq(edit.getUsername()), eq(edit.getUsername()), anyObject(String.class), eq(edit.getVersionName()),
//                eq(edit.getNativeType()), eq(cp), anyObject(InputStream.class), eq(edit.getDomain()), anyObject(String.class),
//                eq(edit.getCreationDate()), eq(edit.getLastUpdate()))).andReturn(procVersion);
//        replayAll();
//
//        JAXBElement<ImportProcessOutputMsgType> response = endpoint.importProcess(request);
//
//        Assert.assertNotNull(response.getValue().getResult());
////        Assert.assertNotNull(response.getValue().getImportProcessResult());
////        Assert.assertEquals("Result Code Doesn't Match", response.getValue().getResult().getCode().intValue(), 0);
//
//        Assert.assertNotNull(response.getValue().getImportProcessResult().getMessage());
//        Assert.assertEquals(response.getValue().getImportProcessResult().getMessage().getMessage().get(0).getValue(), "test");
////        Assert.assertNotNull(response.getValue().getImportProcessResult().getProcessSummary());
//
//        verifyAll();
    }

}
