package lk.ijse.dep.web.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;

@MultipartConfig
@WebServlet(urlPatterns = "/api/v1/customers")
public class CustomerServlet extends HttpServlet {

    private Connection getConnection() {
        try {
            return ((BasicDataSource) getServletContext().getAttribute("pool")).getConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM Customer");
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while(rst.next()){
                String id = rst.getString(1);
                String name = rst.getString(2);
                String address = rst.getString(3);
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                objectBuilder.add("id",id);
                objectBuilder.add("name",name);
                objectBuilder.add("address",address);
                arrayBuilder.add(objectBuilder.build());
            }
            JsonArray customers = arrayBuilder.build();
            resp.setContentType("application/json");
            resp.setIntHeader("X-Count", customers.size());
            resp.getWriter().println(customers.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject customer = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
            try {
                if (customer.getString("id").trim().length() == 0){
                    throw new NullPointerException();
                }
                pstm.setObject(1, customer.getString("id"));
                pstm.setObject(2, customer.getString("name"));
                pstm.setObject(3, customer.getString("address"));
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            pstm.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_CREATED);
        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject customer = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
            pstm.setObject(3,customer.getString("id"));
            pstm.setObject(1,customer.getString("name"));
            pstm.setObject(2,customer.getString("address"));
            pstm.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String customerId = req.getParameter("id");
        if (customerId == null || customerId.trim().length() == 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Connection connection = getConnection();
        try{
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
            pstm.setObject(1, customerId);
            boolean result = pstm.executeUpdate() > 0;
            if (result){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }catch (Exception e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
    }
