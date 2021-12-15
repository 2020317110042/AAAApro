package org.apromore.service.impl;

import org.apromore.common.Constants;
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.dao.*;
import org.apromore.dao.model.*;
import org.apromore.exception.ExceptionMergeProcess;
import org.apromore.exception.ImportException;
import org.apromore.exception.SerializationException;
import org.apromore.helper.Version;
import org.apromore.model.ParameterType;
import org.apromore.model.ParametersType;
import org.apromore.model.ProcessVersionIdType;
import org.apromore.model.ProcessVersionIdsType;
import org.apromore.service.CanoniserService;
import org.apromore.service.MergeService;
import org.apromore.service.ProcessService;
import org.apromore.service.model.CanonisedProcess;
import org.apromore.service.model.MergeData;
import org.apromore.service.model.ToolboxData;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.tools.MergeProcesses;
import org.apromore.toolbox.similaritySearch.tools.MergeProcesses_Result_DO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of the MergeService Contract.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @author <a herf="mailto:fzwbmw@gmail.com">Zaiwen FENG</>
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = true, rollbackFor = Exception.class)
public class MergeServiceImpl implements MergeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeServiceImpl.class);

    private ProcessModelVersionRepository processModelVersionRepo;
    private CanoniserService canoniserSrv;
    private ProcessService processSrv;
    private NodeRepository nodeSrv;
    private EdgeRepository edgeSrv;

    /*add NodeMappingGandCGRepository to the implementation of merge service*/
    private NodeMappingGandCGRepository nodeMappingGandCGRepo;

    /*add EdgeMappingGandCGRepository to the implementation of merge service*/
    private EdgeMappingGandCGRepository edgeMappingGandCGRepo;

    /**
     * Default Constructor allowing Spring to Autowire for testing and normal use.
     *
     * @param processModelVersionRepository Annotation Repository.
     * @param canoniserService              Canoniser Service.
     * @param processService                Native Type repository.
     */
    @Inject
    public MergeServiceImpl(final ProcessModelVersionRepository processModelVersionRepository, final CanoniserService canoniserService,
                            final ProcessService processService, final NodeMappingGandCGRepository nodeMappingGandCGRepository, final EdgeMappingGandCGRepository edgeMappingGandCGRepository
            , final NodeRepository nodeRepository, final EdgeRepository edgeRepository) {
        processModelVersionRepo = processModelVersionRepository;
        canoniserSrv = canoniserService;
        processSrv = processService;
        nodeMappingGandCGRepo = nodeMappingGandCGRepository;
        edgeMappingGandCGRepo = edgeMappingGandCGRepository;
        nodeSrv = nodeRepository;
        edgeSrv = edgeRepository;
    }


    /**
     * @see org.apromore.service.MergeService#mergeProcesses(String, String, String, String, String, Integer, org.apromore.model.ParametersType, org.apromore.model.ProcessVersionIdsType, boolean)
     *      {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = false)
    public ProcessModelVersion mergeProcesses(String processName, String version, String domain, String username, String algo, Integer folderId,
                                              ParametersType parameters, ProcessVersionIdsType ids, final boolean makePublic) throws ExceptionMergeProcess {
        List<ProcessModelVersion> models = new ArrayList<>();
        for (ProcessVersionIdType cpf : ids.getProcessVersionId()) {
            models.add(processModelVersionRepo.getProcessModelVersion(cpf.getProcessId(), cpf.getBranchName(), cpf.getVersionNumber()));
        }

        ProcessModelVersion pmv = null;
        try {
            ToolboxData data = convertModelsToCPT(models);
            data = getParametersForMerge(data, algo, parameters);

            MergeProcesses_Result_DO mpr = performMerge(data);

            CanonisedProcess cp = new CanonisedProcess();
            cp.setCpt(mpr.getCanonicalProcessType());
            cp.setCpf(new ByteArrayInputStream(canoniserSrv.CPFtoString(cp.getCpt()).getBytes()));

            SimpleDateFormat sf = new SimpleDateFormat(Constants.DATE_FORMAT);
            String created = sf.format(new Date());

            // This fails as we need to specify a native type and pass in the model.
            Version importVersion = new Version(1, 0);
            pmv = processSrv.importProcess(username, folderId, processName, importVersion, null, cp, domain, "", created, created, makePublic);

            String pid_cg = pmv.getId().toString();

            /*declare an array list for storing vertex mapping*/
            List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();
            vertexPairFromG2CGs = mpr.getVertexPairFromG2CGs();

            /*declare an array list for storing edge mapping*/
            List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();
            edgePairFromG2CGs = mpr.getEdgePairFromG2CGs();

            //       List<NodeMappingGandCG> nodeMappingGandCGs = new ArrayList<NodeMappingGandCG>();

        /*get an iterator*/
            Iterator<VertexPairFromG2CG> ite = vertexPairFromG2CGs.iterator();
            int i = 60000;
        /*fill Node Mapping beans and persist them into data base*/
            while(ite.hasNext()){
                VertexPairFromG2CG vpg2cg = ite.next();

                NodeMappingGandCG np = new NodeMappingGandCG();

                np.setId(new Integer(i));

                /*UPDATE NODE ID IN G INFORMATION: here need to transform the left_g_v_id (canonical node id) to 'node id' in table of 'node'*
                * however, what we need to do is just remove the prefix 'n' of the canonical node id.
                * The exception is that, when node is 'null', we do not need to remove the prefix 'n'*/
                if(vpg2cg.getLeft_G_v_id().equals("null")){

                    np.setId_G_n(new String("null"));
                }else {
                    np.setId_G_n(vpg2cg.getLeft_G_v_id().substring(1));
                }


                np.setPID_G(vpg2cg.getPid_G());
                //np.setId_CG_n(vpg2cg.getRight_CG_v_id());
                /*UPDATE NODE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical node id) to 'node id' in table of 'node'**/
                String uri_CG_n = vpg2cg.getRight_CG_v_id();
                           /*get root_fragment_version of merged model by pmv*/
                FragmentVersion fv = pmv.getRootFragmentVersion();
           /*get root_fragment_version_id from root_fragment_version*/
                Integer fv_id = fv.getId();
                Node node = nodeSrv.findNodeByUriAndFragmentVersion(uri_CG_n, fv_id);
                Integer node_id = node.getId();
                //**save node_id into the bean**//
                np.setId_CG_n(node_id.toString());

                np.setPID_CG(pid_cg);
             /*persistence of node mapping data between variants and merged model*/
                nodeMappingGandCGRepo.save(np);
                i++;
            }

                /*get an iterator of 'EdgePairFromG2CG' and then loop it*/
            Iterator<EdgePairFromG2CG> ite_edge = edgePairFromG2CGs.iterator();
            int j = 60000;
        /*fill Edge Mapping beans and persist them into data base*/
            while(ite_edge.hasNext()){
                EdgePairFromG2CG epg2cg = ite_edge.next();
                List<String> right_CG_p_e_id = new ArrayList<String>();
                right_CG_p_e_id = epg2cg.getRight_CG_p_e_id();

                if(right_CG_p_e_id.size() == 1){

                    EdgeMappingGandCG ep = new EdgeMappingGandCG();

                    ep.setId(new Integer(j));

                           /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_e_id (canonical edge id) to 'edge id' in table of 'edge'*
                * however, what we need to do is just remove the prefix 'e' of the canonical edge id*/
                    ep.setId_G_e(epg2cg.getLeft_G_e_id().substring(1));

                    ep.setPID_G(epg2cg.getPid_G());
 //                   ep.setId_CG_e(epg2cg.getRight_CG_p_e_id().get(0));

                    /*UPDATE EDGE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                    String originalId_CG_e = epg2cg.getRight_CG_p_e_id().get(0);
                           /*get root_fragment_version of merged model by pmv*/
                    FragmentVersion fv = pmv.getRootFragmentVersion();
           /*get root_fragment_version_id from root_fragment_version*/
                    Integer fv_id = fv.getId();
                    Edge edge = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(originalId_CG_e, fv_id);
                    Integer edge_id = edge.getId();
                    //**save edge_id into the bean**//
                    ep.setId_CG_e(edge_id.toString());

                    ep.setPID_CG(pid_cg);
                    ep.setPart_of_mapping(false);
                    edgeMappingGandCGRepo.save(ep);
                    j++;
                }else{
                            /*if the size of 'right_cg_p_e_id' is more than one, get the iterator for 'right_cg_p_e_id' and then loop it*/
                    Iterator<String> it = right_CG_p_e_id.iterator();

                    while (it.hasNext()){

                        String right_CG_e_id = it.next();

                        EdgeMappingGandCG ep = new EdgeMappingGandCG();
                        ep.setId(new Integer(j));
                                                   /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_e_id (canonical edge id) to 'edge id' in table of 'edge'*
                * however, what we need to do is just remove the prefix 'e' of the canonical edge id*/
                        ep.setId_G_e(epg2cg.getLeft_G_e_id().substring(1));

                        ep.setPID_G(epg2cg.getPid_G());
                        //ep.setId_CG_e(right_CG_e_id);

                        /*UPDATE EDGE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                                             /*get root_fragment_version of merged model by pmv*/
                        FragmentVersion fv = pmv.getRootFragmentVersion();
           /*get root_fragment_version_id from root_fragment_version*/
                        Integer fv_id = fv.getId();
                        Edge edge = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(right_CG_e_id, fv_id);
                        Integer edge_id = edge.getId();
                        //**save edge_id into the bean**//
                        ep.setId_CG_e(edge_id.toString());


                        ep.setPID_CG(pid_cg);
                        ep.setPart_of_mapping(true);

                /*persistence of node mapping data between variants and merged model*/
                        edgeMappingGandCGRepo.save(ep);
                        j++;
                    }
                }
            }

        /*test if it's successful for saving nodeMapping*/
//        NodeMappingGandCG nodeMappingGandCG1 = new NodeMappingGandCG();
//        nodeMappingGandCG1.setId(new Integer("200"));
//        nodeMappingGandCG1.setPID_G(new String("635"));
//        nodeMappingGandCG1.setPID_CG(new String("636"));
//        nodeMappingGandCG1.setId_G_n(new String("637"));
//        nodeMappingGandCG1.setId_CG_n(new String("638"));
//        nodeMappingGandCGRepo.save(nodeMappingGandCG1);
//        NodeMappingGandCG nodeMappingGandCG2 = new NodeMappingGandCG();
//        nodeMappingGandCG2.setId(new Integer("201"));
//        nodeMappingGandCG2.setPID_G(new String("735"));
//        nodeMappingGandCG2.setPID_CG(new String("736"));
//        nodeMappingGandCG2.setId_G_n(new String("737"));
//        nodeMappingGandCG2.setId_CG_n(new String("738"));
//        nodeMappingGandCGRepo.save(nodeMappingGandCG2);
//        NodeMappingGandCG nodeMappingGandCG3 = new NodeMappingGandCG();
//        nodeMappingGandCG3.setId(new Integer("202"));
//        nodeMappingGandCG3.setPID_G(new String("835"));
//        nodeMappingGandCG3.setPID_CG(new String("836"));
//        nodeMappingGandCG3.setId_G_n(new String("837"));
//        nodeMappingGandCG3.setId_CG_n(new String("838"));
//        nodeMappingGandCGs.add(nodeMappingGandCG1);
//        nodeMappingGandCGs.add(nodeMappingGandCG2);
//        nodeMappingGandCGs.add(nodeMappingGandCG3);
//        nodeMappingGandCGRepo.save(nodeMappingGandCG3);
            //       nodeMappingGandCGRepo.save(nodeMappingGandCGs);
        } catch (SerializationException se) {
            LOGGER.error("Failed to convert the models into the Canonical Format.", se);
        } catch (ImportException | JAXBException ie) {
            LOGGER.error("Failed Import the newly merged model.", ie);
        }

        return pmv;
    }


    /* Responsible for getting all the Models and converting them to CPT internal format */
    private ToolboxData convertModelsToCPT(List<ProcessModelVersion> models) throws SerializationException {
        ToolboxData data = new ToolboxData();

        for (ProcessModelVersion pmv : models) {
            data.addModel(pmv, processSrv.getCanonicalFormat(pmv));
        }

        return data;
    }


    /* Loads the Parameters used for the Merge */
    private ToolboxData getParametersForMerge(ToolboxData data, String method, ParametersType params) {
        data.setAlgorithm(method);

        for (ParameterType p : params.getParameter()) {
            if (ToolboxData.MODEL_THRESHOLD.equals(p.getName())) {
                data.setModelthreshold(p.getValue());
            } else if (ToolboxData.LABEL_THRESHOLD.equals(p.getName())) {
                data.setLabelthreshold(p.getValue());
            } else if (ToolboxData.CONTEXT_THRESHOLD.equals(p.getName())) {
                data.setContextthreshold(p.getValue());
            } else if (ToolboxData.SKIP_N_WEIGHT.equals(p.getName())) {
                data.setSkipnweight(p.getValue());
            } else if (ToolboxData.SUB_N_WEIGHT.equals(p.getName())) {
                data.setSubnweight(p.getValue());
            } else if (ToolboxData.SKIP_E_WEIGHT.equals(p.getName())) {
                data.setSkipeweight(p.getValue());
            } else if (ToolboxData.REMOVE_ENT.equals(p.getName())) {
                data.setRemoveEntanglements(p.getValue() == 1);
            }
        }

        return data;
    }


    /* Does the merge. */
    private MergeProcesses_Result_DO performMerge(ToolboxData data) {

        //ArrayList<CanonicalProcessType> canonicalProcesses = new ArrayList<>(data.getModel().values());
        //ArrayList<ProcessModelVersion> processModelVersions = new ArrayList<>(data.getModel().keySet());
        Map<ProcessModelVersion, CanonicalProcessType> original_models = new HashMap<>(data.getModel());
        ArrayList<MergeData> models = new ArrayList<MergeData>();

                /**Loop*/
       for(Map.Entry<ProcessModelVersion, CanonicalProcessType> entry : original_models.entrySet()){

            ProcessModelVersion pmv = entry.getKey();
            CanonicalProcessType cpt = entry.getValue();
            Integer pmv_id = pmv.getId();

           /*get root_fragment_version by pmv*/
            FragmentVersion fv = pmv.getRootFragmentVersion();
           /*get root_fragment_version_uri from root_fragment_version*/
            String fv_uri = fv.getUri();

           /*get node set in a fragment version*/
            List<Node> nodes = nodeSrv.getNodesByFragmentURI(fv_uri);
           /*set up a hash map to record the mapping between node_id_in_canonical (uri) and node id in NODE TABLE*/
            HashMap<String, String> node_id_mapping = new HashMap<String, String>();
            Iterator<Node> ite = nodes.iterator();/*get the iterator*/
           while(ite.hasNext()){

               Node node = ite.next();
               node_id_mapping.put(node.getUri(), "n" + node.getId().toString());
           }

           /*get edge set in a fragment version*/
            List<Edge> edges = edgeSrv.getEdgesByFragmentURI(fv_uri);
           /*set up a hash map to record the mapping between edge_id_in_canonical (uri) and edge id in EDGE TABLE*/
            HashMap<String, String> edge_id_mapping = new HashMap<String, String>();
            Iterator<Edge> ite_edge = edges.iterator();/*get the iterator*/
           while (ite_edge.hasNext()){

               Edge edge = ite_edge.next();
               edge_id_mapping.put(edge.getOriginalId(), "e" + edge.getId());
           }

            MergeData model = new MergeData();
            model.setModel_id(pmv_id);
            model.setNode_id_mapping(node_id_mapping);
            model.setEdge_id_mapping(edge_id_mapping);
            model.setCanonicalProcessType(cpt);

           models.add(model);

        }

        MergeProcesses_Result_DO mergeProcessesResultDo = MergeProcesses.mergeProcesses(models, data.isRemoveEntanglements(), data.getAlgorithm(),
                data.getModelthreshold(), data.getLabelthreshold(), data.getContextthreshold(), data.getSkipnweight(),
                data.getSubnweight(), data.getSkipeweight());


        return mergeProcessesResultDo;
    }

//    public List<String> getNodesIds(String processModelVersionId){
//        List<String> nodesIds = new ArrayList<String>();
//
//        return  nodesIds;
//    }
//
//    public List<String> getEdgesIds (String processModelVersionId){
//        List<String> edgeIds = new ArrayList<String>();
//
//        return edgeIds;
//    }
}
