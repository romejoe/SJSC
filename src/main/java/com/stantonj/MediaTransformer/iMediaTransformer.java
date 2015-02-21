package com.stantonj.MediaTransformer;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Joey on 2/20/15.
 */
public interface iMediaTransformer {
    public String getResultMediaType();
    public Object getTransformation(Object obj, Map<String, String> Metadata);
}
