/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * This is a simple servlet that will use JDBC to gather all of the Indy 500
 * winner information from a database and format it into an HTML table.
 * No guarantees of meeting:
 *			Thread safety
 *			Does not adhere to "SOLID:
 *			No DAO pattern etc.
 *			No page scolling
 * This is "quick and dirty" simple DB table query, formats DB resultset to
 * an HTML table format
 */
@WebServlet(urlPatterns = {"/IndyWinnerSimpleSV"})
public class IndyWinnerSimpleSV extends HttpServlet {

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        response.setContentType("text/html");

        formatPageHeader(buffer);

        // Get the current page number from the request, default to 1
        int currentPage = 1;
        if (request.getParameter("page") != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        // Set the number of winners per page
        int winnersPerPage = 10;
        int offset = (currentPage - 1) * winnersPerPage;

        // Query the database for the current page's winners
        sqlQuery("com.mysql.cj.jdbc.Driver",                   // Database Driver
                "jdbc:mysql://localhost/IndyWinners",            // Database Name
                "root", "root",                                 // Username and password
                "SELECT * from IndyWinners ORDER BY year DESC LIMIT " + winnersPerPage + " OFFSET " + offset,  // Pagination query
                buffer, uri, currentPage);

        buffer.append("</html>");

        // Send the formatted page back to the client
        try (java.io.PrintWriter out = new java.io.PrintWriter(response.getOutputStream())) {
            out.println(buffer.toString());
            out.flush();
        } catch (Exception ex) {
        }
    }

    /**
     * Create an HTML page header for this output
     * @param buffer Place to build the string
     */
    private void formatPageHeader(StringBuilder buffer) {
        buffer.append("<html>");
        buffer.append("<head>");
        buffer.append("<title>Indianapolis 500 Winners</title>");
        buffer.append("</head>");
        buffer.append("<h2><center>");
        buffer.append("Indianapolis 500 Winners");
        buffer.append("</center></h2>");
        buffer.append("<br>");
    }

    /**
     * Execute the SQL query and format the results into an HTML table
     *
     * @param driverName JDBC driver name
     * @param connectionURL JDBC connection URL
     * @param user Database username
     * @param pass Database password
     * @param query SQL query
     * @param buffer Output string buffer to build and populate with DB row details
     * @param uri Request URI
     * @param currentPage Current page number
     */
    private void sqlQuery(String driverName, String connectionURL, String user, String pass,
                          String query, StringBuilder buffer, String uri, int currentPage) {
        boolean rc = true;

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        long startMS = System.currentTimeMillis();
        int rowCount = 0;

        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(connectionURL, user, pass);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            rowCount = resultSetToHTML(rs, buffer, uri);

        } catch (Exception ex) {
            buffer.append("Exception!").append(ex.toString());
            rc = false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException sqlEx) {
            }
        }

        if (rc) {
            long elapsed = System.currentTimeMillis() - startMS;
            buffer.append("<br><i> (");
            buffer.append(rowCount);
            buffer.append(" rows in ");
            buffer.append(elapsed);
            buffer.append("ms) </i>");
        }

        // Add the pagination controls (Continue button)
        addPaginationControls(buffer, currentPage);
    }

    /**
     * Convert the ResultSet to an HTML table
     *
     * @param rs JDBC ResultSet
     * @param buffer Output string buffer to build HTML
     * @param uri Request URI
     * @return Number of rows in the ResultSet
     */
    private int resultSetToHTML(ResultSet rs, StringBuilder buffer, String uri) throws Exception {

        int rowCount = 0;

        buffer.append("<center><table border>");

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        buffer.append("<tr>");
        for (int i = 0; i < columnCount; i++) {
            buffer.append("<th>").append(rsmd.getColumnLabel(i + 1)).append("</th>");
        }
        buffer.append("</tr>");

        while (rs.next()) {
            rowCount++;
            buffer.append("<tr>");
            for (int i = 0; i < columnCount; i++) {
                String data = rs.getString(i + 1);
                buffer.append("<td>").append(data).append("</td>");
            }
            buffer.append("</tr>");
        }

        buffer.append("</table></center>");

        return rowCount;
    }

    /**
     * Add pagination controls (Previous and Continue buttons)
     * @param buffer Output string buffer
     * @param currentPage Current page number
     */
    private void addPaginationControls(StringBuilder buffer, int currentPage) {
        buffer.append("<center>");
        
        // Add "Previous" button if not on the first page
        if (currentPage > 1) {
            buffer.append("<a href=\"IndyWinnerSimpleSV?page=")
                  .append(currentPage - 1)
                  .append("\">Previous</a> ");
        }

        // Add "Continue" button to go to the next page
        buffer.append("<a href=\"IndyWinnerSimpleSV?page=")
              .append(currentPage + 1)
              .append("\">Continue</a>");

        buffer.append("</center>");
    }
}
