/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import com.google.gson.Gson;

/**
 *
 * @author girish
 */
public class ReportInfo {
    public String ReportRenderedPath = "";
    public int PageCount = 0;
    public long PageHeight = 0;
    public long PageWidth = 0;
    public long MarginLeft = 0;
    public long MarginRight = 0;
    public long MarginTop = 0;
    public long MarginBottom = 0;
    public int Orientation = 0;
    
    public String toJSON() {
        Gson gson = new Gson();
        String result = gson.toJson(this);
        return result;
    }
}
