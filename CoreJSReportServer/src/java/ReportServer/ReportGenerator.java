/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.type.HtmlSizeUnitEnum;

/**
 *
 * @author girish
 */
public class ReportGenerator {
    
    private static ReportGenerator _RGinstance = null;
    public static String servletPath = "";
    
    public static ReportGenerator getInstance() {
        if(_RGinstance == null) {
            _RGinstance = new ReportGenerator();
        }
        return _RGinstance;
    }
    
    private ReportGenerator() {
        // Start the JasperSoft Report Engine
        
        
    }
    
    public static JasperDesign getReportDefinition(ReportOptions rptOptions, StringBuilder debugInfo) throws Exception {
        
        Class.forName("org.postgresql.Driver");
                
        // Try to load the preport form path
        JasperDesign rpt = getReportFromPath(rptOptions, debugInfo);
        
        // Return the MasterReport
        return rpt;
    }
    
    public static void renderReport(JasperDesign rpt, ReportOptions rptOptions, HttpServletResponse resp, StringBuilder debugInfo) throws Exception {
        
        // First prepare the Parameters
        HashMap preparedParams = ReportGenerator.getParameters(rptOptions, rpt);         
        // Next prepare connection object
        Connection cn = getDbProperties(rptOptions, rpt);
        // Set the NumberFormat
        if(rptOptions.RptParams.containsKey("pcwf_locale")) {
            //   .setLocale(rptOptions.RptParams.get("pcwf_locale").toString());
            CoreJSFormatUtils.Formatter.setLocale(rptOptions.RptParams.get("pcwf_locale").toString());
        }
        // Compile the report
        JasperReport rptCompiled = JasperCompileManager.compileReport(rpt);   
        // Get output directory
        File outpath = SessionFolderManager.getFolder(rptOptions.SessionID, rptOptions.ViewerID);
        String outFile = outpath.getAbsolutePath();
        //Generate the report and prepare response
        JasperPrint jasperPrint = null;
        resp.setContentType("application/json"); 
        ReportInfo rptInfo = new ReportInfo();
        rptInfo.ReportRenderedPath = SessionFolderManager.getRenderFolder(rptOptions.SessionID, rptOptions.ViewerID); 
        
        switch(rptOptions.OutputType) {
            case html_file:
                CoreJSFormatUtils.Formatter.setForExport(false);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                int totalPages = jasperPrint.getPages().size();
                int maxPages = Integer.parseInt(rptOptions.PrintSettings.get("max_pages").toString());
                if(maxPages > 0 && totalPages > maxPages) {
                    totalPages = maxPages;
                }
                for(int pageIndex=0; pageIndex < totalPages; pageIndex++) {
                    HtmlExporter htmlExporter = new HtmlExporter();
                    htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    // create destination instance and set output
                    File htmlOutput = new File(outpath.getAbsolutePath() + "/page_" + String.valueOf(pageIndex) + ".html");
                    SimpleHtmlExporterOutput htmldest = new SimpleHtmlExporterOutput(htmlOutput);
                    htmlExporter.setExporterOutput(htmldest);
                    // create export configuration
                    SimpleHtmlExporterConfiguration htmlconf = new SimpleHtmlExporterConfiguration();
                    htmlExporter.setConfiguration(htmlconf);
                    // create report configuration
                    SimpleHtmlReportConfiguration rpthtmlconf = new SimpleHtmlReportConfiguration();
                    if(rptOptions.PrintSettings.containsKey("html_no_margin")) {
                        if(rptOptions.PrintSettings.get("html_no_margin").equals("true")) {
                            rpthtmlconf.setIgnorePageMargins(Boolean.TRUE);
                        }
                    }
                    if(rptOptions.PrintSettings.containsKey("html_in_point")) {
                        if(rptOptions.PrintSettings.get("html_in_point").equals("true")) {
                            rpthtmlconf.setSizeUnit(HtmlSizeUnitEnum.POINT);
                        }
                    }
                    rpthtmlconf.setWrapBreakWord(Boolean.TRUE);
                    rpthtmlconf.setPageIndex(pageIndex);
                    htmlExporter.setConfiguration(rpthtmlconf);
                    // export report
                    htmlExporter.exportReport();
                    htmlExporter = null;
                    SessionFolderManager.makeReadable(htmlOutput);
                    htmlOutput = null;                    
                }
                break;
            case html_single_file:
                CoreJSFormatUtils.Formatter.setForExport(false);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                int totalHtmlPages = jasperPrint.getPages().size();
                HtmlExporter htmlExporter = new HtmlExporter();
                htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File htmlOutput = new File(outpath.getAbsolutePath() + "/pages.html");
                SimpleHtmlExporterOutput htmldest = new SimpleHtmlExporterOutput(htmlOutput);
                htmlExporter.setExporterOutput(htmldest);
                // create export configuration
                SimpleHtmlExporterConfiguration htmlconf = new SimpleHtmlExporterConfiguration();
                htmlExporter.setConfiguration(htmlconf);
                // create report configuration
                SimpleHtmlReportConfiguration rpthtmlconf = new SimpleHtmlReportConfiguration();
                if(rptOptions.PrintSettings.containsKey("html_no_margin")) {
                    if(rptOptions.PrintSettings.get("html_no_margin").equals("true")) {
                        rpthtmlconf.setIgnorePageMargins(Boolean.TRUE);
                    }
                }
                if(rptOptions.PrintSettings.containsKey("html_in_point")) {
                    if(rptOptions.PrintSettings.get("html_in_point").equals("true")) {
                        rpthtmlconf.setSizeUnit(HtmlSizeUnitEnum.POINT);
                    }
                }
                rpthtmlconf.setWrapBreakWord(Boolean.TRUE);
                //rpthtmlconf.setPageIndex(pageIndex);
                htmlExporter.setConfiguration(rpthtmlconf);
                // export report
                htmlExporter.exportReport();
                htmlExporter = null;
                SessionFolderManager.makeReadable(htmlOutput);
                htmlOutput = null;
                break;
            case pdf_file:
                preparedParams.put(JRParameter.REPORT_LOCALE, new Locale("en"));
                CoreJSFormatUtils.Formatter.setForExport(false);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                JRPdfExporter pdfExporter = new JRPdfExporter();
                pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File pdfOutput = new File(outpath.getAbsolutePath() + "/report_" + rptOptions.ViewerID + ".pdf");
                rptInfo.ReportRenderedPath += "/" + pdfOutput.getName();
                SimpleOutputStreamExporterOutput pdfdest = new SimpleOutputStreamExporterOutput(pdfOutput);
                pdfExporter.setExporterOutput(pdfdest);
                // create export configuration
                SimplePdfExporterConfiguration pdfconf = new SimplePdfExporterConfiguration();
                pdfconf.setPermissions(PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING);
                pdfExporter.setConfiguration(pdfconf);
                
                //export report
                pdfExporter.exportReport();
                pdfExporter = null;
                SessionFolderManager.makeReadable(pdfOutput);
                pdfOutput = null;                    
                break;
            case ms_doc_file:
                preparedParams.put(JRParameter.REPORT_LOCALE, new Locale("en"));
                CoreJSFormatUtils.Formatter.setForExport(false);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                JRDocxExporter docxExporter = new JRDocxExporter();
                docxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File docxOutput = new File(outpath.getAbsolutePath() + "/report_" + rptOptions.ViewerID + ".docx");
                rptInfo.ReportRenderedPath += "/" + docxOutput.getName();
                SimpleOutputStreamExporterOutput docxdest = new SimpleOutputStreamExporterOutput(docxOutput);
                docxExporter.setExporterOutput(docxdest);
                
                //export report
                docxExporter.exportReport();
                docxExporter = null;
                SessionFolderManager.makeReadable(docxOutput);
                docxOutput = null;         
                break;
            case ms_xls_file:
                preparedParams.put(JRParameter.REPORT_LOCALE, new Locale("en"));
                if(rptOptions.RptParams.containsKey("pcwf_data_only") && rptOptions.RptParams.get("pcwf_data_only").equals("true")) {
                    preparedParams.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
                }
                CoreJSFormatUtils.Formatter.setForExport(true);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File xlsxOutput = new File(outpath.getAbsolutePath() + "/report_" + rptOptions.ViewerID + ".xlsx");
                rptInfo.ReportRenderedPath += "/" + xlsxOutput.getName();
                SimpleOutputStreamExporterOutput xlsxdest = new SimpleOutputStreamExporterOutput(xlsxOutput);
                xlsxExporter.setExporterOutput(xlsxdest);
                
                //export report
                xlsxExporter.exportReport();
                xlsxExporter = null;
                SessionFolderManager.makeReadable(xlsxOutput);
                xlsxOutput = null;       
                break;
            case open_doc_file:
                preparedParams.put(JRParameter.REPORT_LOCALE, new Locale("en"));
                CoreJSFormatUtils.Formatter.setForExport(false);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                JROdtExporter odtExporter = new JROdtExporter();
                odtExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File odtOutput = new File(outpath.getAbsolutePath() + "/report_" + rptOptions.ViewerID + ".odt");
                rptInfo.ReportRenderedPath += "/" + odtOutput.getName();
                SimpleOutputStreamExporterOutput odtdest = new SimpleOutputStreamExporterOutput(odtOutput);
                odtExporter.setExporterOutput(odtdest);
                
                //export report
                odtExporter.exportReport();
                odtExporter = null;
                SessionFolderManager.makeReadable(odtOutput);
                odtOutput = null;       
                break;
            case open_calc_file:
                preparedParams.put(JRParameter.REPORT_LOCALE, new Locale("en"));
                CoreJSFormatUtils.Formatter.setForExport(true);
                jasperPrint =  JasperFillManager.fillReport(rptCompiled, preparedParams, cn);
                // create exporter instance and set input
                JROdsExporter odsExporter = new JROdsExporter();
                odsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // create destination instance and set output
                File odsOutput = new File(outpath.getAbsolutePath() + "/report_" + rptOptions.ViewerID + ".ods");
                rptInfo.ReportRenderedPath += "/" + odsOutput.getName();
                SimpleOutputStreamExporterOutput odsdest = new SimpleOutputStreamExporterOutput(odsOutput);
                odsExporter.setExporterOutput(odsdest);
                
                //export report
                odsExporter.exportReport();
                odsExporter = null;
                SessionFolderManager.makeReadable(odsOutput);
                odsOutput = null;       
                break;
            default:
                break;
        }
        
        // render exported information
        rptInfo.PageCount = jasperPrint.getPages().size();
        rptInfo.MarginTop = jasperPrint.getTopMargin();
        rptInfo.MarginRight = jasperPrint.getRightMargin();
        rptInfo.MarginBottom = jasperPrint.getBottomMargin();
        rptInfo.MarginLeft = jasperPrint.getLeftMargin();
        rptInfo.PageHeight = jasperPrint.getPageHeight();
        rptInfo.PageWidth = jasperPrint.getPageWidth();
        rptInfo.Orientation = jasperPrint.getOrientationValue().ordinal();
        jasperPrint = null;
        rptCompiled = null;
        cn.close();
        
        PrintWriter out = resp.getWriter();
        out.write(rptInfo.toJSON());
    }
    
    
    private static JasperDesign getReportFromPath(ReportOptions rptOptions, StringBuilder debugInfo) throws Exception {
        // set the URL for the file
        String rptPath = rptOptions.RptParams.get("pcwf_base_path").toString() + rptOptions.RptPath + "/" + rptOptions.RptName + ".jrxml";
        debugInfo.append("Report Path: ").append(rptPath).append(System.lineSeparator());
        // Open the report
        JasperDesign rpt = JRXmlLoader.load(rptPath);
        // set custom width and height if required
        if(rptOptions.PrintSettings.containsKey("html_paper_width")) {
            int width = Integer.parseInt(rptOptions.RptParams.get("html_paper_width").toString());
            rpt.setPageWidth(width);
        }
        if(rptOptions.PrintSettings.containsKey("html_paper_height")) {
            int height = Integer.parseInt(rptOptions.RptParams.get("html_paper_height").toString());
            rpt.setPageHeight(height);
        }
        
        return rpt;
    }
    
    private static Connection getDbProperties(ReportOptions rptOptions, JasperDesign rpt) throws Exception {
        String url = "jdbc:postgresql://" + rptOptions.DBServer + ":" + rptOptions.DBPort + "/" + rptOptions.DBName;
        Properties props = new Properties();
        props.setProperty("user", rptOptions.DBUser);
        props.setProperty("password", rptOptions.DBPass);
        Connection cn = DriverManager.getConnection(url, props);
        return cn;
    }
    
    private static HashMap getParameters(ReportOptions rptOptions, JasperDesign rpt) throws Exception {
        HashMap preparedParams = new HashMap();
        for(JRParameter param: rpt.getParameters()) {
            if(!param.isSystemDefined()) {
                if(rptOptions.RptParams.containsKey(param.getName())) {
                    Object val = ReportUtil.ChangeType(rptOptions.RptParams.get(param.getName()).toString(), param.getValueClassName(), param.getName());
                    preparedParams.put(param.getName(), val);
                } else {
                    throw new Exception("Missing report Parameter: " + param.getName());
                }
            }
        }
        return preparedParams;
    }
}
