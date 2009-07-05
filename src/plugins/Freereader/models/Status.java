package plugins.Freereader.models;

import java.util.List;

import plugins.Freereader.Freereader;
import plugins.Freereader.Store;

import com.db4o.ext.ExtObjectContainer;
import freenet.support.Logger;

/**
 * Status
 * This class is stored as DB4O Object.
 * 
 * @author Mario Volke
 */
public class Status {
	/**
	 * singleton instance
	 */
	private static Status instance = null;
	
	private transient Freereader freereader;

	/**
	 * The USK url of the uploaded freesite.
	 */
	private String freesiteUrl;
	
	/**
	 * The number of uploaded freesites.
	 */
	private int freesitesUploaded;
	
	/**
	 * The time of the last freesite upload
	 * in milliseconds since unix epoch.
	 */
	private long lastUploaded;
	
	private Status(Freereader freereader)
	{
		reset(freereader);
	}
	
	public void reset(Freereader freereader)
	{
		// set standard values
		freesiteUrl = null;
		freesitesUploaded = 0;
		lastUploaded = -1;
	}
	
	public void setFreesiteUrl(String url)
	{
		freesiteUrl = url;
	}
	
	public String getFreesiteUrl()
	{
		return freesiteUrl;
	}
	
	public void setFreesitesUploaded(int num)
	{
		freesitesUploaded = num;
	}
	
	public int getFreesitesUploaded()
	{
		return freesitesUploaded;
	}
	
	public void setLastUploaded(long time)
	{
		lastUploaded = time;
	}
	
	public long getLastUploaded()
	{
		return lastUploaded;
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
	public static synchronized Status getInstance(Freereader freereader)
	{
		if(instance == null) {
			ExtObjectContainer db = Store.getInstance(freereader).getDb();
			
			List<Status> result = db.queryByExample(Status.class);
			if(result.size() ==  0) {
				// create new object with default values
				Logger.debug(freereader, "Create new Status object.");
				instance = new Status(freereader);
				instance.store();
			}
			else {
				if(result.size() > 1)
					Logger.error(freereader, "Multiple Status objects stored!");
				Logger.debug(freereader, "Load Status object.");
				instance = result.get(0);
			}
				
			instance.freereader = freereader;
		}
		return instance;
	}
}
