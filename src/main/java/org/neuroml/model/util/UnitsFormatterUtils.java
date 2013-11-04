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
		formattedUnit=formattedUnit.replace("_per_","/");
		formattedUnit=formattedUnit.replace("_","/");
		formattedUnit=formattedUnit.replace("2","²");
		formattedUnit=formattedUnit.replace("ohm","Ω");
		formattedUnit=formattedUnit.replace("u","μ");
		return formattedUnit;
	}

}
