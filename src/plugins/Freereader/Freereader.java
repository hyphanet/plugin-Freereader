package plugins.Freereader;

import java.util.Timer;

import plugins.Freereader.models.Config;
import plugins.Freereader.web.WebInterface;
import freenet.clients.http.PageMaker;
import freenet.l10n.BaseL10n;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Logger;

/**
 * Freereader
 * Freenet Plugin
 * 
 * @author Mario Volke
 */
public class Freereader implements FredPlugin, FredPluginThreadless, FredPluginVersioned, FredPluginRealVersioned, FredPluginL10n, FredPluginBaseL10n 
{
	public static final String l10nFilesBasePath = "plugins/Freereader/l10n/";
	
	public static final String l10nFilesMask = "UI_${lang}.l10n";
	public static final String l10nOverrideFilesMask = "Freereader_UI_${lang}.override.l10n";
	private static PluginL10n l10n;
	private PluginRespirator pluginRespirator;
	private WebInterface webInterface;
	
	private Config config;
		
	public static final String pluginUri = "/plugins/plugins.Freereader.Freereader";
	public static final String pluginName = "Freereader";
	
	public void runPlugin(PluginRespirator pr) 
	{
		pluginRespirator = pr;
		
		Freereader.l10n = new PluginL10n(this);
		
		config = Config.getInstance(this);
		
		webInterface = new WebInterface(this, pluginRespirator.getHLSimpleClient(), pluginRespirator.getToadletContainer());
		webInterface.load();

		Timer timer = new Timer();
		timer.schedule(new UpdateTask(this), 1000, config.getUpdateInterval() * 1000);
	}
	
	public String getVersion() 
	{
		return Version.getVersion() + " r" + Version.getGitRevision();
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
	
	public void terminate() 
	{
		webInterface.unload();
		
		Store.destruct();
		
		Logger.normal(this, pluginName + " terminated.");
	}
	
	/**
	 * This code is only used by FredPluginL10n...
	 * @param arg0
	 * @return
	 */
	@Override
	public String getString(String arg0) {
		return Freereader.getBaseL10n().getString(arg0);
	}
	
	/**
	 * This code is only called during startup or when the user
	 * selects another language in the UI.
	 * @param arg0 Language to use.
	 */
	public void setLanguage(final BaseL10n.LANGUAGE arg0) {
		Freereader.l10n = new PluginL10n(this, arg0);
	}
	
	/**
	 * BaseL10n object can be accessed statically to get L10n data from anywhere.
	 *
	 * @return L10n object.
	 */
	public static BaseL10n getBaseL10n() {
		return Freereader.l10n.getBase();
	}

	/**
	 * This is where our L10n files are stored.
	 * @return Path of our L10n files.
	 */
	@Override
	public String getL10nFilesBasePath() {
		return Freereader.l10nFilesBasePath;
	}

	/**
	 * This is the mask of our L10n files : UI_en.l10n, UI_fr.10n, ...
	 * @return Mask of the L10n files.
	 */
	@Override
	public String getL10nFilesMask() {
		return Freereader.l10nFilesMask;
	}

	/**
	 * Override L10n files are stored on the disk, their names should be explicit
	 * we put here the plugin name, and the "override" indication. Plugin L10n
	 * override is not implemented in the node yet.
	 * @return Mask of the override L10n files.
	 */
	@Override
	public String getL10nOverrideFilesMask() {
		return Freereader.l10nOverrideFilesMask;
	}

	/**
	 * Get the ClassLoader of this plugin. This is necessary when getting
	 * resources inside the plugin's Jar, for example L10n files.
	 * @return
	 */
	@Override
	public ClassLoader getPluginClassLoader() {
		return Freereader.class.getClassLoader();
	}

}
