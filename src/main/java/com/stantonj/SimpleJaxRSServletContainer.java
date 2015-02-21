package com.stantonj;



import com.google.gson.Gson;
import com.stantonj.MediaTransformer.JsonTransformer;
import com.stantonj.MediaTransformer.XmlTransformer;
import com.stantonj.MediaTransformer.iMediaTransformer;
import com.sun.deploy.util.ArrayUtil;
import com.sun.tools.internal.ws.wsdl.document.http.HTTPConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Joseph Stanton on 2/19/15.
 */
public class SimpleJaxRSServletContainer extends HttpServlet {




    //HTTPMethod to Endpoint to mediatype to method
    private Map<String,Map<String, Map<String, MethodInfo>>> Endpoints;
    /*private List<TransformerLevel> MediaTransformers;
    private HashMap<Integer, TransformerLevel> TransformerMap;
    public class TransformerLevel{
        int level;
        Map<String,iMediaTransformer> transformers;
    }

    public void AddTransformer(int level, iMediaTransformer transformer){
        if(!TransformerMap.containsKey(level)){
            int i = 0;
            TransformerLevel newLevel = new TransformerLevel();
            newLevel.level = level;
            newLevel.transformers = new HashMap<String, iMediaTransformer>();
            TransformerMap.put(level, newLevel);

            for(TransformerLevel tlevel: MediaTransformers){
                if(level < tlevel.level)
                    break;
                ++i;
            }

            MediaTransformers.add(i, newLevel);
        }
        TransformerMap.get(level).transformers.put(transformer.getResultMediaType(), transformer);
    }

    public SimpleJaxRSServletContainer(Map<iMediaTransformer, Integer> transformers){ //Robots in disguise
        Endpoints = new HashMap<String, Map<String, MethodInfo>>();
        MediaTransformers = new LinkedList<TransformerLevel>();
        TransformerMap = new HashMap<Integer, TransformerLevel>();

        for(Map.Entry<iMediaTransformer, Integer> pair : transformers.entrySet()){
            AddTransformer(pair.getValue(), pair.getKey());
        }

    }

    public SimpleJaxRSServletContainer() {
        this(new HashMap<iMediaTransformer, Integer>(){{
            put(new JsonTransformer(), 0);
        }});

    }
*/

    public Map<String, iMediaTransformer> MediaTransformers;

    public void AddTransformer(iMediaTransformer transformer){
        MediaTransformers.put(transformer.getResultMediaType(),transformer);
    }

    public SimpleJaxRSServletContainer(Collection<iMediaTransformer> transformers){ //Robots in disguise
        Endpoints = new HashMap<String, Map<String, Map<String, MethodInfo>>>();
        MediaTransformers = new HashMap<String, iMediaTransformer>();

        for(iMediaTransformer transformer : transformers){
            AddTransformer(transformer);
        }

    }

    public SimpleJaxRSServletContainer() {
        this(new ArrayList<iMediaTransformer>(){{
            add(new JsonTransformer());
            add(new XmlTransformer());
        }});

    }

    public boolean AddServlet(Object servlet) {
        MethodInfo[] methods = MethodInfo.GetServletMethodInfo(servlet.getClass());

        if (methods.length == 0)
            return false;

        RegisterMethods(methods, servlet);

        return true;
    }

    public boolean AddServlet(Class servletClass) throws IllegalAccessException, InstantiationException {
        MethodInfo[] methods = MethodInfo.GetServletMethodInfo(servletClass);
        if (methods.length == 0)
            return false;
        Object servlet = servletClass.newInstance();
        RegisterMethods(methods, servlet);

        return false;
    }

    private void RegisterMethods(MethodInfo[] methods, Object inst){
        for(MethodInfo mi : methods){
            mi.instance=inst;
            //TODO: add locale???
            mi.HttpMethod = mi.HttpMethod.toUpperCase();
            if(!Endpoints.containsKey(mi.HttpMethod)){
                Endpoints.put(mi.HttpMethod, new HashMap<String, Map<String, MethodInfo>>());
            }
            Map<String, Map<String, MethodInfo>> pathMap = Endpoints.get(mi.HttpMethod);
            if(!pathMap.containsKey(mi.Path)){
                pathMap.put(mi.Path, new HashMap<String, MethodInfo>());
            }
            Map<String,MethodInfo> TypeMap = pathMap.get(mi.Path);
            for(String type:mi.Produces){
                TypeMap.put(type, mi);
            }

        }
    }




    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String,MethodInfo> TypeMap = Endpoints
                .get(req.getMethod())
                .get(req.getPathInfo()) //TODO: double check this method call
            ;
        Object ret = null;
        if(TypeMap.isEmpty()) {
            res.setStatus(500);
            return;
        }

        Enumeration<String> AcceptHeaders = req.getHeaders("Accept");
        String AcceptHeader = "";

        while(AcceptHeaders.hasMoreElements()){
            AcceptHeader = AcceptHeader + AcceptHeaders.nextElement();
            if(AcceptHeaders.hasMoreElements()) AcceptHeader = AcceptHeader + ";";
        }

        List<AcceptHeaderInfo> HeaderInfos = AcceptHeaderInfo.ParseAcceptHeader(AcceptHeader);

        if(HeaderInfos.size() == 0){
            HeaderInfos.add(new AcceptHeaderInfo(){{
                Type = "*/*";
            }});
        }

        Collections.sort(HeaderInfos);

        CollectionUtils.filter(HeaderInfos, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                AcceptHeaderInfo tmp = (AcceptHeaderInfo) o;
                for(String type: MediaTransformers.keySet()){
                    if(MediaTypeUtil.isValidType(tmp.Type, type)){
                        return true;
                    }
                }
                return false;
            }
        });

        if(HeaderInfos.size() == 0){
            res.setStatus(406);
            return;
        }
        //an acceptable transformer was found

        //select appropriate Method
        MethodInfo mi = null;
        iMediaTransformer transformer = null;

        Done:
        if(mi == null)
        for(AcceptHeaderInfo header:HeaderInfos){
            for(String type: TypeMap.keySet()){
                if(MediaTypeUtil.isValidType(header.Type, type)){
                    for(String transformerType:MediaTransformers.keySet()){
                        if(MediaTypeUtil.isValidType(type, transformerType)){
                            mi = TypeMap.get(type);
                            transformer = MediaTransformers.get(transformerType);

                            break Done;
                        }
                    }
                }
            }
        }


        ret = executeMethod(mi, req);
        /*try {
            ret = mi.method.invoke(mi.instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }*/

        if(ret == null) {
            res.setStatus(500);
            return;
        }

        Object out = transformer.getTransformation(ret, null);

        res.getOutputStream().print(String.valueOf(out));
    }

    private Object executeMethod(MethodInfo mi, HttpServletRequest req){


        //build arguments
        Method m = mi.method;
        List<ArgumentInfo> ai = mi.argumentInfo;

        Object[] arguments = new Object[ai.size()];
        for(int i = 0; i < arguments.length; ++i){
            ArgumentInfo curInfo = ai.get(i);
            Object InitialValue = curInfo.DefaultValue;

            Object tmp = null;
            if(curInfo.FormParam != null){
                tmp = req.getParameter(curInfo.FormParam);
            }else if(curInfo.CookieParam != null){
                for(javax.servlet.http.Cookie cookie : req.getCookies()){
                    if(cookie.getName() == curInfo.CookieParam){
                        tmp = cookie;
                        break;
                    }
                }
            } else if(curInfo.HeaderParam != null){
                //TODO: Join all headers
                tmp = req.getHeader(curInfo.HeaderParam);
            } else if(curInfo.PathParam != null){
                //TODO: Parse Path
            } else if(curInfo.QueryParam != null){
                String queryStr = req.getQueryString();
                if(queryStr == null){
                    tmp = null;
                }
                else{
                    tmp = QueryStringParser.ParseQueryString(queryStr).get(curInfo.QueryParam);
                }
            }


        }

        return null;
    }


}
