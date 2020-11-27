package lk.ijse.dep.web.service;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@MultipartConfig
@WebServlet(urlPatterns = "/api/v1/items")
public class ItemServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/webposservlet", "root", "1234");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM item");
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while(rst.next()){
                String code = rst.getString(1);
                String description = rst.getString(2);
                Double unitPrice = rst.getDouble(3);
                int qtyOnHand = rst.getInt(4);
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                objectBuilder.add("code",code);
                objectBuilder.add("description",description);
                objectBuilder.add("unitPrice",unitPrice);
                objectBuilder.add("qtyOnHand",qtyOnHand);
                arrayBuilder.add(objectBuilder.build());
            }
            JsonArray items = arrayBuilder.build();
            resp.setContentType("application/json");
            resp.setIntHeader("X-Count", items.size());
            resp.getWriter().println(items.toString());
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
            JsonObject item = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item VALUES (?,?,?,?)");
            try {
                if (item.getString("code").trim().length() == 0){
                    throw new NullPointerException();
                }
                pstm.setObject(1, item.getString("code"));
                pstm.setObject(2, item.getString("description"));
                pstm.setObject(3, item.getJsonNumber("unitPrice").doubleValue());
                pstm.setObject(4, item.getJsonNumber("qtyOnHand").intValue());
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
            JsonObject item = reader.readObject();
            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?, unitPrice=?, qtyOnHand=? WHERE code=?");
            pstm.setObject(4,item.getString("code"));
            pstm.setObject(1,item.getString("description"));
            pstm.setObject(2,item.getJsonNumber("unitPrice").doubleValue());
            pstm.setObject(3,item.getJsonNumber("qtyOnHand").intValue());
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
        String itemCode = req.getParameter("code");
        if (itemCode == null || itemCode.trim().length() == 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Connection connection = getConnection();
        try{
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE code=?");
            pstm.setObject(1, itemCode);
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
