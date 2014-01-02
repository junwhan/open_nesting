package aleph.dir;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

import edu.vt.rt.hyflow.util.io.Logger;

import aleph.GlobalObject;

public class ObjectsRegistery {

	private static Map<Object, GlobalObject> registery = new ConcurrentHashMap<Object, GlobalObject>();
	
	public static boolean regsiterObject(GlobalObject globalKey){
		Object key = globalKey.getKey();
		while(true){
			GlobalObject oldGlobalKey = registery.get(key);
			if(oldGlobalKey==null)
				synchronized (registery) {
					if(registery.containsKey(key))
						continue;	// concurrent insert, retry
					registery.put(key, globalKey);	// update the key
					break;
				}
			else
				synchronized (oldGlobalKey) {
					if(!registery.containsValue(oldGlobalKey))
						continue;	// the other thread retires
					oldGlobalKey = registery.get(key);
					if(oldGlobalKey.getVersion()>=globalKey.getVersion())
						return false;	// discard old populated messages
					registery.put(key, globalKey);	// update the key
					break;
				}
		}
		return true;
	}
	
	public static void unregsiterObject(GlobalObject key){
		registery.remove(key.getKey());
	}

	public static void dump(){
		Logger.debug(Arrays.toString(((HashMap)registery).keySet().toArray()));
	}
	
	public static GlobalObject getKey(Object key){
		return registery.get(key);
	}
}
