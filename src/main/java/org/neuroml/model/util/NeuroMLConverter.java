package org.neuroml.model.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import ncsa.hdf.object.Group;
import org.neuroml.model.Base;
import org.neuroml.model.BaseConnection;
import org.neuroml.model.BaseConnectionNewFormat;
import org.neuroml.model.BaseConnectionOldFormat;
import org.neuroml.model.ComponentType;
import org.neuroml.model.Connection;
import org.neuroml.model.ConnectionWD;
import org.neuroml.model.ContinuousConnection;
import org.neuroml.model.ContinuousConnectionInstance;
import org.neuroml.model.ContinuousConnectionInstanceW;
import org.neuroml.model.ContinuousProjection;
import org.neuroml.model.ElectricalConnection;
import org.neuroml.model.ElectricalConnectionInstance;
import org.neuroml.model.ElectricalConnectionInstanceW;
import org.neuroml.model.ElectricalProjection;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.Include;
import org.neuroml.model.IncludeType;
import org.neuroml.model.Input;
import org.neuroml.model.InputList;
import org.neuroml.model.InputW;
import org.neuroml.model.Location;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.ObjectFactory;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.hdf5.Hdf5Exception;
import org.neuroml.model.util.hdf5.NetworkHelper;
import org.neuroml.model.util.hdf5.NeuroMLHDF5Reader;

public class NeuroMLConverter
{
	protected static JAXBContext jaxb;
	
	protected static Marshaller marshaller;
	
	protected static Unmarshaller unmarshaller;	
	
	
	public NeuroMLConverter() throws NeuroMLException
	{
		ClassLoader cl = ObjectFactory.class.getClassLoader();
        try {
            jaxb = JAXBContext.newInstance("org.neuroml.model",cl);

            marshaller = jaxb.createMarshaller();		
            //marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NeuroMLNamespacePrefixMapper());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                    NeuroMLElements.NAMESPACE_URI_VERSION_2+" "+NeuroMLElements.LATEST_SCHEMA_LOCATION);

            unmarshaller = jaxb.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem creating NeuroMLConverter", ex);
        }
	}
	

	
	public Morphology xmlToMorphology(String xmlFile) throws FileNotFoundException, NeuroMLException
	{
		File f = new File(xmlFile);
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<Morphology> jbe = (JAXBElement<Morphology>) unmarshaller.unmarshal(f);

            return jbe.getValue();	
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem converting XML to Morphology in NeuroML", ex);
        }
	}
    
    public NetworkHelper loadNeuroMLOptimized(String xml) throws NeuroMLException
    {
        NeuroMLDocument nmlDoc = loadNeuroML(xml);
        NetworkHelper netHelper = new NetworkHelper(nmlDoc);
        return netHelper;
    }
    
    public NetworkHelper loadNeuroMLOptimized(File xmlOrH5File) throws NeuroMLException
    {
        return loadNeuroMLOptimized(xmlOrH5File, true);
    }

    public NetworkHelper loadNeuroMLOptimized(File xmlOrH5File, boolean includeIncludes) throws NeuroMLException
    {
        try
        {
            if (xmlOrH5File.getName().endsWith("h5") ||xmlOrH5File.getName().endsWith("hdf5"))
            {
                NeuroMLHDF5Reader h5Reader = new NeuroMLHDF5Reader();
                NetworkHelper netHelper = h5Reader.parseOptimized(xmlOrH5File, includeIncludes);
                return netHelper;
            }
            else
            {
                NeuroMLDocument nmlDoc = loadNeuroML(xmlOrH5File, includeIncludes, true);
                NetworkHelper netHelper = new NetworkHelper(nmlDoc);
                return netHelper;
            }
        }
        catch (IOException ex)
        {
            throw new NeuroMLException("Problem loading "+xmlOrH5File.getAbsolutePath(), ex);
        }
        catch (Hdf5Exception ex)
        {
            throw new NeuroMLException("Problem loading "+xmlOrH5File.getAbsolutePath(), ex);
        }
    }

	
	public NeuroMLDocument loadNeuroML(File xmlFile) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, false, false);
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, includeIncludes, true);
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes, boolean failOnMissingIncludes) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, includeIncludes, failOnMissingIncludes, new ArrayList<String>());
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes, boolean failOnMissingIncludes, ArrayList<String> alreadyIncluded) throws NeuroMLException, IOException
	{
		if (!xmlFile.exists()) 
            throw new FileNotFoundException(xmlFile.getAbsolutePath());
        
        NeuroMLDocument nmlDocument;
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(xmlFile);
            nmlDocument = jbe.getValue();
		} catch (JAXBException ex) {
            throw new NeuroMLException("Problem loading NeuroML document", ex);
        }	
        if (includeIncludes) {
            ArrayList<IncludeType> toRemove = new ArrayList<IncludeType>();
            for (IncludeType include: nmlDocument.getInclude()) 
            {
                String relativeFileLocation = include.getHref();
                File subFile = new File(xmlFile.getAbsoluteFile().getParentFile(), relativeFileLocation);
                
                if (failOnMissingIncludes && !subFile.exists()) 
                {
                    throw new NeuroMLException("Missing file included by "+xmlFile.getAbsolutePath()+": "+relativeFileLocation);
                }
                if (subFile.exists() && !alreadyIncluded.contains(subFile.getCanonicalPath())) 
                {
                    NeuroMLDocument subDoc = loadNeuroML(subFile, includeIncludes, failOnMissingIncludes, alreadyIncluded);
                    LinkedHashMap<String,Standalone> saes = getAllStandaloneElements(subDoc);
                    for (Standalone sae: saes.values()) 
                    {
                        addElementToDocument(nmlDocument, sae);
                    }
                    for (ComponentType ct: subDoc.getComponentType())
                    {
                        nmlDocument.getComponentType().add(ct);
                    }
                }
                toRemove.add(include);
            }
            for (IncludeType i: toRemove)
            {
                nmlDocument.getInclude().remove(i);
            }
        }
        
        alreadyIncluded.add(xmlFile.getCanonicalPath());
            
        return nmlDocument;	
	}
	
	public NeuroMLDocument loadNeuroML(String nml2Contents) throws NeuroMLException
	{	
        return loadNeuroML(nml2Contents, false, null);
    }
    
	public NeuroMLDocument loadNeuroML(String nml2Contents, boolean includeIncludes, File baseDirectoryForIncludes) throws NeuroMLException
    {
        return loadNeuroML(nml2Contents, includeIncludes, baseDirectoryForIncludes, new ArrayList<String>());
    }
    
	public NeuroMLDocument loadNeuroML(String nml2Contents, boolean includeIncludes, File baseDirectoryForIncludes, ArrayList<String> alreadyIncluded) throws NeuroMLException
    {
        StringReader sr = new StringReader(nml2Contents);

        NeuroMLDocument nmlDocument;
        try
        {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(sr);
            nmlDocument = jbe.getValue();
        }
        catch (JAXBException ex)
        {
            throw new NeuroMLException("Problem loading NeuroML document", ex);
        }

        if (includeIncludes)
        {
            ArrayList<IncludeType> toRemove = new ArrayList<IncludeType>();

            for (IncludeType include : nmlDocument.getInclude())
            {
                String relativeFileLocation = include.getHref();

                File subFile = new File(baseDirectoryForIncludes, relativeFileLocation);

                try
                {
                    if (!subFile.exists())
                    {
                        throw new NeuroMLException("Missing file included: " + relativeFileLocation + " (assume: " + subFile + ")");
                    }
                    else if (!alreadyIncluded.contains(subFile.getCanonicalPath()))
                    {
                        NeuroMLDocument subDoc = loadNeuroML(subFile, includeIncludes, true, alreadyIncluded);
                        LinkedHashMap<String, Standalone> saes = getAllStandaloneElements(subDoc);
                        for (Standalone sae : saes.values())
                        {
                            addElementToDocument(nmlDocument, sae);
                        }
                        for (ComponentType ct: subDoc.getComponentType())
                        {
                            nmlDocument.getComponentType().add(ct);
                        }
                    }
                }
                catch (IOException ex)
                {
                    throw new NeuroMLException("Problem loading NeuroML document", ex);
                }
                
                toRemove.add(include);
            }
            for (IncludeType i : toRemove)
            {
                nmlDocument.getInclude().remove(i);
            }
        }

        return nmlDocument;
    }
	
	public NeuroMLDocument urlToNeuroML(URL url) throws NeuroMLException
	{
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(url);
            return jbe.getValue();	
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem loading NeuroML document from URL", ex);
        }
	}
    
    private static <T extends Base> void sortElements(List<T> elements) 
    {
        Collections.sort(elements, new Comparator<T>(){
            @Override
            public int compare(T o1, T o2){
                if(o1.getId().equals(o2.getId()))
                    return 0;
                return o1.getId().compareTo(o2.getId());
            }
       });
    }
    
    public static String summary(NeuroMLDocument nmlDocument) throws NeuroMLException 
    {
        return summary(nmlDocument, false);
    }
    
    public static String summary(NeuroMLDocument nmlDocument, boolean full) throws NeuroMLException 
    {
        
        String info = new String();
        info += "*******************************************************\n";
        info += "* NeuroMLDocument: "+nmlDocument.getId()+"\n*\n";
        
        LinkedHashMap<String,Standalone> sae = getAllStandaloneElements(nmlDocument);
        String post = "";
        for (String el: sae.keySet())
        {
            Standalone s = sae.get(el);
            if (!s.getClass().getSimpleName().equals("Network")) {
                info += "*  "+s.getClass().getSimpleName()+": "+s.getId()+"\n";
                post = "*\n";
            }
        }
        info += post;
        
        for (ComponentType ct: nmlDocument.getComponentType())
        {
            info +="*  ComponentType: "+ct.getName()+"\n";
        }
        if (nmlDocument.getComponentType().size()>0)
            info +="*\n";
            
        
        for (Network network: nmlDocument.getNetwork()) {
            
            info += "*  Network: "+network.getId();
            if (network.getTemperature()!=null) 
            {
                info += " (temperature: "+network.getTemperature()+")";
            }
            info += "\n*\n";
            
            // Populations
            
            List<Population> pops = network.getPopulation();
            sortElements(pops);
            String pop_info = "";
            int tot_pops = 0, tot_cells = 0;
            for (Population population: pops)
            {
                int size;
                if (population.getInstance().size()>0)
                    size = population.getInstance().size();
                else
                    size = population.getSize();
                pop_info += "*     Population: "+population.getId()+
                    " with "+ size +" components of "+population.getComponent()+ "\n";
                tot_pops+=1;
                tot_cells+=size;
                if (population.getInstance().size()>0) 
                {
                    Location l = population.getInstance().get(0).getLocation();
                    pop_info += "*       Locations: [("+ l.getX() + ","+ l.getY() + ","+ l.getZ() + "), ...]\n";
                }
            }
            info+= "*   "+tot_cells+" cells in "+tot_pops+" populations\n"+pop_info+"*\n";
            
            // Projections
            
            List<Projection> projs = network.getProjection();
            sortElements(projs);
            List<ElectricalProjection> eprojs = network.getElectricalProjection();
            sortElements(eprojs);
            List<ContinuousProjection> cprojs = network.getContinuousProjection();
            sortElements(cprojs);
            
            String proj_info = "";
            int tot_projs = 0, tot_conns = 0;
            for (Projection projection: projs)
            {
                proj_info += "*     Projection: "+projection.getId()+
                    " from "+projection.getPresynapticPopulation() +" to "+projection.getPostsynapticPopulation()
                    +", synapse: "+projection.getSynapse()+ "\n";
                tot_projs+=1;
                
                tot_conns+=projection.getConnection().size();
                if (projection.getConnection().size()>0) 
                {
                    Connection c = projection.getConnection().get(0);
                    proj_info += "*       "+projection.getConnection().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
                
                tot_conns+=projection.getConnectionWD().size();
                if (projection.getConnectionWD().size()>0) 
                {
                    ConnectionWD c = projection.getConnectionWD().get(0);
                    proj_info += "*       "+projection.getConnectionWD().size()+" connections (wd): ["+ connectionInfo(c)+ ", ...]\n";
                }
            }
            for (ElectricalProjection projection: eprojs)
            {
                proj_info += "*     Electrical projection: "+projection.getId()+
                    " from "+projection.getPresynapticPopulation() +" to "+projection.getPostsynapticPopulation()
                    + "\n";
                tot_projs+=1;
                
                tot_conns+=projection.getElectricalConnection().size();
                if (projection.getElectricalConnection().size()>0) 
                {
                    ElectricalConnection c = projection.getElectricalConnection().get(0);
                    proj_info += "*       "+projection.getElectricalConnection().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
                
                tot_conns+=projection.getElectricalConnectionInstance().size();
                if (projection.getElectricalConnectionInstance().size()>0) 
                {
                    ElectricalConnectionInstance c = projection.getElectricalConnectionInstance().get(0);
                    proj_info += "*       "+projection.getElectricalConnectionInstance().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
                
                tot_conns+=projection.getElectricalConnectionInstanceW().size();
                if (projection.getElectricalConnectionInstanceW().size()>0) 
                {
                    ElectricalConnectionInstanceW c = projection.getElectricalConnectionInstanceW().get(0);
                    proj_info += "*       "+projection.getElectricalConnectionInstanceW().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
            }
            
            for (ContinuousProjection projection: cprojs)
            {
                proj_info += "*     Continuous projection: "+projection.getId()+
                    " from "+projection.getPresynapticPopulation() +" to "+projection.getPostsynapticPopulation()
                    + "\n";
                tot_projs+=1;
                
                tot_conns+=projection.getContinuousConnection().size();
                if (projection.getContinuousConnection().size()>0) 
                {
                    ContinuousConnection c = projection.getContinuousConnection().get(0);
                    proj_info += "*       "+projection.getContinuousConnection().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
                
                tot_conns+=projection.getContinuousConnectionInstance().size();
                if (projection.getContinuousConnectionInstance().size()>0) 
                {
                    ContinuousConnection c = projection.getContinuousConnectionInstance().get(0);
                    proj_info += "*       "+projection.getContinuousConnectionInstance().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
                tot_conns+=projection.getContinuousConnectionInstanceW().size();
                if (projection.getContinuousConnectionInstanceW().size()>0) 
                {
                    ContinuousConnection c = projection.getContinuousConnectionInstanceW().get(0);
                    proj_info += "*       "+projection.getContinuousConnectionInstanceW().size()+" connections: ["+ connectionInfo(c)+ ", ...]\n";
                }
            }
            info+= "*   "+tot_conns+" connections in "+tot_projs+" projections\n"+proj_info+"*\n";
            
            // InputLists
            
            List<InputList> ils = network.getInputList();
            sortElements(ils);
            String il_info = "";
            int tot_ils = 0, tot_is = 0;
            for (InputList il: ils)
            {
                il_info += "*     InputList: "+il.getId()+" to "+il.getPopulation()+", component "+il.getComponent()+ "\n";
                tot_ils+=1;
                tot_is+=il.getInput().size();
                if (il.getInput().size()>0) 
                {
                    Input i = il.getInput().get(0);
                    il_info += "*      "+il.getInput().size()+" inputs: ["+ inputInfo(i)+ ", ...]\n";
                    
                }
                tot_is+=il.getInputW().size();
                if (il.getInputW().size()>0) 
                {
                    InputW i = il.getInputW().get(0);
                    il_info += "*      "+il.getInputW().size()+" inputs: ["+ inputInfo(i)+ ", ...]\n";
                    
                }
            }
            info+= "*   "+tot_is+" inputs in "+tot_ils+" input lists\n"+il_info+"*\n";
        }
        info += "*******************************************************\n";
        
        return info;
    }
    
    public static int getPreCellId(BaseConnection c)
    {
        if (c instanceof BaseConnectionOldFormat)
            return getCellId(((BaseConnectionOldFormat)c).getPreCellId());
        else
            return getCellId(((BaseConnectionNewFormat)c).getPreCell());
            
    }
    
    public static int getPostCellId(BaseConnection c)
    {
        if (c instanceof BaseConnectionOldFormat)
            return getCellId(((BaseConnectionOldFormat)c).getPostCellId());
        else
            return getCellId(((BaseConnectionNewFormat)c).getPostCell());
    }
    
    private static int getCellId(String path)
    {
        try
        {
            return Integer.parseInt(path);
        }
        catch (NumberFormatException ne)
        {
            return Integer.parseInt(path.split("/")[2]);
        }
    }
    
    
    private static String formatDelay(String delay)
    {
        if (delay==null)
            return "???";
        
        float factor = 1;
        if (delay.endsWith("ms")) 
            delay = delay.substring(0, delay.length()-2);
        if (delay.endsWith("s")) 
        {
            delay = delay.substring(0, delay.length()-1);
            factor = 0.001f;
        }
        return Float.parseFloat(delay)*factor+" ms";
    }
    
    public static String connectionInfo(BaseConnectionOldFormat c)
    {
        String weight = "";
        String delay = "";
        if (c instanceof ConnectionWD)
        {
            weight = " weight: "+((ConnectionWD)c).getWeight();
            delay = " delay: "+formatDelay(((ConnectionWD)c).getDelay());
        }
        return "(Connection "+c.getId()+": "+getPreCellId(c)
            +":"+c.getPreSegmentId()
            +"("+c.getPreFractionAlong()
            +") -> "+getPostCellId(c)
            +":"+c.getPostSegmentId()
            +"("+c.getPostFractionAlong()
            +")"
            + weight
            + delay
            +"))";
    }
    
    
    public static String connectionInfo(BaseConnectionNewFormat c)
    {
        String type = null;
        String more ="";
        if (c instanceof ElectricalConnection)
        {
            type = "Electrical connection";
            more += " synapse: "+((ElectricalConnection)c).getSynapse();
        }
        if (c instanceof ElectricalConnectionInstance)
        {
            type = "Electrical connection (Instance based)";
        }
        if (c instanceof ElectricalConnectionInstanceW)
        {
            type = "Electrical connection (Instance based & weight)";
            more += ", weight: "+((ElectricalConnectionInstanceW)c).getWeight();
        }
        if (c instanceof ContinuousConnection)
        {
            type = "Continuous connection";
            ContinuousConnection cc = (ContinuousConnection)c;
            more += " pre comp: "+cc.getPreComponent()+", post comp: "+cc.getPostComponent();
        }
        if (c instanceof ContinuousConnectionInstance)
        {
            type = "Continuous connection (Instance based)";
        }
        if (c instanceof ContinuousConnectionInstanceW)
        {
            type = "Continuous connection (Instance based & weight)";
            more += ", weight: "+((ContinuousConnectionInstanceW)c).getWeight();
        }
        
        
        return "("+type+" "+c.getId()+": "+getPreCellId(c)
            +":"+c.getPreSegment()
            +"("+c.getPreFractionAlong()
            +") -> "+getPostCellId(c)
            +":"+c.getPostSegment()
            +"("+c.getPostFractionAlong()
            +")"+more+")";
    }
    
    public static int getTargetCellId(Input i)
    {
        return getCellId(i.getTarget());
    }
    
    public static int getSegmentId(Input i)
    {
        return (i.getSegmentId()!=null ? i.getSegmentId() : 0);
    }
    public static float getFractionAlong(Input i)
    {
        return (float)(i.getFractionAlong()!=null ? i.getFractionAlong() : 0.5);
    }
    
    private static String inputInfo(Input i)
    {
        String weightTag = "";
        String weightInfo = "";
        if (i instanceof InputW)
        {
            InputW iw = (InputW)i;
            weightTag = " (weight)";
            weightInfo = ", weight: "+iw.getWeight();
        }
           
        return "(Input"+weightTag+" "+i.getId()
            +": "+getTargetCellId(i)
            +":"+getSegmentId(i)
            +"("+getFractionAlong(i)
            +")"+weightInfo+")";
    }
	
	public static LinkedHashMap<String,Standalone> getAllStandaloneElements(NeuroMLDocument nmlDocument) throws NeuroMLException
    {
        LinkedHashMap<String,Standalone> elements = new LinkedHashMap<String,Standalone>();
        Class<?> c = NeuroMLDocument.class;
        
        XmlType annot = c.getAnnotation(XmlType.class);
        
        //System.out.println("Checking: "+annot);
        HashMap<String, List<Standalone>> output = new HashMap<String, List<Standalone>>();
        for (Method m: c.getDeclaredMethods()) {
            String elementName = m.getName().substring(3,4).toLowerCase()+m.getName().substring(4);
            
            // TODO: investigate why these are needed...
            if (elementName.startsWith("iF"))
                elementName = "if"+elementName.substring(2);
            if (elementName.startsWith("hH"))
                elementName = "hh"+elementName.substring(2);
            if (elementName.startsWith("eIF"))
                elementName = "eif"+elementName.substring(3);
            
            //System.out.println("----\nM: "+m.toString()+", "+elementName);
            try {
                m.setAccessible(true);
                if (m.toString().contains("List")) {
                    Object o = m.invoke(nmlDocument, (Object[])null);
                    //System.out.format("%s() returned %s\n", m, o.toString());
                    if (o instanceof List && !elementName.equals("include") && !elementName.equals("componentType"))
                    {
                        try {
                            List<Standalone> list = (List<Standalone>)o;
                            //if (!list.isEmpty())
                            //    System.out.println("Add it: "+elementName+", "+list.size());
                            output.put(elementName, list);

                        } catch (ClassCastException cce) {
                            //System.out.println("Failure: "+cce);
                        }
                    }
                    else
                    {
                        //System.out.println("Ignore it...");
                    }
                }

            } catch (IllegalAccessException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            } catch (InvocationTargetException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            }
            
        }
        //System.out.println("found: "+output.values());
        for (String element: annot.propOrder()) {
            //System.out.println("el: "+element);
            if (output.containsKey(element))
            {
                List<Standalone> list = output.get(element);
                for (Standalone s: list) {
                    if (elements.containsKey(s.getId()))
                    {
                        throw new NeuroMLException("Repeated top level (standalone element) Id: "+elements.get(s.getId())+" and "+s);
                    }
                    elements.put(s.getId(), s);
                }
            }
        }
        //System.out.println("found: "+elements.values());
	    
        return elements;
    }
    
	public static void addElementToDocument(NeuroMLDocument nmlDocument, Standalone nmlElement) throws NeuroMLException
    {
        Class<?> c = NeuroMLDocument.class;
        
        String elType = nmlElement.getClass().getSimpleName();
        //System.out.println("Checking: "+c.getDeclaredMethods()+", adding: "+elType);
        for (Method m: c.getDeclaredMethods()) {
            try {
                if (m.getName().startsWith("get")) {
                    m.setAccessible(true);
                    Object o = m.invoke(nmlDocument, (Object[])null);
                    //System.out.format("%s returned %s, %s, %s\n", m, o.toString(), o.getClass(), m.getName());
                    String expected = "get"+elType;
                    if (m.getName().equalsIgnoreCase(expected)) {
                        //System.out.println("Adding...");
                        ArrayList list = (ArrayList)o;
                        list.add(nmlElement);
                    }
                }
                
            } catch (IllegalAccessException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            } catch (InvocationTargetException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            }
            
        }
	    
    }

    /*
     * TODO: Needs to be made much more efficient
     */
	public String neuroml2ToXml(NeuroMLDocument nml2) throws NeuroMLException
	{
		JAXBElement<NeuroMLDocument> jbc =
			new JAXBElement<NeuroMLDocument>(new QName("neuroml"),
					NeuroMLDocument.class,
					                    nml2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			marshaller.marshal(jbc, baos);
		} catch (JAXBException e) {
			throw new NeuroMLException(e);
		}

        String withNs = baos.toString();
        String correctNs = withNs.replaceAll(NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE+":", "");
        correctNs = correctNs.replaceAll(":"+NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE, "");

		return correctNs;
	}
    
    /*
     * TODO: Needs to be made much more efficient
     */
	public File neuroml2ToXml(NeuroMLDocument nml2, String filename) throws NeuroMLException, IOException
	{
        String correctNs = neuroml2ToXml(nml2);

		File f = new File(filename);
		FileOutputStream fos = new FileOutputStream(f);
        fos.write(correctNs.getBytes());
        
		fos.close();
		return f;
	}
	
    /*
     * Convert a string of XML in NeuroML2 (i.e. root element <neuroml>) to LEMS
     * i.e. root element <Lems>
     */
    public static String convertNeuroML2ToLems(String nml2string)
    {
        if (nml2string.startsWith("<?xml")) {
            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1).trim();
        }
        
        if (nml2string.startsWith("<neuroml")) {

            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1);
            // Assume </neuroml> at end...
            nml2string = nml2string.replace("</neuroml>", "");

            nml2string = "<Lems>\n\n"
                    + "    <Include file=\"NeuroMLCoreDimensions.xml\"/>\n"
                    + "    <Include file=\"Cells.xml\"/>\n"
                    + "    <Include file=\"PyNN.xml\"/>\n"
                    + "    <Include file=\"Networks.xml\"/>\n"
                    + "    <Include file=\"Simulation.xml\"/>\n\n"
                    + nml2string + "\n"
                    + "</Lems>";


        }
        return nml2string;
    }
    
    
	public static void main(String[] args) throws Exception {
        
        String fileName = "../neuroConstruct/osb/showcase/NetPyNEShowcase/NeuroML2/scaling/Balanced.net.nml";
        fileName = "src/test/resources/examples/MediumNet.net.nml";
        fileName = "../git/ca1/NeuroML2/network/PINGNet_0_1.net.nml";
        fileName = "../git/ca1/NeuroML2/network/PINGNet_0_1.net.nml.h5";
        //fileName = "src/test/resources/examples/complete.nml";
		NeuroMLConverter nmlc = new NeuroMLConverter();
        
    	NetworkHelper nmlDocument = nmlc.loadNeuroMLOptimized(new File(fileName), true);
       
        System.out.println("Loaded: \n"+NeuroMLConverter.summary(nmlDocument.getNeuroMLDocument()));
        
        
        /*
        fileName = "../neuroConstruct/osb/showcase/NetPyNEShowcase/NeuroML2/scaling/Balanced.net.nml.h5";
        
    	NeuroMLHDF5Reader nmlH5 = new NeuroMLHDF5Reader();
        
        nmlDocument = nmlH5.parse(new File(fileName), false);
       
        System.out.println("Loaded: \n"+NeuroMLConverter.summary(nmlDocument));
        
        nmlDocument = nmlH5.parse(new File(fileName), true);
       
        System.out.println("Loaded: \n"+NeuroMLConverter.summary(nmlDocument));*/
        
        
        
    }


}
