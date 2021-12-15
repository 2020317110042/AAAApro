package org.apromore.manager.service;

import org.apromore.anf.AnnotationsType;
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.exception.LockFailedException;
import org.apromore.exception.SerializationException;
import org.apromore.manager.ManagerPortalEndpoint;
import org.apromore.model.DeployProcessInputMsgType;
import org.apromore.model.DeployProcessOutputMsgType;
import org.apromore.model.ObjectFactory;
import org.apromore.model.PluginParameters;
import org.apromore.plugin.deployment.exception.DeploymentException;
import org.apromore.plugin.message.PluginMessage;
import org.apromore.service.*;
import org.apromore.service.helper.UIHelper;
import org.apromore.service.helper.UserInterfaceHelper;
import org.apromore.service.impl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.*;

public class DeployProcessEndpointUnitTest {

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
    public void testDeployProces() throws DeploymentException, SerializationException, LockFailedException {
        DeployProcessInputMsgType msg = new DeployProcessInputMsgType();
        JAXBElement<DeployProcessInputMsgType> request = new ObjectFactory().createDeployProcessRequest(msg );

        String branchName = "test";
        request.getValue().setBranchName(branchName);
        String processName = "test";
        request.getValue().setProcessName(processName);
        String versionName = "1.0";
        request.getValue().setVersionName(versionName);
        String nativeType = "Test 2.1";
        request.getValue().setNativeType(nativeType);
        PluginParameters pluginProperties = new PluginParameters();
        request.getValue().setDeploymentParameters(pluginProperties);

        expect(procSrv.getCurrentProcessModel(processName, branchName, false)).andReturn(new CanonicalProcessType());
        expect(deploymentService.deployProcess(eq(nativeType), anyObject(CanonicalProcessType.class), isNull(AnnotationsType.class),
                anyObject(HashSet.class))).andReturn(new ArrayList<PluginMessage>());

        replayAll();

        JAXBElement<DeployProcessOutputMsgType> result = endpoint.deployProcess(request);

        Assert.assertNotNull(result.getValue().getResult());
        Assert.assertEquals("Result Code Doesn't Match", result.getValue().getResult().getCode().intValue(), 0);

        assertNotNull(result.getValue().getMessage());

        verifyAll();
    }

}
