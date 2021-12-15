package org.apromore.dao.model;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Describes the data object of node mapping.
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */


@Entity
@Table(name = "node_mapping_between_g_cg",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id"})
        }
)
@Configurable("node_mapping_between_g_cg")
@Cache(expiry = 180000, size = 10000, coordinationType = CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)

public class NodeMappingGandCG implements Serializable {


    private Integer id;
    private String pid_CG;
    private String id_CG_n;
    private String pid_G;
    private String id_G_n;


    /**
     * Public Constructor.
     */
    public NodeMappingGandCG() {super();}

    public NodeMappingGandCG(Integer nodeMappingGandCGId) {
        id = nodeMappingGandCGId;
    }

    /**
     * returns the Id of this Object.
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the Id of this Object
     * @param id the new Id.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * The PID of the merged model.
     * @return the PID
     */
    @Column(name = "PID_CG", length = 200)
    public String getPID_CG() {
        return this.pid_CG;
    }

    /**
     * The PID of the merged model.
     * @param new_PID_CG the new pid of CG.
     */
    public void setPID_CG(final String new_PID_CG) {
        this.pid_CG = new_PID_CG;
    }

    /**
     * The ID of the node in merged model.
     * @return the id_CG_n
     */
    @Column(name = "Id_CG_n", length = 200)
    public String getId_CG_n() {
        return this.id_CG_n;
    }

    /**
     * The ID of the node in the merged model.
     * @param new_Id_CG_n the new pid of CG.
     */
    public void setId_CG_n(final String new_Id_CG_n) {
        this.id_CG_n = new_Id_CG_n;
    }

    /**
     * The PID of the variant.
     * @return the PID
     */
    @Column(name = "PID_G", length = 200)
    public String getPID_G() {
        return this.pid_G;
    }

    /**
     * The PID of the variant.
     * @param new_PID_G the new pid of G.
     */
    public void setPID_G(final String new_PID_G) {
        this.pid_G = new_PID_G;
    }

    /**
     * The ID of the node in variant.
     * @return the id_G_n
     */
    @Column(name = "Id_G_n", length = 200)
    public String getId_G_n() {
        return this.id_G_n;
    }

    /**
     * The ID of the node in the variant.
     * @param new_Id_G_n the new pid of G.
     */
    public void setId_G_n(final String new_Id_G_n) {
        this.id_G_n = new_Id_G_n;
    }


}
