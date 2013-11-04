package org.neuroml.model.test;

import junit.framework.Assert;

import org.junit.Test;
import org.neuroml.model.util.UnitsFormatterUtils;

/**
 * @author matteocantarelli
 *
 */
public class UnitsFormatterUtilsTest
{ 

	/**
	 * Test method for {@link org.neuroml.model.util.UnitsFormatterUtils#getFormattedUnits(java.lang.String)}.
	 */
	@Test
	public void testGetFormattedUnits()
	{
        Assert.assertEquals("mS cm⁻²",UnitsFormatterUtils.getFormattedUnits("mS_per_cm2"));
        Assert.assertEquals("S m⁻²",UnitsFormatterUtils.getFormattedUnits("S_per_m2"));
        Assert.assertEquals("Ω m",UnitsFormatterUtils.getFormattedUnits("ohm_m"));
        Assert.assertEquals("kΩ cm",UnitsFormatterUtils.getFormattedUnits("kohm_cm"));
        Assert.assertEquals("μF cm⁻²",UnitsFormatterUtils.getFormattedUnits("uF_per_cm2"));
        Assert.assertEquals("mol cm⁻³",UnitsFormatterUtils.getFormattedUnits("mol_per_cm3"));
        Assert.assertEquals("μA cm⁻²",UnitsFormatterUtils.getFormattedUnits("uA_per_cm2"));

        Assert.assertEquals("cm s⁻¹",UnitsFormatterUtils.getFormattedUnits("cm_per_s"));
	}

}
