package plugins.Freereader;

import com.db4o.Db4o;
import com.db4o.config.Configuration;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.reflect.jdk.JdkReflector;

/**
 * Store
 * 
 * @author Mario Volke
 */
public class Store 
{
	/**
	 * singleton instance
	 */
	private static Store instance = null;
	
	private static String dbFilename = "freereader.db4o";
	private ExtObjectContainer db;
	
	/**
	 * Private constructor
	 */
	private Store(ClassLoader classLoader)
	{
		Configuration cfg = Db4o.newConfiguration();
		cfg.reflectWith(new JdkReflector(classLoader));
		cfg.exceptionsOnNotStorable(true);
		
		db = Db4o.openFile(cfg, dbFilename).ext();
	}
	
	public ExtObjectContainer getDb()
	{
		return db;
	}
	
	/**
	 * Implements the singleton pattern.
	 * Use this method to get an instance of Store.
	 * The method has to be synchronized in order to be thread-safe.
	 */
	public static synchronized Store getInstance(Freereader freereader) 
	{
		if(instance == null) {
			instance = new Store(freereader.getClassLoader());
		}
		return instance;
	}
	
	/**
	 * Call this method before application shutdown.
	 */
	public static synchronized void destruct()
	{
		if(instance != null) {
			ExtObjectContainer db = instance.getDb();
			
			synchronized(db.lock()) {
				db.close();
				db = null;
			}
			
			instance = null;
		}
	}
}
