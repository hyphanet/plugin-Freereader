package plugins.Freereader.web;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import plugins.Freereader.Freereader;
import plugins.Freereader.models.*;
import plugins.Freereader.models.Feeds.Feed;

import freenet.clients.http.PageMaker;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;

/**
 * FeedsPage
 * 
 * @author Mario Volke
 */
class FeedsPage implements WebPage 
{
	private final PageMaker pageMaker;
	private final PluginRespirator pr;
	private Feeds feeds;

	public FeedsPage(Freereader freereader) 
	{
		pageMaker = freereader.getPageMaker();
		pr = freereader.getPluginRespirator();

		feeds = Feeds.getInstance(freereader);
	}

	@SuppressWarnings("unchecked")
	public void processPostRequest(HTTPRequest request, HTMLNode contentNode) 
	{		
		if(request.isPartSet("action")) {
			if(request.getPartAsString("action", 20).equals("remove")) {
				// remove form submitted
				if(request.isPartSet("size")) {
					int size = request.getIntPart("size", 0);
					for(int i = 0; i < size; ++i) {
						if(request.isPartSet("remove-" + i)) {
							feeds.removeFeed(request.getPartAsString("remove-" + i, 300));
						}
					}
					
					feeds.store();
				}
			}
			else if(request.getPartAsString("action", 20).equals("opml")) {
				if(request.isPartSet("opml")) {					
					HTTPUploadedFile opml = request.getUploadedFile("opml");
					if(opml != null) {
						SAXBuilder builder = new SAXBuilder();
						try {
							Document doc = builder.build(opml.getData().getInputStream());
							XPath xpath = XPath.newInstance("//outline/@xmlUrl");
							List<Attribute> urls = xpath.selectNodes(doc);
							int count = 0;
							for(Attribute a : urls) {
								String url = a.getValue();
								if(!feeds.hasFeed(url)) {
									try {
										URL feedUrl = new URL(url);
										SyndFeedInput input = new SyndFeedInput();
										SyndFeed feed = input.build(new XmlReader(feedUrl));
										
										// everything worked fine, so add the feed to the model
										feeds.addFeed(url, feed.getTitle());
										feeds.store();
										
										count++;
									}
									catch (Exception e) {
										// ignore error here
									}
								}
							}
							
							pageMaker.getInfobox("infobox infobox-success", "Feeds Added", contentNode).
							addChild("#", "Added " + count + " feeds.");
						} 
						catch(JDOMException e) {
							pageMaker.getInfobox("infobox infobox-error", "Invalid file format", contentNode).
							addChild("#", e.getMessage());
						} 
						catch(IOException e) {
							pageMaker.getInfobox("infobox infobox-error", "Error uploading OPML-File", contentNode).
							addChild("#", e.getMessage());
						}
					}
					else {
						pageMaker.getInfobox("infobox infobox-error", "Error uploading OPML-File", contentNode).
							addChild("#", "Couldn't upload the file.");
					}
				}
				else {
					pageMaker.getInfobox("infobox infobox-error", "Error uploading OPML-File", contentNode).
						addChild("#", "You have to choose an OPML-File to upload.");
				}
			}
			else {
				// add single form submitted
				if(request.isPartSet("url")) {
					String url = request.getPartAsString("url", 300);
					// check if URL is already in feed list
					if(feeds.hasFeed(url)) {
						pageMaker.getInfobox("infobox infobox-error", "Error adding Feed", contentNode).
						addChild("#", "The URL '" + url + "' is already in feed list.");
					}
					else {
						try {
							URL feedUrl = new URL(url);
							SyndFeedInput input = new SyndFeedInput();
							SyndFeed feed = input.build(new XmlReader(feedUrl));
							
							// everything worked fine, so add the feed to the model
							feeds.addFeed(url, feed.getTitle());
							feeds.store();
							
							pageMaker.getInfobox("infobox infobox-success", "Feed Added", contentNode).
								addChild("#", "Added " + url);
						}
						catch (Exception e) {
							pageMaker.getInfobox("infobox infobox-error", "Error adding Feed", contentNode).
								addChild("#", e.getMessage());
						}
					}
				}
				else {
					pageMaker.getInfobox("infobox infobox-error", "Error adding Feed", contentNode).
						addChild("#", "Feed URL is empty.");
				}
			}
		}
	}

	public void writeContent(HTTPRequest request, HTMLNode contentNode) 
	{
		HTMLNode feedsContent = pageMaker.getInfobox("#", "Add/Remove Feeds", contentNode);
		
		// Add Single Form
		HTMLNode addForm = pr.addFormChild(feedsContent, "", "addSingleForm");
		addForm.addChild("input", 
				new String[] { "type", "name", "value" },
				new String[] { "hidden", "action", "add" });
		
		addForm.addChild("div", "class", "configprefix", "Add Single Feed");
		
		HTMLNode addList = addForm.addChild("ul", "class", "config").addChild("li");
		addList.addChild("span","class","configshortdesc", "Feed URL: ");
		addList.addChild("span","class","config").addChild("input",
				new String[] { "class", "type", "name" },
				new String[] { "config", "text", "url" });
		addList.addChild("span", "class", "configlongdesc", "A URL pointing to a RSS or Atom document.");
		addList.addChild("span", 
				new String[] { "class", "style" },
				new String[] { "configlongdesc", "color:#f00;" },
				"Attention: The feed will be requested directly over the internet (not over Freenet).");
		addForm.addChild("input",
				new String[] { "type", "value", "style" },
				new String[] { "submit", "Add Feed", "margin-top:10px; margin-bottom:20px;" });
		
		// Upload OPML Form
		HTMLNode opmlForm = pr.addFormChild(feedsContent, "", "opmlForm");
		opmlForm.addChild("input", 
				new String[] { "type", "name", "value" },
				new String[] { "hidden", "action", "opml" });
		
		opmlForm.addChild("div", "class", "configprefix", "Upload OPML-File");
		
		HTMLNode opmlList = opmlForm.addChild("ul", "class", "config").addChild("li");
		opmlList.addChild("span","class","configshortdesc", "OPML-File: ");
		opmlList.addChild("span","class","config").addChild("input",
				new String[] { "class", "type", "name" },
				new String[] { "config", "file", "opml" });
		opmlList.addChild("span", "class", "configlongdesc", "Upload an OPML-File with Feed-URLs.");
		opmlList.addChild("span", 
				new String[] { "class", "style" },
				new String[] { "configlongdesc", "color:#f00;" },
				"Attention: The feeds will be requested directly over the internet (not over Freenet).");
		opmlForm.addChild("input",
				new String[] { "type", "value", "style" },
				new String[] { "submit", "Upload OPML", "margin-top:10px; margin-bottom:20px;" });
		
		// Remove Form
		HTMLNode removeForm = pr.addFormChild(feedsContent, "", "removeForm");
		removeForm.addChild("input", 
				new String[] { "type", "name", "value" },
				new String[] { "hidden", "action", "remove" });
		
		List<Feed> feedList = feeds.getFeeds();
		
		removeForm.addChild("input", 
				new String[] { "type", "name", "value" },
				new String[] { "hidden", "size", String.valueOf(feedList.size()) });
		
		removeForm.addChild("div", "class", "configprefix", "Remove Feeds");
		HTMLNode removeList = removeForm.addChild("ul", "class", "config");
		if(feedList.size() == 0) {
			HTMLNode li = removeList.addChild("li");
			li.addChild("#", "There are no feeds yet. Please use the input field above.");
		}
		else {
			for(int i = 0; i < feedList.size(); ++i) {
				Feed f = feedList.get(i);
				HTMLNode li = removeList.addChild("li");
				li.addChild("input", 
						new String[] { "type", "name", "value", "style" },
						new String[] { "checkbox", "remove-" + i, f.url, "margin-right:15px;" });
				li.addChild("#", f.title);
			}
			
			removeForm.addChild("input",
					new String[] { "type", "value", "style" },
					new String[] { "submit", "Remove selected", "margin-top:10px;" });
		}
	}
}
