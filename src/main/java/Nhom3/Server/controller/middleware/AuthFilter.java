package Nhom3.Server.controller.middleware;


import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.service.AccountService;
import Nhom3.Server.service.General;
import com.google.gson.Gson;
import jakarta.servlet.*;
import java.io.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {
    @Autowired
    AccountService accountService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String path = request.getServletPath();
//        System.out.println("path:"+path);
        if(
                path.startsWith("/account/registerStep1")
                ||path.startsWith("/account/registerStep2")
                ||path.startsWith("/account/login")
                ||path.startsWith("/account/resetPasswordStep1")
                ||path.startsWith("/account/resetPasswordStep2")
                ||path.startsWith("/resource")
        ){
            //has not auth
            filterChain.doFilter(request,servletResponse);
        }else{
            String jwt = request.getHeader("auth");
            AccountService.AccountAuth accountAuth = accountService.checkAndGetAccountAuth(jwt);
//            System.out.println(jwt);
            if(accountAuth==null){
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                httpResponse.setStatus(HttpServletResponse.SC_OK);
                httpResponse.setContentType("application/json");

                // Create a JSON response object
                ResponseAPIModel errorResponse = new ResponseAPIModel(403,ResponseAPIModel.Status.Fail,"Access Denied.","");

                // Convert the JSON response object to a JSON string
                String jsonResponse = new Gson().toJson(errorResponse);

                // Write the JSON response to the output stream
                PrintWriter writer = httpResponse.getWriter();
                writer.print(jsonResponse);
                writer.flush();
            }else{
                String jsonAccountAuth = new Gson().toJson(accountAuth);
                request.setAttribute("accountAuth",jsonAccountAuth);
                filterChain.doFilter(request,servletResponse);
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
