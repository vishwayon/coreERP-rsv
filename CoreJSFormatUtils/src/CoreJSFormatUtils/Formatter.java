/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreJSFormatUtils;


import java.math.BigDecimal;
import java.util.Locale;

/**
 *
 * @author girish
 */
public class Formatter {
    private static Locale _locale;
    private static com.ibm.icu.text.NumberFormat _nf;
    private static boolean _forExport;
    
    public static void setLocale(String locale) {
        String[] lparts = locale.split("-");
        _locale = new Locale(lparts[0], lparts[1].toUpperCase());
        _nf = com.ibm.icu.text.NumberFormat.getNumberInstance(_locale);
        _nf.setMinimumFractionDigits(2);
        _nf.setMaximumFractionDigits(2);
    }
    
    public static void setForExport(boolean val) {
        _forExport = val;
    }
    
    public static Object formatDecimal(BigDecimal val) {
        if(_locale == null) {
            setLocale("en-in");
        }
        if(_forExport) {
            return val;
        }   else {
            return _nf.format(val);
        }
    }
}
