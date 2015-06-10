/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlet;

import DatabaseConnection.Credentials;
import static DatabaseConnection.Credentials.getConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author NILESH
 */
@WebServlet("/products")
public class ProductServlets extends HttpServlet {
    private Object JSONValue;
    
     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Content-Type", "text/plain-text");
        try {
            PrintWriter output = response.getWriter();
            String query = "SELECT * FROM product;";
            if (!request.getParameterNames().hasMoreElements()) {
                output.println(resultMethod(query));
            } else {
                int id = Integer.parseInt(request.getParameter("product_id"));
                output.println(resultMethod("SELECT * FROM product WHERE product_id= ?", String.valueOf(id)));
            }

        } catch (IOException ex) {
            System.err.println("Input output Exception: " + ex.getMessage());
        }
    }

   @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Set<String> keyValues = request.getParameterMap().keySet();

        try {
            PrintWriter output = response.getWriter();
            if (keyValues.contains("product_id") && keyValues.contains("name") && keyValues.contains("description")
                    && keyValues.contains("quantity")) {
                String product_id = request.getParameter("product_id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("INSERT INTO product (product_id,name,description,quantity) VALUES (?, ?, ?, ?)", product_id, name, description, quantity);

            } else {
                response.setStatus(500);
                output.println("Error: Not data found for this input. Please use a URL of the form /servlet?name=XYZ&age=XYZ");
            }

        } catch (IOException ex) {
            System.err.println("Input Output Issue in doPost Method: " + ex.getMessage());
        }

    }
     @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("product_id") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String product_id = request.getParameter("product_id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("update product set product_id = ?, name = ?, description = ?, quantity = ? where product_id = ?", product_id, name, description, quantity, product_id);
            } else {
                out.println("Error: Not data found for this input. Please use a URL of the form /products?id=xx&name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            response.setStatus(500);
            System.out.println("Error in writing output: " + ex.getMessage());
        }
    }
     @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("product_id")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `product` WHERE `product_id`=" + request.getParameter("product_id"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("SQL Exception Error in Update prepared Statement: " + ex.getMessage());
                    out.println("Error in deleting entry.");
                   
                }
            } else {
                out.println("Error: Not enough data in table to delete");
                
            }
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
    }
    
     @Override
    public String getServletInfo() {
        return "Short description";
    }
    
     private String resultMethod(String query, String... params) {
        StringBuilder sb = new StringBuilder();
        String jsonString = "";
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            List l1 = new LinkedList();
            while (rs.next()) {
               
                Map m1 = new LinkedHashMap();
                m1.put("product_id", rs.getInt("product_id"));
                m1.put("name", rs.getString("name"));
                m1.put("description", rs.getString("description"));
                m1.put("quantity", rs.getInt("quantity"));
                l1.add(m1);

            }

            
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
        return jsonString.replace("},", "},\n");
    }
  private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("SQL EXception in doUpdate Method" + ex.getMessage());
        }
        return numChanges;
    }


   
}