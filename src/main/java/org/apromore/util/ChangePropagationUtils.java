package org.apromore.util;

import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by Zaiwen Feng on 10/1/2016.
 */
public class ChangePropagationUtils {


    /*convert a comma separated string to an hashset*/
    public static HashSet<String> convertCommaSeparatedStringToHashset(String commaSeperatedString){

        HashSet<String> trimedHashSet = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(commaSeperatedString,",");

        while(st.hasMoreTokens()){

            trimedHashSet.add(st.nextToken());
        }



        return trimedHashSet;

    }

}
