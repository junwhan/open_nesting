package org.deuce.transform.asm.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class MagicLoader extends URLClassLoader {

	public MagicLoader(URL[] paramArrayOfURL) {
		super(paramArrayOfURL);
	}

}
