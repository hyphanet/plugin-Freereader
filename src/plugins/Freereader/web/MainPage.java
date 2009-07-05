package plugins.Freereader.web;

import java.util.Date;

import plugins.Freereader.Freereader;
import plugins.Freereader.UpdateTask;
import plugins.Freereader.models.*;

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
		HTMLNode statusContent = pageMaker.getInfobox("#", "Freereader Status", contentNode);
		statusContent.addChild("#", "Number of Freesite Uploads: " + status.getFreesitesUploaded());
		statusContent.addChild("br");
		statusContent.addChild("#", "Number of Feeds: " + feeds.getFeeds().size());
		statusContent.addChild("br");
		statusContent.addChild("#", "Creation and Upload Process: ");
		if (UpdateTask.isRunning())
			statusContent.addChild("span", "style", "color: red; font-weight: bold;", "RUNNING");
		else
			statusContent.addChild("span", "style", "color: green; font-weight: bold;", "IDLE");
		
		statusContent.addChild("br");
		statusContent.addChild("#", "Last Uploaded: "
		        + (status.getLastUploaded() == -1 ? "NEVER" : new Date(status.getLastUploaded())));
		
		// Freesite Url Box
		HTMLNode freesiteUrlContent = pageMaker.getInfobox("#", "Your Freereader Freesite", contentNode);
		if(status.getFreesiteUrl() == null) {
			freesiteUrlContent.addChild("#", "The Freereader Freesite has not been uploaded yet.");
		}
		else {
			freesiteUrlContent.addChild("#", "Your Freereader Freesite is available at:");
			freesiteUrlContent.addChild("br");
			freesiteUrlContent.addChild("a", "href", "/" + status.getFreesiteUrl(), status.getFreesiteUrl());
		}
		
		// Create Freesite Box
		HTMLNode indexContent = pageMaker.getInfobox("#", "Create and Upload Freesite", contentNode);
		HTMLNode indexForm = pr.addFormChild(indexContent, "plugins.Freereader.Freereader", "createForm");
		indexForm.addChild("input", 
		        new String[] { "name", "type", "value" },
		        new String[] { "createFreesite", "hidden", "createFreesite" });
		indexForm.addChild("input", 
		        new String[] { "type", "value" },
		        new String[] { "submit", "Create and Upload Freesite now" });
	}
}
