package org.deuce.transform.asm.loader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.FileInputStream;

import org.deuce.transform.asm.ExcludeIncludeStore;
import org.deuce.transform.asm.code.ProxyImplementation;
import org.deuce.transform.asm.code.ProxyInterfaceCode;
import org.deuce.transform.asm.storage.MethodDetails;

public class SimpleClassLoader extends ClassLoader {
    private Hashtable classes = new Hashtable();
    ArrayList<Object> remoteDetails = new ArrayList<Object>();
    public SimpleClassLoader() {
    	
    }

    /**
     * This is a simple version for external clients since they
     * will always want the class resolved before it is returned
     * to them.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * This is the required version of loadClass which is called
     * both from loadClass above and from the internal function
     * FindClassFromClass.
     */
    public synchronized Class loadClass(String className, boolean resolveIt)
    	throws ClassNotFoundException {
        Class result;
        byte  classData[];

        System.out.println("        >>>>>> Load class : "+className);

        /* Check our local cache of classes */
        result = (Class)classes.get(className);
        if (result != null) {
            System.out.println("        >>>>>> returning cached result.");
            return result;
        }

        /* Check with the primordial class loader */
        try {
            result = super.findSystemClass(className);
            System.out.println("        >>>>>> returning system class (in CLASSPATH).");
            return result;
        } catch (ClassNotFoundException e) {
            System.out.println("        >>>>>> Not a system class.");
        }

        /* Define it (parse the class file) */
		if (!ExcludeIncludeStore.exclude(className)){
			return super.findClass(className);
		}else if(className.contains("$HY$_")){	
			if(className.contains("$HY$_I")){
				byte[] bytes = ProxyInterfaceCode.getCode(remoteDetails, className);
				Class $HY$_I = super.defineClass(className, bytes, 0, bytes.length);
				classes.put(className, $HY$_I);
				return $HY$_I;		
			}else if(className.contains("$HY$_Proxy")){
				byte[] bytes = ProxyImplementation.getCode(remoteDetails, className);
				Class $HY$_P = super.defineClass(className, bytes, 0, bytes.length);
				classes.put(className, $HY$_P);
				return $HY$_P;
			}			
		}

        if (resolveIt) {
            resolveClass(result);
        }

        classes.put(className, result);
        System.out.println("        >>>>>> Returning newly loaded class.");
        return result;
    }
}
