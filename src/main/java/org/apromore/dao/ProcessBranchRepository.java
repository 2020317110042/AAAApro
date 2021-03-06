/*
 * Copyright © 2009-2014 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.dao;

import org.apromore.dao.model.Process;
import org.apromore.dao.model.ProcessBranch;
import org.apromore.dao.model.ProcessModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * Interface domain model Data access object Branch.
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @version 1.0
 * @see org.apromore.dao.model.ProcessBranch
 */
@Repository
public interface ProcessBranchRepository extends JpaRepository<ProcessBranch, Integer> {

    /**
     * Returns the count of process branches that has used a particular process model version as it's source.
     * @param processModelVersion the process model version we are looking at
     * @return the count of branches, 0 or more
     */
    @Query("SELECT count(b) FROM ProcessBranch b WHERE b.sourceProcessModelVersion = ?1")
    long countProcessModelBeenForked(ProcessModelVersion processModelVersion);


    //*return the process by process branch id*//

    @Query("SELECT b.process FROM ProcessBranch b WHERE b.id = ?1")
    Process FindProcessByBranchId(Integer Id);

}
