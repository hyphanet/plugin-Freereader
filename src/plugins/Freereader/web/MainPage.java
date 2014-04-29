package plugins.Freereader.web;

import java.util.Date;

import plugins.Freereader.Freereader;
import plugins.Freereader.UpdateTask;
import plugins.Freereader.models.Feeds;
import plugins.Freereader.models.Status;
import freenet.clients.http.PageMaker;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * MainPage
 * 
 * @author Mario Volke
 */
class MainPage implements WebPage 
{
	private final Freereader freereader;
	private final PageMaker pageMaker;
	private final PluginRespirator pr;
	private Feeds feeds;
	private Status status;

	public MainPage(Freereader freereader) 
	{
		this.freereader = freereader;
		pageMaker = freereader.getPageMaker();
		pr = freereader.getPluginRespirator();

		feeds = Feeds.getInstance(freereader);
		status = Status.getInstance(freereader);
	}

	public void processPostRequest(HTTPRequest request, HTMLNode contentNode) 
	{
		if(request.isPartSet("createFreesite")) {
			UpdateTask task = new UpdateTask(freereader);
			new Thread(task).start();
		}
	}

	public void writeContent(HTTPRequest request, HTMLNode contentNode) 
	{
		// Status Box
		HTMLNode statusContent = pageMaker.getInfobox("#", Freereader.getBaseL10n().getString("Main.Status"), contentNode);
		statusContent.addChild("#", Freereader.getBaseL10n().getString("Main.FreeSiteNumberUp") + status.getFreesitesUploaded());
		statusContent.addChild("br");
		statusContent.addChild("#", Freereader.getBaseL10n().getString("Main.FeedsNumber") + feeds.getFeeds().size());
		statusContent.addChild("br");
		statusContent.addChild("#", Freereader.getBaseL10n().getString("Main.CreationUploadProcess"));
		if (UpdateTask.isRunning())
			statusContent.addChild("span", "style", "color: red; font-weight: bold;", Freereader.getBaseL10n().getString("Common.RUNNING"));
		else
			statusContent.addChild("span", "style", "color: green; font-weight: bold;", Freereader.getBaseL10n().getString("Common.IDLE"));
		
		statusContent.addChild("br");
		statusContent.addChild("#", Freereader.getBaseL10n().getString("Main.LastUpdated")
		        + (status.getLastUploaded() == -1 ? Freereader.getBaseL10n().getString("Common.NEVER") : new Date(status.getLastUploaded())));
		
		// Freesite Url Box
		HTMLNode freesiteUrlContent = pageMaker.getInfobox("#", Freereader.getBaseL10n().getString("Main.YourFreeReaderFreesite"), contentNode);
		if(status.getFreesiteUrl() == null) {
			freesiteUrlContent.addChild("#", Freereader.getBaseL10n().getString("Main.FreereaderFreesiteNotUploaded"));
		}
		else {
			freesiteUrlContent.addChild("#", Freereader.getBaseL10n().getString("Main.AvailabelAt"));
			freesiteUrlContent.addChild("br");
			freesiteUrlContent.addChild("a", "href", "/" + status.getFreesiteUrl(), status.getFreesiteUrl());
		}
		
		// Create Freesite Box
		HTMLNode indexContent = pageMaker.getInfobox("#", Freereader.getBaseL10n().getString("Main.CreateUploadFreesite"), contentNode);
		HTMLNode indexForm = pr.addFormChild(indexContent, "plugins.Freereader.Freereader", "createForm");
		indexForm.addChild("input", 
		        new String[] { "name", "type", "value" },
		        new String[] { "createFreesite", "hidden", "createFreesite" });
		indexForm.addChild("input", 
		        new String[] { "type", "value" },
		        new String[] { Freereader.getBaseL10n().getString("Common.submit"), Freereader.getBaseL10n().getString("Main.CreateUpload") });
	}
}
