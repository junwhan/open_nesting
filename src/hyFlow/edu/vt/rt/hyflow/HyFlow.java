package edu.vt.rt.hyflow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.deuce.transform.Exclude;
import org.deuce.transform.asm.loader.CustomClassLoader;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.core.cm.policy.AbstractContentionPolicy;
import edu.vt.rt.hyflow.util.network.Network;

@Exclude
public class HyFlow {

	public static <T extends AbstractDistinguishable> DirectoryManager getLocator() {
		return DirectoryManager.getManager();
	}
	
	private static AbstractContentionPolicy contentionManager;
	public static AbstractContentionPolicy  getConflictManager() {
		if(contentionManager==null)
			synchronized (HyFlow.class) {
				if(contentionManager==null)
					try {
						contentionManager = (AbstractContentionPolicy) Class.forName(System.getProperty("contentionPolicy")).newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
			}
		return contentionManager;
	}
	
	public static void readConfigurations() throws FileNotFoundException, IOException{
		Properties defaults = new Properties();
		defaults.load(HyFlow.class.getResourceAsStream("default.conf"));
		
		// rename Aleph pros.
		System.setProperty("aleph.directoryManager",System.getProperty("directoryManager", defaults.remove("directoryManager").toString()));
		System.setProperty("aleph.communicationManager",System.getProperty("communicationManager", defaults.remove("communicationManager").toString()));

		// rename Deuce pros.
		System.setProperty("org.deuce.transaction.contextClass",System.getProperty("context", defaults.get("context").toString()));

		// load HyFlow pros.
		for (Object key : defaults.keySet())
			System.setProperty(key.toString(),System.getProperty(key.toString(), defaults.getProperty(key.toString())));
	}

	public static void start(int id){
		// Init Network with node info //
		Network.init(id);
		
		// Warm-up //
		// TODO: dynamic loading of remote classes
		// getRemoteCaller(BankAccount.class);
	}
	
	public static CustomClassLoader ccl;
	public static CustomClassLoader getClassLoader(){
		if(ccl == null)
			synchronized (HyFlow.class) {
				if(ccl==null)
					try {
						ccl = (CustomClassLoader) CustomClassLoader.class.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
			}
		return ccl;
	}

	public static Class getClass(String className) {
		CustomClassLoader cl = HyFlow.getClassLoader();
		try {
			return cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}