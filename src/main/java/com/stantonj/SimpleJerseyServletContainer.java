package com.stantonj;



import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Joseph Stanton on 2/19/15.
 */
public class SimpleJerseyServletContainer extends HttpServlet {

    public static class MethodInfo {
        public String Path;
        public String HttpMethod;
        public Method method;
        public Object instance;
    }

    private Map<String,Map<String, MethodInfo>> Endpoints;

    public SimpleJerseyServletContainer() {
        Endpoints = new HashMap<String, Map<String, MethodInfo>>();
    }






    public boolean AddServlet(Object servlet) {
        MethodInfo[] methods = GetServletMethodInfo(servlet.getClass());

        if (methods.length == 0)
            return false;

        RegisterMethods(methods, servlet);

        return true;
    }

    public boolean AddServlet(Class servletClass) throws IllegalAccessException, InstantiationException {
        MethodInfo[] methods = GetServletMethodInfo(servletClass);
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
                Endpoints.put(mi.HttpMethod, new HashMap<String, MethodInfo>());
            }
            Endpoints.get(mi.HttpMethod).putIfAbsent(mi.Path, mi);
        }
    }


    private static MethodInfo[] GetServletMethodInfo(Class servlet) {
        Method[] methods = servlet.getDeclaredMethods();
        LinkedList<MethodInfo> Endpoints = new LinkedList<MethodInfo>();

        for (Method m : methods) {
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            Annotation[] annotations = m.getDeclaredAnnotations();
            MethodInfo info = new MethodInfo();
            info.method = m;
            //TODO: get mediainfo
            //TODO: add query string parameters
            for (Annotation an : annotations) {
                if (an instanceof javax.ws.rs.Path) {
                    info.Path = ((Path) an).value();
                } else if (an instanceof HttpMethod) {
                    info.HttpMethod = ((HttpMethod) an).value();
                } else if (an instanceof GET) {
                    info.HttpMethod = "GET";
                } else if (an instanceof POST) {
                    info.HttpMethod = "POST";
                } else if (an instanceof PUT) {
                    info.HttpMethod = "PUT";
                } else if (an instanceof DELETE) {
                    info.HttpMethod = "DELETE";
                } else if (an instanceof HEAD) {
                    info.HttpMethod = "HEAD";
                }
            }
            if (info.Path == null)
                continue;
            if(!info.Path.startsWith("/"))
                info.Path = "/" + info.Path;

            Endpoints.add(info);

        }
        //TODO: check for class Path annotation

        return Endpoints.toArray(new MethodInfo[Endpoints.size()]);
    }

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        MethodInfo mi = Endpoints
                .get(req.getMethod())
                .get(req.getPathInfo()) //TODO: double check this method call
            ;
        Object ret = null;
        if(mi == null) {
            res.setStatus(500);
            return;
        }

        try {
            ret = mi.method.invoke(mi.instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if(ret == null) {
            res.setStatus(500);
            return;
        }
        Gson gson = new Gson();

        res.getOutputStream().print(gson.toJson(ret));
    }


}
