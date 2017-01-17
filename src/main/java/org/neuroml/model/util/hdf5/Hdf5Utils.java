/*
   Originally based on code from neuroConstruct: https://github.com/NeuralEnsemble/neuroConstruct

   @ Author: p.gleeson 
*/

package org.neuroml.model.util.hdf5;

import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.*;
import java.util.*;

import java.util.ArrayList;

import ncsa.hdf.hdf5lib.HDF5Constants;

import ncsa.hdf.utils.SetNatives;

public class Hdf5Utils
{

    
    public Hdf5Utils()
    {
        super();
    }
    
    /*
     * Helper function to throw error if no H5 classes found
     */
    private static void testH5classes() throws Hdf5Exception
    {
        try
        {
            ClassLoader cl  = ClassLoader.getSystemClassLoader();
            cl.loadClass("ncsa.hdf.object.FileFormat");
        }
        catch(Exception ex)
        {
            throw new Hdf5Exception("Problem finding HDF5 classes in classpath. Please ensure jars and libraries are installed correctly.\n", ex);
        }
    }
    
    public static Attribute getSimpleAttr(String name, String value, H5File h5File) throws Exception
    {
        Datatype dtype = h5File.createDatatype(Datatype.CLASS_STRING, value.length()+1, Datatype.NATIVE, Datatype.NATIVE);

        long[] attrDims = {1}; 

        Attribute attr = new Attribute(name, dtype, attrDims);
        String[] info = new String[]{value };

        attr.setValue(info); 
        
        return attr;
    }
    
    public static void addStringAttribute(Group group, String name, String value, H5File h5File) throws Exception
    {
        if (value==null)
            return;
        
        Datatype dtype = h5File.createDatatype(Datatype.CLASS_STRING, value.length()+1, Datatype.NATIVE, Datatype.NATIVE);
        
        String[] info = new String[]{value };
        Attribute attr = new Attribute(name, dtype, new long[]{1}, info);
        
        group.writeMetadata(attr);
    }

    public static H5File createH5file(File file) throws Hdf5Exception
    {
        testH5classes();
        
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null)
        {
            throw new Hdf5Exception("Cannot find HDF5 FileFormat.");
        }

        try
        {
            H5File h5File = (H5File) fileFormat.create(file.getAbsolutePath());

            if (h5File == null)
            {
                throw new Hdf5Exception("Failed to create file:"+file);
            }

            return h5File;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Error creating file: " + file + ".", ex);
        }
    }

    public static H5File openH5file(File file) throws Hdf5Exception
    {
        testH5classes();
        
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null)
        {
            throw new Hdf5Exception("Cannot find HDF5 FileFormat.");
        }

        try
        {
            H5File h5File = (H5File)fileFormat.open(file.getAbsolutePath(), FileFormat.READ);

            if (h5File == null)
            {
                throw new Hdf5Exception("Failed to open file:"+file);
            }
            
            
            open(h5File);

            return h5File;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Error creating file: " + file + ".", ex);
        }
    }

    public static void open(H5File h5File) throws Hdf5Exception
    {
        testH5classes();
        try
        {
            h5File.open();
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to open HDF5 file", ex);
        }

    }

    public static H5File openForRead(File f) throws Hdf5Exception
    {
        try
        {
            H5File h5file = new H5File(f.getAbsolutePath(), HDF5Constants.H5F_ACC_RDONLY);
            
            h5file.open();
            
            return h5file;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to open HDF5 file", ex);
        }

    }

    public static void close(H5File h5File) throws Hdf5Exception
    {
        try
        {
            h5File.close();
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to close HDF5 file", ex);
        }

    }
    
    
    public static String getFirstStringValAttr(ArrayList<Attribute> attrs, String attrName)
    {
        for (Attribute attribute : attrs) 
        {
            if (attribute.getName().equals(attrName))
            {
                /*System.out.println("---- Attribute: "+attribute+"; val: "+attribute.getValue()+
                               "; class: "+attribute.getValue().getClass()+"; dim: "+attribute.getDataDims().length+
                               "; desc: "+attribute.getType().getDatatypeDescription()
                               +"; dt class "+attribute.getType().getDatatypeClass()
                               +"; dt order "+attribute.getType().getDatatypeOrder()
                               +"; dt sign "+attribute.getType().getDatatypeSign()
                               +"; dt size "+attribute.getType().getDatatypeSize()
                               +"; dt nat "+attribute.getType().toNative());*/
            
                if (attribute.getValue() instanceof Object[])
                {
                    Object[] vals = (Object[])attribute.getValue();
                    //System.out.println("Array of length: "+vals.length);

                    return vals[0].toString();
                }
                if (attribute.getValue() instanceof Object)
                {
                    System.out.println("--- "+attribute.getValue().toString()+" for "+ attrName);
                    //Object[] gg = (Object[])attribute.getValue();
                    
                    return attribute.getValue().toString();
                }
            }

        }
        return null;
    }
    
    public static ArrayList<Attribute> parseGroupForAttributes(Group g) throws Hdf5Exception
    {
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        
        try
        {
            List attrList =  g.getMetadata();
            for(Object obj: attrList)
            {
                try
                {
                    Attribute a = (Attribute)obj;
                    attrs.add(a);
                }
                catch (ClassCastException ex)
                {
                    // Not an attribute...
                }
            }
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to parse Group for Attributes", ex);
        }

        return attrs;
    }

    
    public static ArrayList<Attribute> parseDatasetForAttributes(Dataset d) throws Hdf5Exception
    {
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        
        try
        {
            List attrList =  d.getMetadata();
            for(Object obj: attrList)
            {
                try
                {
                    Attribute a = (Attribute)obj;
                    attrs.add(a);
                }
                catch (ClassCastException ex)
                {
                    // Not an attribute...
                }
            }
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to parse Dataset for Attributes", ex);
        }
        return attrs;
    }



    public static Group getRootGroup(H5File h5File) throws Hdf5Exception
    {
        Group root = (Group) ( (javax.swing.tree.DefaultMutableTreeNode) h5File.getRootNode()).getUserObject();

        if (root == null)
        {
            throw new Hdf5Exception("Failed to obtain root group of HDF5 file: "+ h5File.getFilePath());
        }

        return root;
    }
    
    public static String parseAttribute(Attribute a, String indent, Properties p, boolean returnOnlyValueString)
    {
        String info = "";
        if (!returnOnlyValueString)
            info = indent+"  Attribute name: "+ a.getName()+", value: ";
        
        String value = null;
        if (a.getValue() instanceof Object[])
        {
            Object[] objs = (Object[])a.getValue();

            for (Object m: objs)
            {
                if (objs.length==1)
                {
                    value = m.toString();
                    info = info + value+" ";
                }
                else
                {
                    value = value + "("+m.toString()+") ";
                    info = info + "("+m.toString()+") ";
                }
                    
            }
        }
        else if (a.getValue() instanceof int[])
        {
            for (int m: (int[])a.getValue())
            {
                if (((int[])a.getValue()).length==1)
                    value = m+"";
                else
                    value = value + "("+m+") ";
                
                info = info + "("+m  +")";
            }
        }
        else if (a.getValue() instanceof double[])
        {
            for (double m: (double[])a.getValue())
            {
                if (((double[])a.getValue()).length==1)
                    value = m+"";
                else
                    value = value + "("+m+") ";
                
                info = info + "("+m  +")";
            }
        }
        else
        {
            info = info + "(??? Class: "+a.getValue().getClass()+")";
            value = a.getValue()+"";
        }
        
        if (p!=null) p.setProperty(a.getName(), value);

        
        return info.trim();
        
    }


    
    public static float[][] parse2Ddataset(Dataset d) throws Hdf5Exception
    {
        
        try
        {
            d.init();

            if (d.getDims().length!=2) return null;
            float[][] data = new float[(int)d.getDims()[0]][(int)d.getDims()[1]];
            
            float[] oneDdata = new float[(int)( d.getDims()[0] * d.getDims()[1]) ];

            Object dataObj = d.getData();
            
            if (dataObj instanceof short[])
            {
                short[] datas = (short[])dataObj;


                //oneDdata = datas;

            }
            else if (dataObj instanceof float[])
            {
                float[] dataf = (float[])dataObj;
                
                oneDdata = dataf;
                

            }
            else if (dataObj instanceof double[])
            {
                double[] datad = (double[])dataObj;
                
                

            }
            else
            {
                return null;
            }
            
            for(int x=0;x<d.getDims()[0];x++)
            {
                for(int y=0;y<d.getDims()[1];y++)
                {
                    int oneD = (int)(x * d.getDims()[1]) + y;
                    
                    
                    data[x][y] = oneDdata[oneD];
                }
            }
            

            return data;
        
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to parse2Ddataset", ex);
        }

                
    }



    public static void main(String[] args) throws IOException, Hdf5Exception
    {
        try
        {
            
			SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
            
            File f = null;
            f = new File("src/test/resources/examples/simplenet.nml.h5");


            System.out.println("Reading a HDF5 file: " + f.getCanonicalPath());

            H5File h5file = Hdf5Utils.openH5file(f);

            Hdf5Utils.open(h5file);

            System.out.println("h5file: "+h5file.getRootNode());

            Group g = Hdf5Utils.getRootGroup(h5file);


        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }


    }
}
