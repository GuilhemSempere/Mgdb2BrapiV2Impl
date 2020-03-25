package org.brapi.v2.api;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
public class ApiOriginFilter implements javax.servlet.Filter {
	
	 static private final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ApiOriginFilter.class);
	 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    	
    	LOG.info("ApiOriginFilter invoked");

        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
