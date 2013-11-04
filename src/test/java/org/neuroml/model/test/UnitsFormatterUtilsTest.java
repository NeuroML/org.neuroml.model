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
		Assert.assertEquals("mS/cm²",UnitsFormatterUtils.getFormattedUnits("mS_per_cm2"));
		Assert.assertEquals("kΩ/cm",UnitsFormatterUtils.getFormattedUnits("kohm_cm"));
		Assert.assertEquals("μF/cm²",UnitsFormatterUtils.getFormattedUnits("uF_per_cm2"));
	}

}
