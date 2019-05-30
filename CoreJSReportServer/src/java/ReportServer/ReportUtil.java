/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import groovy.util.Eval;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRReportTemplate;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author girish
 */
public class ReportUtil {
    
    public static void fixSubReportLinks(JasperDesign jasperDesign, ReportOptions rptOptions, StringBuilder debugInfo) throws Exception {
        
        // Modify Style paths
        for(JRReportTemplate jrt: jasperDesign.getTemplates()) {
            net.sf.jasperreports.engine.design.JRDesignReportTemplate jrtd = (net.sf.jasperreports.engine.design.JRDesignReportTemplate) jrt;
            String templPath = jrtd.getSourceExpression().getText();
            jrtd.setSourceExpression(new JRDesignExpression("\"" + rptOptions.RptParams.get("pcwf_base_path").toString() + "/" + templPath.substring(1)));
            debugInfo.append("Template path set: ").append(jrtd.getSourceExpression().getText()).append(System.lineSeparator());
        }
        // Modify Sub Report Paths
        JRBand jbs[] = jasperDesign.getAllBands();
        for(JRBand jb: jbs) {
            if(jb.getClass().getName().equals("net.sf.jasperreports.engine.design.JRDesignBand")) {
                List<JRChild> jrl = jb.getChildren();
                Object jres[] =  jrl.toArray();
                for(Object jre: jres) {
                    if(jre.getClass().getName().equals("net.sf.jasperreports.engine.design.JRDesignSubreport")) {
                        JRDesignSubreport sr = (JRDesignSubreport) jre;
                        debugInfo.append("Found Sub Report with expression: ").append(sr.getExpression().getText()).append(System.lineSeparator());
                        String srexpr = sr.getExpression().getText();
                        String srpath = "";
                        
                        // The report path is a parameter. Append to this absolute path as string
                        srpath = parsePathExpression(jasperDesign, rptOptions, srexpr, debugInfo);
                        srpath = srpath.replace(".jasper", ".jrxml");
                        debugInfo.append("Compiled Expression: ").append(srpath).append(System.lineSeparator());
                        String srCompiledPath = compileSubReport(srpath, rptOptions, debugInfo);
                        JRDesignExpression jrxp = new JRDesignExpression("\"" + srCompiledPath + "\"");
                        sr.setExpression(jrxp);
                        debugInfo.append("New Subreport expression set: ").append(jrxp.getText()).append(System.lineSeparator());
                    }
                }
                
            }
        }
    }
    
    
    public static String compileSubReport(String rptPath, ReportOptions rptOptions, StringBuilder debugInfo) throws Exception {
            debugInfo.append("Compiling Sub Report: ").append(rptPath).append(System.lineSeparator());
            // Open the report
            JasperDesign rpt = JRXmlLoader.load(rptPath);
            
            fixSubReportLinks(rpt, rptOptions, debugInfo);
            // Ideally this needs to be done in the session temp path. This would avoid compile time overrides.
            // todo
            JasperCompileManager.compileReportToFile(rpt, rptPath.replace(".jrxml", ".jasper"));
            debugInfo.append("Completed Sub Report Compile: ").append(rptPath).append(System.lineSeparator());
            return rptPath.replace(".jrxml", ".jasper");
    }
    
    private static String parsePathExpression(JasperDesign jasperDesign, ReportOptions rptOptions, String expresText, StringBuilder debugInfo) throws Exception {
        debugInfo.append("parsePathExpression: ").append(expresText).append(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(rptOptions.RptParams.get("pcwf_base_path").toString()).append("/\"");
        if(expresText.contains("+")) {
            String[] items = expresText.split("\\+");
            debugInfo.append("items count: ").append(items.length).append(System.lineSeparator());
            for(int i=0;i<items.length;i++) {
                sb.append("+");
                String item = items[i].trim();
                debugInfo.append("parse Item: ").append(item).append(System.lineSeparator());
                if(item.startsWith("$P{") && item.endsWith("}")) {
                    sb.append("\"").append(getParameterValue(jasperDesign, rptOptions, item)).append("\"");
                } else {
                    sb.append(item);
                }
            }
        } else if(expresText.startsWith("$P{") && expresText.endsWith("}")) {
            sb.append("+");
            sb.append("\"").append(getParameterValue(jasperDesign, rptOptions, expresText)).append("\"");
        } else {
            sb.append("+");
            sb.append(expresText);
        }
        debugInfo.append("Groovy Eval: ").append(sb.toString()).append(System.lineSeparator());
        Object result = Eval.me(sb.toString());
        
        debugInfo.append("Groovy Eval Output: ").append(result.toString()).append(System.lineSeparator());
        return result.toString();
    }
    
    private static String getParameterValue(JasperDesign jasperDesign, ReportOptions rptOptions, String paramName) throws Exception {
        // extract exact param name
        if(paramName.equals("$P{pcwf_header_template}")) {
            if(!rptOptions.RptParams.containsKey("pcwf_header_template")) {
                throw new Exception("Missing Report Parameter: 'pcwf_header_template'. Failed to generate report.");
            }
            String paramVal = rptOptions.RptParams.get("pcwf_header_template").toString();
            return paramVal;
        } else if (paramName.startsWith("$P{") && paramName.endsWith("}")){
            String pname = paramName.substring(3, paramName.length()-1);
            if(!rptOptions.RptParams.containsKey(pname)) {
                throw new Exception("Missing Report Parameter: '" + pname + "'. Failed to generate report.");
            }
            String paramVal = rptOptions.RptParams.get(pname).toString();
            return paramVal;
        }
        return "";
    }
    
    public static ReportOptions parseReportOptions(HttpServletRequest request, StringBuilder debugInfo) throws Exception {
        ReportOptions rptOptions = new ReportOptions();
        InputStream in = request.getInputStream();
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        
        Document doc = dBuilder.parse(in);
        doc.getDocumentElement().normalize();
        // get all children of root node
        NodeList nlist = doc.getFirstChild().getChildNodes();
        for(int i = 0; i <nlist.getLength(); i++) {
            Node n = nlist.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName().replaceAll("#text", "")) {
                    case "dbServer":
                        rptOptions.DBServer = n.getTextContent();
                        break;
                    case "dbName":
                        rptOptions.DBName = n.getTextContent();
                        break;
                    case "dbUser":
                        rptOptions.DBUser = n.getTextContent();
                        break;
                    case "dbPass":
                        rptOptions.DBPass = n.getTextContent();
                        break;
                    case "dbPort":
                        rptOptions.DBPort = n.getTextContent();
                    case "outputType":
                        String outType = n.getTextContent();
                        if ("html_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.html_file;
                        } else if("pdf_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.pdf_file;
                        } else if("ms_doc_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.ms_doc_file;
                        } else if("ms_xls_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.ms_xls_file;
                        } else if("open_doc_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.open_doc_file;
                        } else if("open_calc_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.open_calc_file;
                        } else if("html_single_file".equals(outType)) {
                            rptOptions.OutputType = ReportOptions.OutputTypeEnum.html_single_file;
                        }
                        break;
                    case "serverUrl":
                        rptOptions.ServerURL = n.getTextContent();
                        break;
                    case "sessionID":
                        rptOptions.SessionID = n.getTextContent();
                        break;
                    case "viewerID":
                        rptOptions.ViewerID = n.getTextContent();
                        break;
                    case "rptPath":
                        rptOptions.RptPath = n.getTextContent();
                        break;
                    case "rptName":
                        rptOptions.RptName = n.getTextContent();
                        break;
                    case "rptParams":
                        getParamsFromNode(n, rptOptions);
                        break;
                    case "printSettings":
                        getPrintSettingsFromNode(n, rptOptions);
                        break;
                }           
            }
            
        }
        return rptOptions;
    }
    
    private static void getParamsFromNode(Node pNode, ReportOptions rptOptions) {
        NodeList nlist = pNode.getChildNodes();
        HashMap rptParams = rptOptions.RptParams;
        for(int i=0; i<nlist.getLength(); i++) {
            Node n = nlist.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getAttributes().getNamedItem("name").getTextContent();
                String nodeVal = n.getTextContent();
                rptParams.put(nodeName, nodeVal);
            }
        }
    } 
    
    private static void getPrintSettingsFromNode(Node pNode, ReportOptions rptOptions) {
        NodeList nlist = pNode.getChildNodes();
        HashMap rptPrintSettings = rptOptions.PrintSettings;
        for(int i=0; i<nlist.getLength(); i++) {
            Node n = nlist.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getAttributes().getNamedItem("name").getTextContent();
                String nodeVal = n.getTextContent();
                rptPrintSettings.put(nodeName, nodeVal);
            }
        }
    }
    
    public static Object ChangeType(String value, String type, String paramName) throws Exception {        
        try {
            if(type.equals("java.util.Date")) {
                java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return formatter.parse(value);
            } else if(type.equals("java.lang.Long")) {
                return Long.parseLong(value);
            } else if(type.equals("java.lang.Boolean")) {
                if(value.equals("1")) {
                    return true;
                }
                return Boolean.parseBoolean(value);
            } else if(type.equals("java.lang.Double")) {
                return Double.parseDouble(value);
            } else if(type.equals("java.lang.Integer")) {
                return Integer.parseInt(value);
            } else if(type.equals("java.math.BigDecimal")) {
                return java.math.BigDecimal.valueOf(Double.parseDouble(value));
            } else if(type.equals("java.lang.String")) {
                return value;
            }
        } catch (Exception ex) {
            throw new Exception("Failed to convert parameter '" + paramName + "' of type '" + type + "' for value: " + value);
        }
        // none of the case statements could handle it. Therefore throw exception
        String msg = "Parameter '" + paramName + "' is of unknown type (" + type + "). Please use one of the known types (Date, Long, Boolean, Double, Integer, BigDecimal, String)";
        throw new Exception(msg);
    }
    
}

// JasperCompileManager.compileReport(new FileInputStream("/home/girish/jasper-test/TBSubReport.jrxml"))