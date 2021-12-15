package org.apromore.service.impl;

import org.apromore.cpf.CanonicalProcessType;
import org.apromore.dao.*;
import org.apromore.dao.model.*;
import org.apromore.exception.ExceptionChangePropagation;
import org.apromore.exception.ImportException;
import org.apromore.exception.RepositoryException;
import org.apromore.exception.SerializationException;
import org.apromore.helper.Version;
import org.apromore.model.ProcessVersionIdType;
import org.apromore.service.CanoniserService;
import org.apromore.service.ChangePropagationService;
import org.apromore.service.ProcessService;
import org.apromore.service.SecurityService;
import org.apromore.service.model.CanonisedProcess;
import org.apromore.service.model.MergeData;
import org.apromore.service.model.ToolboxData;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.tools.MergeProcesses_Result_DO;
import org.apromore.toolbox.similaritySearch.tools.PropagateChanges;
import org.apromore.toolbox.similaritySearch.tools.PropagateChangesCG2G_Result_DO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by fengz2 on 1/10/2014.
 */
@Service
public class ChangePropagationServiceImpl implements ChangePropagationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePropagationServiceImpl.class);

    private ProcessModelVersionRepository processModelVersionRepo;
    private CanoniserService canoniserSrv;
    private ProcessService processSrv;
    private NodeRepository nodeSrv;
    private EdgeRepository edgeSrv;
    private NodeMappingGandCGRepository nodeMappingGandCGRepo;
    private EdgeMappingGandCGRepository edgeMappingGandCGRepo;
    private ProcessRepository processRepo;
    private SecurityService securitySrv;
    private ProcessBranchRepository processBranchRepository;

    private List<ProcessModelVersion> models = new ArrayList<ProcessModelVersion>();


    /*Default Constructor allowing Spring to Autowire for testing and normal use*/
    /**
     * @param processModelVersionRepository*
     * @param canoniserService
     * @param processService
     * @param  nodeRepository
     * @param  edgeRepository
     * @param nodeMappingGandCGRepository
     * @param edgeMappingGandCGRepository*/

    @Inject
    public ChangePropagationServiceImpl(final ProcessModelVersionRepository processModelVersionRepository, final CanoniserService canoniserService, final ProcessService processService,
                                        final NodeMappingGandCGRepository nodeMappingGandCGRepository, final EdgeMappingGandCGRepository edgeMappingGandCGRepository, final NodeRepository nodeRepository,
                                        final EdgeRepository edgeRepository, final ProcessRepository processRepository, final SecurityService securityService, final ProcessBranchRepository branchRepository){

        processModelVersionRepo = processModelVersionRepository;
        canoniserSrv = canoniserService;
        processSrv = processService;
        nodeMappingGandCGRepo = nodeMappingGandCGRepository;
        edgeMappingGandCGRepo = edgeMappingGandCGRepository;
        nodeSrv = nodeRepository;
        edgeSrv = edgeRepository;
        processRepo = processRepository;
        securitySrv = securityService;
        processBranchRepository = branchRepository;
    }




    public List<ProcessModelVersion> changePropagationFromGtoCG(String username, ProcessVersionIdType originalVariantVersion,
                                                          ProcessVersionIdType changedVariantVersion, String newBranchName,
                                                          Version newVersionForMergedModel,NativeType natType) throws ExceptionChangePropagation {
        List<ProcessModelVersion> changedMergedModelsVersion = new ArrayList<ProcessModelVersion>();

        ProcessModelVersion model_original_variant = processModelVersionRepo.getProcessModelVersion(originalVariantVersion.getProcessId(), originalVariantVersion.getBranchName(),
                originalVariantVersion.getVersionNumber());

        ProcessModelVersion model_changed_variant = processModelVersionRepo.getProcessModelVersion(changedVariantVersion.getProcessId(), changedVariantVersion.getBranchName(),
                changedVariantVersion.getVersionNumber());

        //here, we should get the pmv list of the original merged model, which should be changed , based on the pmv of the original process variant.
        String pmv_id_original_variant = model_original_variant.getId().toString();
        List<String> pmv_ids_original_merged = nodeMappingGandCGRepo.findMergedModelIdByVariantId(pmv_id_original_variant);
        List<ProcessModelVersion> models_original_merged = new ArrayList<ProcessModelVersion>();
        for(String pmv_id_original_merged : pmv_ids_original_merged){

            models_original_merged.add(processModelVersionRepo.getProcessModelVersionById(Integer.parseInt(pmv_id_original_merged)));
        }


        //loop the list of the original merged model version, for each, update the process
        for(ProcessModelVersion model_original_merged : models_original_merged){
            //clear the 'models'
            models.clear();

            models.add(model_original_variant);
            models.add(model_changed_variant);
            models.add(model_original_merged); //note that 'models' does imply the sequence of models, 1. original variant; 2. changed variant; 3. original merged

            ProcessModelVersion pmv = null;

            try{

                ToolboxData data = convertModelsToCPT(models);

                //*get the pmv id of the original model variant*//
                Integer pmv_id_model_original_variant = model_original_variant.getId();

                //*get the pmv id of the original merged model*//
                Integer pmv_id_model_original_merged = model_original_merged.getId();

                //get the process name of the original merged model
                Integer process_BranchId_original_merged = model_original_merged.getProcessBranch().getId();
                Integer processId_original_merged = processBranchRepository.FindProcessByBranchId(process_BranchId_original_merged).getId();
                String name_original_merged = processRepo.findUniqueById(processId_original_merged).getName();
//            String name_original_merged = "process100";

                //get the branch name of the original merged model
                String branch_name_original_merged = model_original_merged.getProcessBranch().getBranchName();

                //create the version number of the original merged model
                String version_number_original_merged = model_original_merged.getVersionNumber();
                Version originalVersion = new Version(version_number_original_merged);

                //get the new version number of process to be evolved.
//            Version newVersion = new Version(newVersionNumber);

                //create user who updates the process
                User user = securitySrv.getUserByName(username);

                //get the lock status for the original merged process
                String lockStatus = model_original_merged.getLockStatus().toString();

                //get the native type of the original merged model
                //           NativeType nativeType = model_original_merged.getNativeType();

                List<NodeMappingGandCG> nodeMappingGandCGs;

            /*get the original node mappings between the original process variant G and the original merged model CG*/
                nodeMappingGandCGs=
                        nodeMappingGandCGRepo.findNodeMappingBetweenGAndCGByVariantIdAndMergedId(pmv_id_model_original_variant.toString(), pmv_id_model_original_merged.toString());

                List<EdgeMappingGandCG> edgeMappingGandCGs;

            /*get the original edge mapping between the original process variant G and the original merged model CG*/
                edgeMappingGandCGs = edgeMappingGandCGRepo.findEdgeMappingBetweenGAndCGByVariantIdAndMergedId(pmv_id_model_original_variant.toString(), pmv_id_model_original_merged.toString());


            /*invoke, and get the evolved process cpf*/
                MergeProcesses_Result_DO changedMergedCanonicalModel = performChangePropagation(data, nodeMappingGandCGs, edgeMappingGandCGs);

                CanonisedProcess cp = new CanonisedProcess();
                cp.setCpt(changedMergedCanonicalModel.getCanonicalProcessType());
                cp.setCpf(new ByteArrayInputStream(canoniserSrv.CPFtoString(cp.getCpt()).getBytes()));

                //update the process targeted to be evolved.
                pmv = processSrv.updateProcess(processId_original_merged, name_original_merged, branch_name_original_merged, newBranchName, newVersionForMergedModel,
                        originalVersion, user, lockStatus, natType, cp);

                String pid_cg = pmv.getId().toString();

                String pid_g = model_changed_variant.getId().toString();

                            /*declare an array list for storing vertex mapping*/
                List<VertexPairFromG2CG> vertexPairFromG2CGs = changedMergedCanonicalModel.getVertexPairFromG2CGs();

        /*declare an array list for storing edge mapping*/
                List<EdgePairFromG2CG> edgePairFromG2CGs = changedMergedCanonicalModel.getEdgePairFromG2CGs();

                    /*get an iterator*/
                Iterator<VertexPairFromG2CG> ite = vertexPairFromG2CGs.iterator();
                int i = 60000;
        /*fill Node Mapping beans and persist them into data base*/
                while(ite.hasNext()){
                    VertexPairFromG2CG vpg2cg = ite.next();

                    NodeMappingGandCG np = new NodeMappingGandCG();

                    np.setId(new Integer(i));

                /*UPDATE NODE IN IN G INFORMATION: here need to transform the left_g_v_id (canonical node id) in cpf to 'node id' in table of 'node'*/
                    String uri_G_n = vpg2cg.getLeft_G_v_id();
                    FragmentVersion fv_g = model_changed_variant.getRootFragmentVersion();
                    Integer fv_g_id = fv_g.getId();
                    Node node_in_g  = nodeSrv.findNodeByUriAndFragmentVersion(uri_G_n, fv_g_id);
                    Integer node_in_g_id = node_in_g.getId();
                    np.setId_G_n(node_in_g_id.toString());

                    np.setPID_G(pid_g);

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

                    /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                        String canonicalId_G_e=epg2cg.getLeft_G_e_id();
                        FragmentVersion fv_g = model_changed_variant.getRootFragmentVersion();
                        Integer fv_g_id = fv_g.getId();
                        Edge edge_in_g = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(canonicalId_G_e, fv_g_id);
                        Integer edge_id_in_g = edge_in_g.getId();
                        ep.setId_G_e(edge_id_in_g.toString());

                        ep.setPID_G(pid_g);
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

                        /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                            String canonicalId_G_e=epg2cg.getLeft_G_e_id();
                            FragmentVersion fv_g = model_changed_variant.getRootFragmentVersion();
                            Integer fv_g_id = fv_g.getId();
                            Edge edge_in_g = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(canonicalId_G_e, fv_g_id);
                            Integer edge_id_in_g = edge_in_g.getId();
                            ep.setId_G_e(edge_id_in_g.toString());

                            ep.setPID_G(pid_g);
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





            }catch (SerializationException se){

                LOGGER.error("Failed to convert the models into the Canonical Format, should be checked about it!!!23 Jan 2016", se);
            }catch (ImportException | JAXBException ie) {
                LOGGER.error("Failed Import the newly changed merged model after change propagation.", ie);
            }catch (RepositoryException re){

                LOGGER.error("Data base related errors." , re);
            }

            changedMergedModelsVersion.add(pmv);

        }

        return changedMergedModelsVersion;

    }

    /*service implementation that propagates changes from CG to G(s)*/
    public List<ProcessModelVersion> changePropagationFromCGtoG(String username, ProcessVersionIdType originalMergedVersion,
                                                         ProcessVersionIdType changedMergedVersion, String updated_variant_branch_name,
                                                         Version updated_variant_version, NativeType natType) throws ExceptionChangePropagation{

        List<ProcessModelVersion> changedVariantsVersion = new ArrayList<ProcessModelVersion>();

        ProcessModelVersion model_original_merged = processModelVersionRepo.getProcessModelVersion(originalMergedVersion.getProcessId(), originalMergedVersion.getBranchName(),
                originalMergedVersion.getVersionNumber());

        ProcessModelVersion model_changed_merged = processModelVersionRepo.getProcessModelVersion(changedMergedVersion.getProcessId(), changedMergedVersion.getBranchName(),
                changedMergedVersion.getVersionNumber());

        //here, we should get the pmv list of the original process variants, based on the pmv of the original merged process.
        String pmv_id_original_merged = model_original_merged.getId().toString();
        List<String> pmv_ids_original_variants = nodeMappingGandCGRepo.findVariantsIdByMergedId(pmv_id_original_merged);

        //remove 'null' element
        Iterator<String> iterator = pmv_ids_original_variants.iterator();
        while(iterator.hasNext()){

            String pmv_id_original_variants = iterator.next();
            if(pmv_id_original_variants.equals("null")){

                iterator.remove();
            }
        }

        List<ProcessModelVersion> models_original_variants = new ArrayList<ProcessModelVersion>();
        for(String pmv_id_original_variant : pmv_ids_original_variants){

            models_original_variants.add(processModelVersionRepo.getProcessModelVersionById(Integer.parseInt(pmv_id_original_variant)));
        }

            //clear the 'models'
        models.clear();

        models.add(model_original_merged);
        models.add(model_changed_merged);
        models.addAll(models_original_variants); //note that 'models' does imply the sequence of models, 1. original merged; 2. changed merged; 3. original variants;

        ProcessModelVersion pmv = null;

        try{

            LinkedHashMap<ProcessModelVersion,CanonicalProcessType> data = convertModelsToCPT_from_CG_to_G(models);

                //*get the pmv id of the original merged model*//
            Integer pmv_id_model_original_merged = model_original_merged.getId();

            /*create the list to save the original node mappings between the original merged model CG and all the process variants*/
            List<NodeMappingGandCG> nodeMappingGandCGs = new ArrayList<NodeMappingGandCG>();

            /*create the list to save the original edge mappings between the original merged model CG and all the process variants*/
            List<EdgeMappingGandCG> edgeMappingGandCGs = new ArrayList<EdgeMappingGandCG>();

            //loop the original process variant pmv id
            for(String pmv_id_original_variant : pmv_ids_original_variants){

                List<NodeMappingGandCG> nodeMappingGandCG;

                /*get the original node mappings between the original process variant G and the original merged model CG*/
                nodeMappingGandCG=
                        nodeMappingGandCGRepo.findNodeMappingBetweenGAndCGByVariantIdAndMergedId(pmv_id_original_variant.toString(), pmv_id_model_original_merged.toString());

                List<EdgeMappingGandCG> edgeMappingGandCG;

                /*get the original edge mapping between the original process variant G and the original merged model CG*/
                edgeMappingGandCG = edgeMappingGandCGRepo.findEdgeMappingBetweenGAndCGByVariantIdAndMergedId(pmv_id_original_variant.toString(),   pmv_id_model_original_merged.toString());

                nodeMappingGandCGs.addAll(nodeMappingGandCG);

                edgeMappingGandCGs.addAll(edgeMappingGandCG);
            }

            /*Here adding node mapping relations between G and CG when node is auxiliary node */
            List<NodeMappingGandCG> nodeMappingGandCGs_aux = nodeMappingGandCGRepo.findNodeMappingBetweenGAndCGByVariantIdAndMergedId(
                    new String("null"), pmv_id_model_original_merged.toString()
            );

            nodeMappingGandCGs.addAll(nodeMappingGandCGs_aux);

            /*invoke, and get the cpf of the evolved process variants, conducting actual propagation, the results are in the sequence*/
            PropagateChangesCG2G_Result_DO changed_variants_cpf = performChangePropagationFromCGToG(data, nodeMappingGandCGs, edgeMappingGandCGs);

            Map<String, CanonicalProcessType> changed_variants_cpt = changed_variants_cpf.getCanonicalProcessTypes();

            /*create a hash map, so that <K,V> K:pmv of the original variant, V: canonical process type of the corresponding changed variant to K*/
            HashMap<ProcessModelVersion, CanonicalProcessType> changed_variants_cpt_with_original_pmv = new HashMap<ProcessModelVersion, CanonicalProcessType>();

            /*loop the map 'changed_variants_cpt'*/
            Set<String> keySet = changed_variants_cpt.keySet();
            Iterator<String> keySetIterator = keySet.iterator();
            while(keySetIterator.hasNext()){

                String models_original_variants_id = keySetIterator.next();
                /*Here, we should find the original process variant from the list 'models_original_variants',
                which is corresponding to 'original_pmv_id'*/
                Iterator<ProcessModelVersion> models_original_variants_to_be_changed_it = models_original_variants.iterator();
                while(models_original_variants_to_be_changed_it.hasNext()){

                    ProcessModelVersion model_original_variant = models_original_variants_to_be_changed_it.next();

                    if(model_original_variant.getId().toString().equals(models_original_variants_id)){

                        changed_variants_cpt_with_original_pmv.put(model_original_variant,changed_variants_cpt.get(models_original_variants_id));
                    }


                }


            }

            /*Obtain all the vertex mappings between all the changed variants and merged graph (the id here is still canonical)*/
            List<VertexPairFromG2CG> vertexPairFromG2CGs = changed_variants_cpf.getVertexPairFromG2CGs();

            /*Obtain all the edge mappings between all the changed variants and merged graph (the id here is still canonical )*/
            List<EdgePairFromG2CG> edgePairFromG2CGs = changed_variants_cpf.getEdgePairFromG2CGs();


            /**Take the actual UPDATE for all the process variants**/
            Iterator it = changed_variants_cpt_with_original_pmv.entrySet().iterator();

            /*for each variant of CG, create the process model version and edge mapping and node mapping (with database id) to merged model ,modified on the 7th of May*/
            while(it.hasNext()){

                Map.Entry pair = (Map.Entry)it.next();

                ProcessModelVersion original_variant_pmv = (ProcessModelVersion)pair.getKey();

                CanonicalProcessType changed_variant_cpt = (CanonicalProcessType)pair.getValue();

                //*get the pmv of the original model variant, we need it to find the related edge & vertex mappings*//
                String original_variant_pmv_id = original_variant_pmv.getId().toString();

                List<VertexPairFromG2CG> vertexPairFromG2CGs_for_specified_variant = new ArrayList<VertexPairFromG2CG>();

                List<EdgePairFromG2CG> edgePairFromG2CGs_for_specified_variant = new ArrayList<EdgePairFromG2CG>();

                Iterator<VertexPairFromG2CG> iterator1 = vertexPairFromG2CGs.iterator();
                while(iterator1.hasNext()){

                    VertexPairFromG2CG vertexPairFromG2CG = iterator1.next();

                    if(vertexPairFromG2CG.getPid_G().equals(original_variant_pmv_id)){

                        vertexPairFromG2CGs_for_specified_variant.add(vertexPairFromG2CG);
                    }
                }

                Iterator<EdgePairFromG2CG> iterator2 = edgePairFromG2CGs.iterator();
                while(iterator2.hasNext()){

                    EdgePairFromG2CG edgePairFromG2CG = iterator2.next();

                    if(edgePairFromG2CG.getPid_G().equals(original_variant_pmv_id)){

                        edgePairFromG2CGs_for_specified_variant.add(edgePairFromG2CG);
                    }
                }

                /*get the process name of the original process variant*/
                Integer process_BranchId_original_variant = original_variant_pmv.getProcessBranch().getId();
                Integer processId_original_variant = processBranchRepository.FindProcessByBranchId(process_BranchId_original_variant).getId();
                String original_variant_name = processRepo.findUniqueById(processId_original_variant).getName();

                /*get the branch name of the original process variant*/
                String original_variant_branch_name = original_variant_pmv.getProcessBranch().getBranchName();

                /*create the version number of the original process variant*/
                String original_variant_version_number = original_variant_pmv.getVersionNumber();
                Version original_variant_version = new Version(original_variant_version_number);

                //create user who updates the process
                User user = securitySrv.getUserByName(username);

                //get the lock status for the original process variant
                String lockStatus = original_variant_pmv.getLockStatus().toString();

                CanonisedProcess cp = new CanonisedProcess();
                cp.setCpt(changed_variant_cpt);
                String cpf_str = canoniserSrv.CPFtoString(cp.getCpt());
                cp.setCpf(new ByteArrayInputStream(cpf_str.getBytes()));


                /*update the process variant in the database.  *NOTE:*'updated_variant_branch_name' is the new branch for the variant,*/
                /*'updated_variant_version' is the new version of the variant*/
                pmv = processSrv.updateProcess(processId_original_variant, original_variant_name, original_variant_branch_name,  updated_variant_branch_name,
                        updated_variant_version, original_variant_version, user, lockStatus, natType, cp);

                /**ADD the node mappings between the changed merged graph and the changed variant*/
                String pid_g = pmv.getId().toString();//get the pid of the variant

                String pid_cg = model_changed_merged.getId().toString();//get the pid of the merged model

                /*get an iterator*/
                Iterator<VertexPairFromG2CG> ite = vertexPairFromG2CGs_for_specified_variant.iterator();
                int i = 60000;

                 /*fill Node Mapping beans and persist them into data base according to the information saved in 'VertexPairFromG2CG'*/
                while(ite.hasNext()){

                    VertexPairFromG2CG vpg2cg = ite.next();
                    /*UPDATE NODE IN IN G INFORMATION: here need to transform the left_g_v_id (canonical node id) in cpf to 'node id' in table of 'node'*/
                    String uri_G_n = vpg2cg.getLeft_G_v_id();
                    FragmentVersion fv_g = pmv.getRootFragmentVersion();
                    Integer fv_g_id = fv_g.getId();
                    Node node_in_g  = nodeSrv.findNodeByUriAndFragmentVersion(uri_G_n, fv_g_id);
                    /*if node can not be found in this variant, continuing next loop*/
                    if(node_in_g == null){

                        continue;

                    }
                    /*if node of this variant is found, create the node mapping bean*/
                    else{
                        NodeMappingGandCG np = new NodeMappingGandCG();

                        np.setId(new Integer(i));

                        Integer node_in_g_id = node_in_g.getId();

                        np.setId_G_n(node_in_g_id.toString());

                        np.setPID_G(pid_g);
                        //np.setId_CG_n(vpg2cg.getRight_CG_v_id());
                        /*UPDATE NODE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical node id) to 'node id' in table of 'node'**/
                        String uri_CG_n = vpg2cg.getRight_CG_v_id();
                           /*get root_fragment_version of merged model by 'model_changed_merged'*/
                        FragmentVersion fv = model_changed_merged.getRootFragmentVersion();
                        /*get root_fragment_version_id from root_fragment_version*/
                        Integer fv_id = fv.getId();
                        Node node = nodeSrv.findNodeByUriAndFragmentVersion(uri_CG_n, fv_id);
                        Integer node_id = node.getId();
                        //**save node_id into the bean**//
                        np.setId_CG_n(node_id.toString());

                        np.setPID_CG(pid_cg);
                        /*persistence of node mapping data between variants and merged model*/
                        nodeMappingGandCGRepo.save(np);

                    }

                    i++;
                }//end node mapping



                /*get an iterator of 'EdgePairFromG2CG' and then loop it*/
                Iterator<EdgePairFromG2CG> ite_edge = edgePairFromG2CGs_for_specified_variant.iterator();
                int j = 60000;
                /*fill Edge Mapping beans and persist them into data base*/
                while(ite_edge.hasNext()){
                    EdgePairFromG2CG epg2cg = ite_edge.next();
                    List<String> right_CG_p_e_id = new ArrayList<String>();
                    right_CG_p_e_id = epg2cg.getRight_CG_p_e_id();

                    if(right_CG_p_e_id.size() == 1){

                        EdgeMappingGandCG ep = new EdgeMappingGandCG();
                        ep.setId(new Integer(j));

                        /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                        String canonicalId_G_e=epg2cg.getLeft_G_e_id();
                        FragmentVersion fv_g = pmv.getRootFragmentVersion();
                        Integer fv_g_id = fv_g.getId();
                        Edge edge_in_g = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(canonicalId_G_e, fv_g_id);

                        if(edge_in_g == null){

                            continue;

                        }else{
                            Integer edge_id_in_g = edge_in_g.getId();
                            ep.setId_G_e(edge_id_in_g.toString());

                            ep.setPID_G(pid_g);

                            /*UPDATE EDGE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                            String originalId_CG_e = epg2cg.getRight_CG_p_e_id().get(0);
                           /*get root_fragment_version of merged model by pmv*/
                            FragmentVersion fv = model_changed_merged.getRootFragmentVersion();
                            /*get root_fragment_version_id from root_fragment_version*/
                            Integer fv_id = fv.getId();
                            Edge edge = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(originalId_CG_e, fv_id);
                            Integer edge_id = edge.getId();
                            //**save edge_id into the bean**//
                            ep.setId_CG_e(edge_id.toString());

                            ep.setPID_CG(pid_cg);
                            ep.setPart_of_mapping(false);
                            edgeMappingGandCGRepo.save(ep);

                        }

                        j++;
                    }else{
                        /*if the size of 'right_cg_p_e_id' is more than one, get the iterator for 'right_cg_p_e_id' and then loop it*/
                        Iterator<String> it_path = right_CG_p_e_id.iterator();

                        while (it_path.hasNext()){

                            String right_CG_e_id = it_path.next();

                            EdgeMappingGandCG ep = new EdgeMappingGandCG();

                            ep.setId(new Integer(j));

                            /*UPDATE EDGE ID IN G INFORMATION: here need to transform the left_g_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                            String canonicalId_G_e=epg2cg.getLeft_G_e_id();

                            FragmentVersion fv_g = pmv.getRootFragmentVersion();
                            Integer fv_g_id = fv_g.getId();
                            Edge edge_in_g = edgeSrv.findEdgeByOriginalIdAndFragmentVersion(canonicalId_G_e, fv_g_id);

                            if(edge_in_g == null){

                                continue;
                            }else{
                                Integer edge_id_in_g = edge_in_g.getId();
                                ep.setId_G_e(edge_id_in_g.toString());

                                ep.setPID_G(pid_g);
                                //ep.setId_CG_e(right_CG_e_id);

                                /*UPDATE EDGE ID IN CG INFORMATION: here need to transform the right_cg_v_id (canonical edge id) to 'edge id' in database table of 'edge'**/
                                             /*get root_fragment_version of merged model by pmv*/
                                FragmentVersion fv = model_changed_merged.getRootFragmentVersion();
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
                            }

                            j++;
                        }
                    }
                }



            }

        }catch (SerializationException se){

                LOGGER.error("Failed to convert the models into the Canonical Format, should be checked about it!!!23 Jan 2016", se);
        }catch (ImportException | JAXBException ie) {
                LOGGER.error("Failed Import the newly changed merged model after change propagation.", ie);
        }catch (RepositoryException re){

                LOGGER.error("Data base related errors." , re);
        }

        changedVariantsVersion.add(pmv);

        return changedVariantsVersion;
    }


    /* Responsible for getting all the Models and converting them to CPT internal format from G to CG*/
    private ToolboxData convertModelsToCPT(List<ProcessModelVersion> models) throws SerializationException {
        ToolboxData data = new ToolboxData();

        for (ProcessModelVersion pmv : models) {
            data.addModel(pmv, processSrv.getCanonicalFormat(pmv));
        }

        return data;
    }

    /* Responsible for getting all the Models and converting them to CPT internal format from CG to G*/
    private LinkedHashMap<ProcessModelVersion, CanonicalProcessType> convertModelsToCPT_from_CG_to_G(List<ProcessModelVersion> models) throws SerializationException {
        LinkedHashMap<ProcessModelVersion,CanonicalProcessType> data = new LinkedHashMap<ProcessModelVersion,CanonicalProcessType>();

        for (ProcessModelVersion pmv : models) {
            data.put(pmv, processSrv.getCanonicalFormat(pmv));
        }

        return data;
    }


    /*Does change propagation from G (one variant of CG) to CG*/
    private MergeProcesses_Result_DO performChangePropagation(ToolboxData data,
                                                              List<NodeMappingGandCG> nodeMappingGandCGs, List<EdgeMappingGandCG> edgeMappingGandCGs ){

        Map<ProcessModelVersion, CanonicalProcessType> models_original = new HashMap<>(data.getModel());//note that this 'models_original' does not consider the sequence of models,

        ArrayList<MergeData> mergeData = new ArrayList<MergeData>();//note that 'mergeData' does consider the sequence of models

        /*loop the map named 'models_original'*/
        for(int i = 0; i<models_original.size(); i++){

            //get pmv from 'models', note sequence of models is implied in it.
            ProcessModelVersion pmv = models.get(i);

            //get 'canonical process type' from map named 'models_original'
            CanonicalProcessType cpt = models_original.get(pmv);

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
                node_id_mapping.put(node.getUri(), node.getId().toString());
            }

           /*get edge set in a fragment version*/
            List<Edge> edges = edgeSrv.getEdgesByFragmentURI(fv_uri);
           /*set up a hash map to record the mapping between edge_id_in_canonical (uri) and edge id in EDGE TABLE*/
            HashMap<String, String> edge_id_mapping = new HashMap<String, String>();
            Iterator<Edge> ite_edge = edges.iterator();/*get the iterator*/
            while (ite_edge.hasNext()){

                Edge edge = ite_edge.next();
                edge_id_mapping.put(edge.getOriginalId(), edge.getId().toString());
            }

            MergeData model = new MergeData();
            model.setModel_id(pmv_id);
            model.setNode_id_mapping(node_id_mapping);
            model.setEdge_id_mapping(edge_id_mapping);
            model.setCanonicalProcessType(cpt);

            mergeData.add(model);



        }

        /*invoke*/
        MergeProcesses_Result_DO changedMergedCanonicalModel = PropagateChanges.propagateChangesFromG2CG(mergeData, nodeMappingGandCGs, edgeMappingGandCGs);

        return changedMergedCanonicalModel;


    }

    /*Does change propagation from CG to all variants of it
    * @param data: a linkedhashmap including 'model_original_merged','model_changed_merged',
        // 'model_original_variant1','model_original_variant2',...in sequence
    * @param nodeMappingGandCGs: node mappings between the original CG and all its variants
    * @param edgeMappingGandCGs: edge mappings between the original CG and all its variants
    * @return propagateChangesCG2G_result_do: propagation result from CG to G, including a list of evolved variant(s),
    * updated mapping relationships between merged model and all the variants.
    *
    * note: the mapping relations between an original variant and a changed variant should be recorded. 11 April 2016
    *
    * */
    private PropagateChangesCG2G_Result_DO performChangePropagationFromCGToG(LinkedHashMap<ProcessModelVersion,CanonicalProcessType> data,
                                                              List<NodeMappingGandCG> nodeMappingGandCGs, List<EdgeMappingGandCG> edgeMappingGandCGs ){

        ArrayList<MergeData> mergeData = new ArrayList<MergeData>();

        /*loop the map named 'models_original'*/
        for(int i = 0; i<data.size(); i++){

            //get pmv from 'models', note sequence of models is implied in it.
            ProcessModelVersion pmv = models.get(i);

            //get 'canonical process type' from map named 'models_original'
            CanonicalProcessType cpt = data.get(pmv);

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


                node_id_mapping.put(node.getUri(), node.getId().toString());//getUri() returns canonical id, getId() returns pmv id
            }

           /*get edge set in a fragment version*/
            List<Edge> edges = edgeSrv.getEdgesByFragmentURI(fv_uri);
           /*set up a hash map to record the mapping between edge_id_in_canonical (uri) and edge id in EDGE TABLE*/
            HashMap<String, String> edge_id_mapping = new HashMap<String, String>();
            Iterator<Edge> ite_edge = edges.iterator();/*get the iterator*/
            while (ite_edge.hasNext()){

                Edge edge = ite_edge.next();
                edge_id_mapping.put(edge.getOriginalId(), edge.getId().toString());//getOriginalId() returns canonical id, getId() returns pmv id
            }

            MergeData model = new MergeData();
            model.setModel_id(pmv_id);
            model.setNode_id_mapping(node_id_mapping);
            model.setEdge_id_mapping(edge_id_mapping);
            model.setCanonicalProcessType(cpt);

            mergeData.add(model);//in sequence, 'mergeData' includes 'model_original_merged','model_changed_merged',
            // 'model_original_variant1','model_original_variant2',...
        }

        /*invoke*/
        PropagateChangesCG2G_Result_DO propagateChangesCG2G_result_do = PropagateChanges.propagateChangesFromCG2G(mergeData, nodeMappingGandCGs, edgeMappingGandCGs);

        return propagateChangesCG2G_result_do;


    }



}
