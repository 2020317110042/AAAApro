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

package org.apromore.dao.model;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Stores the native something in apromore.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 */
@Entity
@Table(name = "native")
@Configurable("native")
@Cache(expiry = 180000, size = 5000, coordinationType = CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)
public class Native implements Serializable {

    private Integer id;
    private String content;
    private String lastUpdateDate;

    private NativeType nativeType;
    private ProcessModelVersion processModelVersion;
    private Set<Annotation> annotations = new HashSet<>();


    /**
     * Default Constructor.
     */
    public Native() { }



    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }


    @Column(name = "lastUpdateDate")
    public String getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public void setLastUpdateDate(final String newLastUpdate) {
        this.lastUpdateDate = newLastUpdate;
    }

    @Column(name = "content")
    public String getContent() {
        return this.content;
    }

    public void setContent(final String newContent) {
        this.content = newContent;
    }

    @ManyToOne
    @JoinColumn(name = "nativeTypeId")
    public NativeType getNativeType() {
        return this.nativeType;
    }

    public void setNativeType(final NativeType newNativeType) {
        this.nativeType = newNativeType;
    }

    @OneToOne(mappedBy = "nativeDocument")
    public ProcessModelVersion getProcessModelVersion() {
        return this.processModelVersion;
    }

    public void setProcessModelVersion(ProcessModelVersion newProcessModelVersion) {
        this.processModelVersion = newProcessModelVersion;
    }

    @OneToMany(mappedBy = "natve")
    public Set<Annotation> getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(final Set<Annotation> newAnnotations) {
        this.annotations = newAnnotations;
    }

}
