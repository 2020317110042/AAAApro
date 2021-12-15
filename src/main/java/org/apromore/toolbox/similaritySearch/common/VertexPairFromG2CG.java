package org.apromore.toolbox.similaritySearch.common;

/**
 *Describe the vertex mapping relations between variants and merged model
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */
public class VertexPairFromG2CG {

    String left_G_v_id;
    String right_CG_v_id;
    public String pid_G;
    public String pid_CG;


    public VertexPairFromG2CG(String left, String right){

        this.left_G_v_id = left;
        this.right_CG_v_id= right;
    }

    public VertexPairFromG2CG(String left, String right, String pid_g, String pid_cg){

        this.left_G_v_id = left;
        this.right_CG_v_id= right;
        this.pid_G = pid_g;
        this.pid_CG = pid_cg;
    }

    public String getLeft_G_v_id(){
        return left_G_v_id;
    }

    public void setLeft_G_v_id(String left_G_v_id){

        this.left_G_v_id = left_G_v_id;
    }

    public String getRight_CG_v_id(){
        return right_CG_v_id;
    }

    public void  setRight_CG_v_id(String right_cg_v_id){
        this.right_CG_v_id  = right_cg_v_id;
    }

    public String getPid_G(){
        return  pid_G;
    }

    public void setPid_G(String pid_G){
        this.pid_G = pid_G;
    }

    public String getPid_CG(){
        return pid_CG;
    }

    public void setPid_CG(String pid_cg){
        this.pid_CG = pid_cg;
    }
}
