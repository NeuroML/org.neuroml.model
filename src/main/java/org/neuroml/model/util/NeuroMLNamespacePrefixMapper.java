package org.neuroml.model.util;

import java.util.*;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class NeuroMLNamespacePrefixMapper extends NamespacePrefixMapper
{
    protected final static String TEMP_NAMESPACE = "_nml2_";

	protected Map<String, String> namespaceToPrefixMap;
	
	public NeuroMLNamespacePrefixMapper()
	{		
		namespaceToPrefixMap = new HashMap<String, String>();
		namespaceToPrefixMap.put("http://morphml.org/metadata/schema", "meta");
		namespaceToPrefixMap.put("http://morphml.org/morphml/schema", "mml");
		namespaceToPrefixMap.put("http://morphml.org/channelml/schema", "cml");
		namespaceToPrefixMap.put("http://morphml.org/biophysics/schema", "bio");
		namespaceToPrefixMap.put("http://morphml.org/channelml/schema", "cml");
		namespaceToPrefixMap.put("http://morphml.org/neuroml/schema", "nml");
		namespaceToPrefixMap.put("http://morphml.org/networkml/schema", "net");
		namespaceToPrefixMap.put("http://www.neuroml.org/schema/neuroml2", TEMP_NAMESPACE);
	}
	
	
    @Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)	
	{
		if (namespaceToPrefixMap.containsKey(namespaceUri)) {
			return namespaceToPrefixMap.get(namespaceUri);
		}
		return suggestion;
	}


}
