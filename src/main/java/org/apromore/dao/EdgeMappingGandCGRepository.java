package org.apromore.dao;

import org.apromore.dao.model.EdgeMappingGandCG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface domain model Data access object EdgeMappingGandCG.
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 * @version 1.0
 * @see org.apromore.dao.model.NodeMappingGandCG
 */

@Repository
public interface EdgeMappingGandCGRepository extends JpaRepository<EdgeMappingGandCG, Integer> {

    /**Find the edge mapping between G (a specified variant) and CG (merged model)*
     * @param  variantId the database id of variant G
     * @param  mergedId the database id of merged graph CG
     * @return  the edge mapping list between G and CG*/

    @Query("SELECT emg2cg FROM EdgeMappingGandCG emg2cg WHERE emg2cg.PID_G = ?1 and emg2cg.PID_CG = ?2")
    List<EdgeMappingGandCG> findEdgeMappingBetweenGAndCGByVariantIdAndMergedId(String variantId, String mergedId);

}
