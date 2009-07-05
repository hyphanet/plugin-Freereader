package plugins.Freereader.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import plugins.Freereader.Freereader;
import plugins.Freereader.Store;

import com.db4o.ext.ExtObjectContainer;

import freenet.support.Logger;

/**
 * Feeds
 * This class is stored as DB4O Object.
 * 
 * @author Mario Volke
 */
public class Feeds 
{
	public class Feed implements Comparable<Feed>
	{
		public String url;
		public String title;
		
		public Feed(String url, String title)
		{
			this.url = url;
			this.title = title;
		}
		
		public int compareTo(Feed f) 
		{
			return title.compareTo(f.title);
		}
	}
	
	/**
	 * singleton instance
	 */
	private static Feeds instance = null;
	
	private transient Freereader freereader;
	
	private List<Feed> feeds;
	
	private Feeds()
	{
		feeds = new ArrayList<Feed>();
	}
	
	public List<Feed> getFeeds()
	{
		return feeds;
	}
	
	public boolean hasFeed(String url)
	{
		for(int i = 0; i < feeds.size(); ++i) {
			if(feeds.get(i).url.equals(url)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized void addFeed(String url, String title)
	{
		feeds.add(new Feed(url, title));
		Collections.sort(feeds);
	}
	
	public synchronized void removeFeed(String url)
	{
		for(int i = 0; i < feeds.size(); ++i) {
			if(feeds.get(i).url.equals(url)) {
				feeds.remove(i);
			}
		}
	}
	
	/**
	 * Store object into database
	 */
	public void store()
	{
		ExtObjectContainer db = Store.getInstance(freereader).getDb();
		
		synchronized(db.lock()) {
			try {
				// we need bigger update depth because of the List objects
				db.configure().updateDepth(2);
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
	public static synchronized Feeds getInstance(Freereader freereader)
	{
		if(instance == null) {
			ExtObjectContainer db = Store.getInstance(freereader).getDb();
			
			List<Feeds> result = db.queryByExample(Feeds.class);
			if(result.size() ==  0) {
				// create new object with default values
				Logger.debug(freereader, "Create new Feeds object.");
				instance = new Feeds();
				instance.store();
			}
			else {
				if(result.size() > 1)
					Logger.error(freereader, "Multiple Feeds objects stored!");
				Logger.debug(freereader, "Load Feeds object.");
				instance = result.get(0);
			}
			
			instance.freereader = freereader;
		}
		return instance;
	}
}
