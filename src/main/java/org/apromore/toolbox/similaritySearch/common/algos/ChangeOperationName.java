package org.apromore.toolbox.similaritySearch.common.algos;

public enum ChangeOperationName {

    //G2CG
    INSERTEDGETOG,//插入边√
    DELETEEDGEFROMG,//删除边√
    INSERTNODETOG,//插入点○
    ADDNODETOG,//添加点○
    PREPENDNODETOG,//预加点√
    APPENDNODETOG,//追加点√
    CHANGEEDGEING,//改变边(插入边与删除边缝合)√
    REMOVENODEFROMG,//删除点√
    UPDATELABELTOG,//更新点标签√

    //CG2G
    APPENDNODETOCG,//追加点√
    PREPENDNODETOCG,//预加点√
    ADDNODETOCG,//添加点√
    DELETEEDGEANNOTATIONFROMCG,//删除边注释√
    ADDEDGEANNOTATION,//添加边注释√
    UPDATENODEANNOTATION,//更新点注释√
    INSERTEDGETOCG,//插入边√
    DELETEEDGEFROMCG//删除边√
}
