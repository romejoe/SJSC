package com.stantonj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joey on 2/20/15.
 */
public class AcceptHeaderInfo implements Comparable<AcceptHeaderInfo> {
    /**
     * Based on specification given here: http://www.w3.org/Protocols/HTTP/HTRQ_Headers.html#z3
     *
     */
    public String Type;
    public Double q = 1.0;
    public Double mxs;
    public Double mxb;
    public Map<String,Double> MiscAttrs;

    public AcceptHeaderInfo(){MiscAttrs = new HashMap<String, Double>();}

    static public List<AcceptHeaderInfo> ParseAcceptHeader(String header){
        String[] headerParts = header.split(",");
        List<AcceptHeaderInfo> ret = new ArrayList<AcceptHeaderInfo>(headerParts.length);
        int i = 0;

        for(String subheader: headerParts){
            String[] parts = subheader.trim().split(";");
            ret.add(new AcceptHeaderInfo());
            ret.get(i).Type = parts[0].trim();

            for(int j = 1; j < parts.length; ++j){
                int splitIndex = parts[j].indexOf('=');
                String attr = parts[j].substring(0, splitIndex).trim();
                Double value = Double.parseDouble(parts[j].substring(splitIndex + 1).trim());
                if(attr.equals("q")){
                    ret.get(i).q = value;
                } else if(attr.equals("mxs")){
                    ret.get(i).mxs = value;
                } else if(attr.equals("mxb")){
                    ret.get(i).mxb = value;
                }else{
                    ret.get(i).MiscAttrs.put(attr, value);
                }
            }

            ++i;
        }
        return ret;
    }

    @Override
    public int compareTo(AcceptHeaderInfo a2){
        return a2.q.compareTo(q);
    }

}
