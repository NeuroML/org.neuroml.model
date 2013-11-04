package org.neuroml.model.util;

/**
 * @author matteocantarelli
 *
 */
public class UnitsFormatterUtils
{
	
	public static String getFormattedUnits(String unformattedUnit)
	{
		String formattedUnit=unformattedUnit;
		formattedUnit=formattedUnit.replace("_per_cm"," cm\u207B");
		formattedUnit=formattedUnit.replace("_per_ms"," ms\u207B\u00b9");
		formattedUnit=formattedUnit.replace("_per_mV"," mV\u207B\u00b9");
		formattedUnit=formattedUnit.replace("_per_m"," m\u207B");
		formattedUnit=formattedUnit.replace("_per_s"," s\u207B\u00b9");
        
		formattedUnit=formattedUnit.replace("2","²");
		formattedUnit=formattedUnit.replace("3","\u00b3");
        
		formattedUnit=formattedUnit.replace("ohm","Ω");
		formattedUnit=formattedUnit.replace("u","μ");
		formattedUnit=formattedUnit.replace("_"," ");
        
        System.out.println(unformattedUnit+" -> "+ formattedUnit);
		return formattedUnit;
	}

}
