/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author girish
 */
public class RenderReportMetadata extends HttpServlet {
    
    @Override
    public void init() {
        if("".equals(ReportGenerator.servletPath)) {
            // ReportGenerator instance not created. Therefore, set the path and create
            String rootPath = this.getServletConfig().getServletContext().getRealPath("/");
            ReportGenerator.servletPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
            ReportGenerator.getInstance();
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            
        // read the request body and resolve the rptOptions
//            ReportOptions rptOptions = ReportGenerator.getReportOptions(request);
//                        
//            // fetch the report instance
//            JasperDesign rpt = ReportGenerator.getReportDefinition(rptOptions);
//            
//            String srOut = ReportUtil.fixSubReportLinks(rpt, rptOptions);
//        
//            response.setContentType("text/html;charset=UTF-8");
//            
//            PrintWriter out = response.getWriter();
//            /* TODO output your page here. You may use following sample code. */
//            out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet RenderReportMetadata</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet RenderReportMetadata at " + request.getContextPath() + "</h1>");
//            out.println("<h2>Output</h2>");
//            out.println("<span>" + srOut + "</span>");
//            out.println("</body>");
//            out.println("</html>");
//            
//            try {
//                // render output
//                ReportGenerator.renderReport(rpt, rptOptions, response);
//            } catch (Exception exInner) {
//                out.println(exInner.getMessage());
//            }
            
        
        } catch (Exception ex) {
            throw new ServletException(ex.getMessage());
        }
             
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        processRequest(request, response);
    }

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
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
