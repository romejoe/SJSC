package com.stantonj;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joey on 2/21/15.
 */
public class QueryStringParser {
    public static Map<String, String> ParseQueryString(String str){
        String[] parts = str.split("&");
        Map<String, String> ret = new HashMap<String, String>();
        for(String param:parts){
            String[] paramParts = param.split("=");
            ret.put(paramParts[0], paramParts[1]);
        }
        return ret;
    }

}
