/*
   Originally based on code from neuroConstruct: https://github.com/NeuralEnsemble/neuroConstruct

   @ Author: p.gleeson 
*/

package org.neuroml.model.util.hdf5;



@SuppressWarnings("serial")

public class Hdf5Exception extends Exception
{
    private Hdf5Exception()
    {
    }

    public Hdf5Exception(String message)
    {
        super(message);
    }

    public Hdf5Exception(String comment, Throwable t)
    {
        super(comment, t);
    }
    

}
