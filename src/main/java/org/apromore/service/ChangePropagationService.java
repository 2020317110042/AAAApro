package org.apromore.service;

import org.apromore.dao.model.NativeType;
import org.apromore.dao.model.ProcessModelVersion;
import org.apromore.exception.ExceptionChangePropagation;
import org.apromore.helper.Version;
import org.apromore.model.ProcessVersionIdType;

import java.util.List;

/**
 * Created by fengz2 on 1/10/2014.
 */
public interface ChangePropagationService {


    //*Compare two process variants, create a new merged model and save the result *//
    /**@param username username
     * @param originalVariantVersion the original version of process variant
     * @param changedVariantVersion the evolved version of process variant
     *
     *
     * @return the new merged process summary of the newly merged process**/

    List<ProcessModelVersion> changePropagationFromGtoCG(String username, ProcessVersionIdType originalVariantVersion,
                                                         ProcessVersionIdType changedVariantVersion, String newBranchName,
                                                         Version newVersionForMergedModel, NativeType natType) throws ExceptionChangePropagation;

    //*Compare two merged process graphs, create a set of new process variants and save the result *//
    /**@param username username
     * @param originalMergedVersion the original version of the merged process
     * @param changedMergedVersion the evolved version of the merged process
     *  @param updated_variant_branch_name the new branch name for new version of any process variant,
     *  @param updated_variant_version the new version info for new version of any process variant,
     *
     * @return the process summary of the newly evolved process variants**/

    List<ProcessModelVersion> changePropagationFromCGtoG(String username, ProcessVersionIdType originalMergedVersion,
                                                         ProcessVersionIdType changedMergedVersion, String updated_variant_branch_name,
                                                         Version updated_variant_version, NativeType natType) throws ExceptionChangePropagation;






}
