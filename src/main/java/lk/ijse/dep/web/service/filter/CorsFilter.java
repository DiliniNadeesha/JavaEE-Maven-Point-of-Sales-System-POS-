package lk.ijse.dep.web.service.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/customers4", "/customers5", "/customers6", "/customers7", "/api/v1/customers", "/api/v1/items"})
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //System.out.println(request.getMethod());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        super.doFilter(request, response, chain);
    }
}
