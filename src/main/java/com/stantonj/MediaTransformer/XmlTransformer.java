package com.stantonj.MediaTransformer;

import javax.ws.rs.core.MediaType;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.beans.XMLEncoder;

/**
 * Created by Joey on 2/20/15.
 */
public class XmlTransformer implements iMediaTransformer{
    @Override
    public String getResultMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public Object getTransformation(Object obj, Map<String, String> Metadata) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder e = new XMLEncoder(out);
        e.writeObject(obj);
        e.close();
        return out.toString();
    }
}
