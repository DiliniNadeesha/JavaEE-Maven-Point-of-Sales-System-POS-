package lk.ijse.dep.web.service.listner;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;

@WebListener
public class ContextListner implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("com.mysql.jdbc.Driver");
        bds.setUsername("root");
        bds.setPassword("1234");
        bds.setUrl("jdbc:mysql://localhost:3306/webposservlet");
        bds.setInitialSize(10);
        bds.setMaxTotal(20);
        bds.setMaxIdle(10);
        sce.getServletContext().setAttribute("pool", bds);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ((BasicDataSource) sce.getServletContext().getAttribute("pool")).close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
