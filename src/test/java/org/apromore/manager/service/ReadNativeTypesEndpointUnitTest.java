package org.apromore.manager.service;

import org.apromore.dao.model.NativeType;
import org.apromore.manager.ManagerPortalEndpoint;
import org.apromore.model.ObjectFactory;
import org.apromore.model.ReadNativeTypesInputMsgType;
import org.apromore.model.ReadNativeTypesOutputMsgType;
import org.apromore.service.*;
import org.apromore.service.helper.UIHelper;
import org.apromore.service.helper.UserInterfaceHelper;
import org.apromore.service.impl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Test the Manager Portal Endpoint WebService.
 */
public class ReadNativeTypesEndpointUnitTest {

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
    public void testInvokeReadNativeTypeSummaries() throws Exception {
        ReadNativeTypesInputMsgType msg = new ReadNativeTypesInputMsgType();
        msg.setEmpty("");
        JAXBElement<ReadNativeTypesInputMsgType> request = new ObjectFactory().createReadNativeTypesRequest(msg);

        List<NativeType> procSummary = new ArrayList<>();
        expect(frmSrv.findAllFormats()).andReturn(procSummary);

        replayAll();

        JAXBElement<ReadNativeTypesOutputMsgType> response = endpoint.readNativeTypes(request);
        Assert.assertNotNull(response.getValue().getResult());
        Assert.assertNotNull(response.getValue().getNativeTypes());
        Assert.assertEquals("Result Code Doesn't Match", response.getValue().getResult().getCode().intValue(), 0);
        Assert.assertEquals("nativeTypes should be empty", response.getValue().getNativeTypes().getNativeType().size(), 0);

        verifyAll();
    }

}
