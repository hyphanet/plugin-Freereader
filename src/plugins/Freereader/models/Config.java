package plugins.Freereader.models;

import java.util.List;

import plugins.Freereader.Freereader;
import plugins.Freereader.Store;

import com.db4o.ext.ExtObjectContainer;

import freenet.pluginmanager.*;
import freenet.client.HighLevelSimpleClient;
import freenet.keys.FreenetURI;
import freenet.support.Logger;

/**
 * Config
 * This class is stored as DB4O Object.
 * 
 * @author Mario Volke
 */
public class Config 
{
	/**
	 * singleton instance
	 */
	private static Config instance = null;
	
	private transient Freereader freereader;

	/**
	 * Interval between updates of Freereader Freesite in seconds.
	 */
	private int updateInterval;
	
	private String insertKey;
	private String requestKey;
	
	/**
	 * The basename used to construct the URI for the freesite.
	 */
	private String basename;
	
	/**
	 * The name of the selected theme.
	 */
	private String theme;
	
	private Config(Freereader freereader)
	{
		reset(freereader);
	}
	
	public void reset(Freereader freereader)
	{
		// set standard values
		
		updateInterval = 1200 * 60;
		
		// generate SSK keypair
		PluginRespirator pr = freereader.getPluginRespirator();
		HighLevelSimpleClient client = pr.getHLSimpleClient();
		FreenetURI[] keypair = client.generateKeyPair("");
		insertKey = keypair[0].toString();
		requestKey = keypair[1].toString();
		
		// seperate only the keys
		insertKey = insertKey.substring(4, insertKey.length()-1);
		requestKey = requestKey.substring(4, requestKey.length()-1);
		
		basename = "freereader";
		theme = "default";
	}
	
	public int getUpdateInterval()
	{
		return updateInterval;
	}
	
	public void setUpdateInterval(int interval)
	{
		updateInterval = interval;
	}
	
	public String getTheme()
	{
		return theme;
	}
	
	public void setTheme(String theme)
	{
		this.theme = theme;
	}
	
	public String getInsertKey()
	{
		return insertKey;
	}
	
	public void setInsertKey(String key)
	{
		insertKey = key;
	}
	
	public String getRequestKey()
	{
		return requestKey;
	}
	
	public void setRequestKey(String key)
	{
		requestKey = key;
	}
	
	public String getBasename()
	{
		return basename;
	}
	
	public void setBasename(String name)
	{
		basename = name;
	}
	
	/**
	 * Store object into database
	 */
	public void store()
	{
		ExtObjectContainer db = Store.getInstance(freereader).getDb();
		
		synchronized(db.lock()) {
			try {
				db.store(this);
			}
			catch(RuntimeException e) {
				throw e;
			}
		}
	}
	
	/**
	 * Implementation of the singleton pattern.
	 * Load object from database or create object with default values.
	 * The method has to be synchronized in order to be thread-safe.
	 */
	public static synchronized Config getInstance(Freereader freereader)
	{
		if(instance == null) {
			ExtObjectContainer db = Store.getInstance(freereader).getDb();
			
			List<Config> result = db.queryByExample(Config.class);
			if(result.size() ==  0) {
				// create new object with default values
				Logger.debug(freereader, "Create new Config object.");
				instance = new Config(freereader);
				instance.store();
			}
			else {
				if(result.size() > 1)
					Logger.error(freereader, "Multiple Config objects stored!");
				Logger.debug(freereader, "Load Config object.");
				instance = result.get(0);
			}
				
			instance.freereader = freereader;
		}
		return instance;
	}
}
