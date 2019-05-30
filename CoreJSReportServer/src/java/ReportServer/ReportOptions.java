/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

/**
 *
 * @author girish
 */
public class ReportOptions {
    public enum OutputTypeEnum {
        html_file, pdf_file, ms_doc_file, ms_xls_file, open_doc_file, open_calc_file, html_single_file
    }
    
    public String SessionID = "";
    public String ViewerID = "";
    public String ServerURL = "";
    
    public String RptPath = "";
    public String RptName = "";
    @SerializedName("HideFromJson")
    public String DBServer = "";
    @SerializedName("HideFromJson")
    public String DBName = "";
    @SerializedName("HideFromJson")
    public String DBUser = "";    
    @SerializedName("HideFromJson")
    public String DBPass = "";
    @SerializedName("HideFromJson")
    public String DBPort = "5432";
    
    public HashMap RptParams = new HashMap<String, Object>();
    public HashMap PrintSettings = new HashMap<String, Object>();
    
    public OutputTypeEnum OutputType = OutputTypeEnum.html_file; 
    
    public String toJSON() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new JsonBlocked())
                .setPrettyPrinting().create();
        String result = gson.toJson(this);
        return result;
    }
    
}
