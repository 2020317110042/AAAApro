package org.apromore.dao;

import org.apromore.dao.model.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface domain model Data access object Edge.
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @version 1.0
 * @see org.apromore.dao.model.Edge
 */
@Repository
public interface EdgeRepository extends JpaRepository<Edge, Integer> {

    /**
     * Find the Edge by It's OriginalId.
     * @param originalId the original to search for.
     * @return the found Edge.
     */
    @Query("SELECT e FROM Edge e JOIN e.edgeMappings em JOIN em.fragmentVersion f WHERE e.originalId = ?1 and f.id = ?2")
    Edge findEdgeByOriginalIdAndFragmentVersion(String originalId, Integer fragmentId);



    /**
     * Returns the Edge records for the fragment URI.
     * @param fragmentURI the fragment uri.
     * @return the list of edges or null.
     */
    @Query("SELECT e FROM Edge e JOIN e.edgeMappings em JOIN em.fragmentVersion f WHERE f.uri = ?1")
    List<Edge> getEdgesByFragmentURI(String fragmentURI);

}
