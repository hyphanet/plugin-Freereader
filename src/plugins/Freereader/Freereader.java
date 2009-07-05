package plugins.Freereader;

import plugins.Freereader.models.Config;
import plugins.Freereader.web.WebInterface;

import freenet.pluginmanager.*;
import freenet.clients.http.PageMaker;
import freenet.support.Logger;
import freenet.l10n.L10n.LANGUAGE;

import java.util.Timer;

/**
 * Freereader
 * Freenet Plugin
 * 
 * @author Mario Volke
 */
public class Freereader implements FredPlugin, FredPluginThreadless, FredPluginVersioned, FredPluginRealVersioned, FredPluginWithClassLoader, FredPluginL10n 
{
	private PluginRespirator pluginRespirator;
	private ClassLoader classLoader;
	private WebInterface webInterface;
	
	private Config config;
	
	@SuppressWarnings("unused")
	private LANGUAGE language;
	
	public static final String pluginUri = "/plugins/plugins.Freereader.Freereader";
	public static final String pluginName = "Freereader";
	
	public void runPlugin(PluginRespirator pr) 
	{
		pluginRespirator = pr;
		
		config = Config.getInstance(this);
		
		webInterface = new WebInterface(this, pluginRespirator.getHLSimpleClient(), pluginRespirator.getToadletContainer());
		webInterface.load();

		Timer timer = new Timer();
		timer.schedule(new UpdateTask(this), 1000, config.getUpdateInterval() * 1000);
	}
	
	public String getVersion() 
	{
		return Version.getVersion() + " r" + Version.getSvnRevision();
	}
	
	public long getRealVersion() 
	{
		return Version.getVersion();
	}	
	
	public PluginRespirator getPluginRespirator()
	{
		return pluginRespirator;
	}
	
	public PageMaker getPageMaker()
	{
		return pluginRespirator.getPageMaker();
	}
	
	/**
	 * Called by the node during the loading of the plugin. The <code>ClassLoader</code> which was used by the node is passed to db4o
	 * by Freereader. Db4o needs to know the <code>ClassLoader</code> which was used to create the classes of the objects it is supposed to store.
	 */
	public void setClassLoader(ClassLoader classLoader) 
	{
		this.classLoader = classLoader;
	}
	
	public ClassLoader getClassLoader() 
	{
		return classLoader;
	}
	
	public void terminate() 
	{
		webInterface.unload();
		
		Store.destruct();
		
		Logger.normal(this, pluginName + " terminated.");
	}
	
	public String getString(String key) {
		// TODO return a translated string
		return key;
	}
	
	public void setLanguage(LANGUAGE newLanguage) {
		language = newLanguage;
	}
}
