/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.design.JasperDesign;

/**
 *
 * @author girish
 */
public class RenderReport extends HttpServlet {
    
    private static ServletContext myLogger = null;
    
    @Override
    public void init() {
        if("".equals(ReportGenerator.servletPath)) {
            // ReportGenerator instance not created. Therefore, set the path and create
            String rootPath = this.getServletConfig().getServletContext().getRealPath("/");
            ReportGenerator.servletPath = rootPath.replaceFirst("/CoreJSReportServer/", "");
            ReportGenerator.getInstance();
            
            myLogger = this.getServletContext();            
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
        StringBuilder debugInfo = new StringBuilder();
        try {
            
            debugInfo.append(System.lineSeparator()).append("-----Debug Info Starts here -------").append(System.lineSeparator());
            debugInfo.append("ServletPath: ").append(ReportGenerator.servletPath).append(System.lineSeparator());
            debugInfo.append("Calling ReportGenerator.getReportOptions").append(System.lineSeparator());
            
            // read the request body and resolve the rptOptions
            ReportOptions rptOptions = ReportUtil.parseReportOptions(request, debugInfo);
            
            
            debugInfo.append("Completed ReportGenerator.getReportOptions").append(System.lineSeparator());
            debugInfo.append("-----Report Options -------").append(System.lineSeparator());
            debugInfo.append(rptOptions.toJSON()).append(System.lineSeparator());
            debugInfo.append("-----Report Options -------").append(System.lineSeparator());
            debugInfo.append("Calling ReportGenerator.getReportDefinition").append(System.lineSeparator());
                        
            // fetch the report instance
            JasperDesign rpt = ReportGenerator.getReportDefinition(rptOptions, debugInfo);
            
            debugInfo.append("Completed ReportGenerator.getReportDefinition").append(System.lineSeparator());
            debugInfo.append("Calling ReportUtil.fixSubReportLinks").append(System.lineSeparator());
            
            // Build subreport relations
            ReportUtil.fixSubReportLinks(rpt, rptOptions, debugInfo);
                        
            debugInfo.append("Completed ReportUtil.fixSubReportLinks").append(System.lineSeparator());
            debugInfo.append("Calling ReportGenerator.renderReport").append(System.lineSeparator());            
            
            // render output
            ReportGenerator.renderReport(rpt, rptOptions, response, debugInfo);            
            
            debugInfo.append("Completed ReportGenerator.renderReport").append(System.lineSeparator());
            debugInfo.append("-----Debug Info Ends Normally -------").append(System.lineSeparator());
            
            if (rptOptions.RptParams.get("debug-report").toString().equals("true")) {                
                myLogger.log(debugInfo.toString());
            }
            
        } catch (Exception ex) {            
            debugInfo.append("-----Debug Info Ends with Exception -------").append(System.lineSeparator());
            debugInfo.append("-----Debug Exception -------").append(System.lineSeparator());
            debugInfo.append(ex.getMessage()).append(System.lineSeparator());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            debugInfo.append(sw.toString());
            debugInfo.append("-----Debug Exception End-------").append(System.lineSeparator());
            myLogger.log(debugInfo.toString());
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
