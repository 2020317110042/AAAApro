package org.apromore.manager.service;

import org.apromore.manager.ManagerPortalEndpoint;
import org.apromore.model.ObjectFactory;
import org.apromore.model.ReadDomainsInputMsgType;
import org.apromore.model.ReadDomainsOutputMsgType;
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
 * Test the Read User method on the Manager Portal Endpoint WebService.
 */
public class ReadDomainsEndpointUnitTest {

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
    public void testInvokeReadDomains() throws Exception {
        ReadDomainsInputMsgType msg = new ReadDomainsInputMsgType();
        msg.setEmpty("");
        JAXBElement<ReadDomainsInputMsgType> request = new ObjectFactory().createReadDomainsRequest(msg);

        List<String> domains = new ArrayList<String>();
        expect(domSrv.findAllDomains()).andReturn(domains);

        replay(domSrv);

        JAXBElement<ReadDomainsOutputMsgType> response = endpoint.readDomains(request);
        Assert.assertNotNull(response.getValue().getResult());
        Assert.assertNotNull(response.getValue().getDomains());
        Assert.assertEquals("Result Code Doesn't Match", response.getValue().getResult().getCode().intValue(), 0);
        Assert.assertEquals("UserNames should be empty", response.getValue().getDomains().getDomain().size(), 0);

        verify(domSrv);
    }

}

