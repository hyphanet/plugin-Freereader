package plugins.Freereader.web;

import plugins.Freereader.Freereader;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.ToadletContainer;

/**
 * WebInterface
 * 
 * @author Mario Volke
 */
public class WebInterface 
{
	private final Freereader freereader;
	private final ToadletContainer toadletContainer;
	private final HighLevelSimpleClient client;
	
	private MainPageToadlet mainToadlet;
	private ConfigPageToadlet configToadlet;
	private FeedsPageToadlet feedsToadlet;

	public WebInterface(Freereader freereader, HighLevelSimpleClient client, ToadletContainer container) 
	{
		this.freereader = freereader;
		this.toadletContainer = container;
		this.client = client;
		
		configToadlet = null;
	}
	
	public void load()
	{
		freereader.getPageMaker().addNavigationCategory("/freereader/", "Freereader", "Freereader", freereader);
		
		toadletContainer.register(mainToadlet = new MainPageToadlet(client, freereader), "Freereader", "/freereader/", true, Freereader.getBaseL10n().getString("Common.Status"), Freereader.getBaseL10n().getString("Common.Status"), true, null);
		toadletContainer.register(feedsToadlet = new FeedsPageToadlet(client, freereader), "Freereader", "/freereader/feeds", true, Freereader.getBaseL10n().getString("Common.AddRemoveFeeds"), Freereader.getBaseL10n().getString("Common.AddRemoveFeeds"), true, null);
		toadletContainer.register(configToadlet = new ConfigPageToadlet(client, freereader), "Freereader", "/freereader/config", true, Freereader.getBaseL10n().getString("Common.Configuration"), Freereader.getBaseL10n().getString("Common.Configuration"), true, null);
	}
	
	public void unload()
	{
		toadletContainer.unregister(configToadlet);
		toadletContainer.unregister(feedsToadlet);
		toadletContainer.unregister(mainToadlet);
		freereader.getPageMaker().removeNavigationCategory("Freereader");
	}
}