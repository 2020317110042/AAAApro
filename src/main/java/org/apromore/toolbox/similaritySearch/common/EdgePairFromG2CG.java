package org.apromore.toolbox.similaritySearch.common;

import java.util.List;

/**
 *Describe the edge mapping relations between variants and merged model
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */
public class EdgePairFromG2CG {

    private String left_G_e_id;
    private List<String> right_CG_p_e_id;
    private String pid_G;
    private String pid_CG;
    private boolean is_part_of_mapping;

    public EdgePairFromG2CG(String left, List<String> right, String pid_g, String pid_cg, boolean is_part_of_mapping){

        this.left_G_e_id = left;
        this.right_CG_p_e_id = right;
        this.pid_G = pid_g;
        this.pid_CG = pid_cg;
        this.is_part_of_mapping = is_part_of_mapping;
    }

    public String getLeft_G_e_id (){

        return  left_G_e_id;
    }

    public void setLeft_G_e_id(String left_G_e_id){

        this.left_G_e_id = left_G_e_id;
    }

    public List<String> getRight_CG_p_e_id(){

        return right_CG_p_e_id;
    }

    public void setRight_CG_p_e_id(List<String> right_CG_p_e_id){

        this.right_CG_p_e_id = right_CG_p_e_id;
    }

    public String getPid_G (){

        return pid_G;
    }

    public void setPid_G(String pid_G){

        this.pid_G = pid_G;
    }

    public String getPid_CG(){

        return  pid_CG;
    }

    public void setPid_CG(String pid_CG){

        this.pid_CG = pid_CG;
    }

    public boolean getIs_part_of_mapping(){

        return is_part_of_mapping;
    }

    public void setIs_part_of_mapping(boolean is_part_of_mapping){

        this.is_part_of_mapping = is_part_of_mapping;
    }


}
