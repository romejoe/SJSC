package com.stantonj.MediaTransformer;

import com.google.gson.Gson;

import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Joey on 2/20/15.
 */
public class JsonTransformer implements iMediaTransformer{

    @Override
    public String getResultMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public Object getTransformation(Object obj, Map<String, String> Metadata) {
        //Metadata.put("Content-Type", MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

}
