package org.apromore.dao;

import org.apromore.dao.model.NodeMappingGandCG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface domain model Data access object NodeMappingGandCG.
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 * @version 1.0
 * @see org.apromore.dao.model.NodeMappingGandCG
 */
@Repository
public interface NodeMappingGandCGRepository extends JpaRepository<NodeMappingGandCG, Integer> {



    /*find the node mapping between G and CG*
     * @param  variantId the pmv id of variant G
     * @param  mergedId the pmv id of merged graph CG
     * @return  the node mapping list between G and CG
     *
     * @*/

    @Query("SELECT nmg2cg FROM NodeMappingGandCG nmg2cg WHERE (nmg2cg.PID_G = ?1 OR nmg2cg.PID_G = null) AND (nmg2cg.PID_CG = ?2)")
    List<NodeMappingGandCG> findNodeMappingBetweenGAndCGByVariantIdAndMergedId(String variantId, String mergedId);


    /*fine the pmv id of merged model by using pmv id of the process variant
    * @param pmv id of the process variant
    * @return list of pmv id of the merged model(s)*/

    @Query("SELECT DISTINCT nmg2cg.PID_CG FROM NodeMappingGandCG nmg2cg WHERE nmg2cg.PID_G =?1")
    List<String> findMergedModelIdByVariantId(String variantId);

    /*find the pmv id of process variants by using pmv id of the merged graph
    * @param pmv id of the merged model
    * @param list of pmv id of the process variant(s) added in 7 April 2016*/
    @Query("SELECT DISTINCT nmg2cg.PID_G FROM NodeMappingGandCG nmg2cg WHERE nmg2cg.PID_CG =?1")
    List<String> findVariantsIdByMergedId(String mergedId);

}

