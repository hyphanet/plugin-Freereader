package plugins.Freereader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;

import plugins.Freereader.models.Config;
import plugins.Freereader.models.Feeds;
import plugins.Freereader.models.Status;
import plugins.Freereader.models.Feeds.Feed;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertException;
import freenet.keys.FreenetURI;
import freenet.support.Logger;
import freenet.support.io.ArrayBucket;
import freenet.support.io.BucketTools;

/**
 * UpdateTask
 * 
 * @author Mario Volke
 */
public class UpdateTask extends TimerTask 
{
	
	private static volatile boolean running = false;
	
	private Freereader freereader;
	
	public static synchronized boolean isRunning()
	{
		return running;
	}
	
	public UpdateTask(Freereader freereader)
	{
		this.freereader = freereader;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run() 
	{
		Feeds feeds = Feeds.getInstance(freereader);
		
		if(running || feeds.getFeeds().size() == 0)
			return;
		running = true;
		
		Config config = Config.getInstance(freereader);
		Status status = Status.getInstance(freereader);
		
		String themeDir = "/themes/" + config.getTheme();
		
		// fetch feeds
		List<SyndFeed> fetchedFeeds = new ArrayList<SyndFeed>();
		for(Feed f : feeds.getFeeds()) {
			try {
				URL feedUrl = new URL(f.url);
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedUrl));
				
				// convert all feeds to atom 1.0
				feed.setFeedType("atom_1.0");
				
				fetchedFeeds.add(feed);
			}
			catch (Exception e) {
				// we ignore this at the moment
			}
		}
		
		// generate filenames
		List<String> filenames = new ArrayList<String>();
		filenames.add("index.html");
		for(int i = 1; i < fetchedFeeds.size(); ++i)
			filenames.add(i + ".html");
		
		// open XSL stylesheet
		URL stylesheetUrl = getClass().getResource(themeDir + "/theme.xsl");
		HashMap<String, Object> site = new HashMap<String, Object>();
		try {		
			Source stylesheetSource = new StreamSource(new XmlReader(stylesheetUrl));
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer transformer = transFact.newTransformer(stylesheetSource);
		
			// generate a site for each feed and store the html file in a bucket
			for(int i = 0; i < fetchedFeeds.size(); ++i) {
				Element feedsElement = new Element("feeds");
				for(int j = 0; j < fetchedFeeds.size(); ++j) {
					SyndFeed feed = fetchedFeeds.get(j);
					
					Element titleElement = new Element("title");
					titleElement.setText(feed.getTitle());
					
					Element hrefElement = new Element("href");
					hrefElement.setText(filenames.get(j));
					
					Element feedElement = new Element("feed");
					feedElement.addContent(titleElement);
					feedElement.addContent(hrefElement);
					if(i == j)
						feedElement.setAttribute("selected", "true");
					
					feedsElement.addContent(feedElement);
				}
				
				SyndFeed feed = fetchedFeeds.get(i);
				SyndFeedOutput output = new SyndFeedOutput();
				
				Element feedElement;
				try {
					feedElement = output.outputJDom(feed).detachRootElement();
				} catch (FeedException e) {
					// if there is an exception then the feed element will be empty
					feedElement = new Element("feed");
				}
				
				Element currentFeedElement = new Element("current_feed");
				currentFeedElement.addContent(feedElement);
				
				Element rootElement = new Element("freereader");
				rootElement.addContent(feedsElement);
				rootElement.addContent(currentFeedElement);
				Document doc = new Document(rootElement);
	
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				
				JDOMSource source = new JDOMSource(doc);
				transformer.transform(source, new StreamResult(os));
				
				site.put(filenames.get(i), new ArrayBucket(os.toByteArray()));
			}
		} 
		catch(TransformerConfigurationException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		} 
		catch(TransformerFactoryConfigurationError e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		} 
		catch(IOException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		}
		catch(TransformerException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		}
		
		// create buckets of theme assets
		try {
			SAXBuilder builder = new SAXBuilder();			
			Document themeDoc = builder.build(getClass().getResourceAsStream(themeDir + "/theme.xml"));
			List<Element> themeAssets = themeDoc.getRootElement().getChildren("asset");
			for(Element asset : themeAssets) {
				String file = asset.getAttribute("file").getValue();
				String[] path = file.split("/");
				HashMap<String, Object> pos = site;
				for(int i = 0; i < path.length-1; ++i) {
					if(!pos.containsKey(path[i])) {
						pos.put(path[i], new HashMap<String, Object>());
					}
					pos = (HashMap<String, Object>)pos.get(path[i]);
				}
				
				ArrayBucket bucket = new ArrayBucket();
				InputStream source = getClass().getResourceAsStream(themeDir + "/" + file);
				if(source != null) {
					BucketTools.copyFrom(bucket, source, -1);
					pos.put(path[path.length-1], bucket);
				}
			}
		} 
		catch(JDOMException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		}
		catch(IOException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		}
		
		// insert freereader freesite
		HighLevelSimpleClient client = freereader.getPluginRespirator().getHLSimpleClient();
		FreenetURI uri;
		try {
			uri = client.insertManifest(
				new FreenetURI("USK@" + config.getInsertKey() + "/" + config.getBasename() + "/0"), 
				site, 
				filenames.get(0));
			status.setFreesiteUrl(uri.toString());
			status.setFreesitesUploaded(status.getFreesitesUploaded() + 1);
			Date now = new Date();
			status.setLastUploaded(now.getTime());
			status.store();
			Logger.normal(this, "Inserted Freesite with uri: " + uri);
		} 
		catch(MalformedURLException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		} 
		catch(InsertException e) {
			Logger.error(this, e.toString());
			e.printStackTrace();
			running = false;
			return;
		}
		
		running = false;
	}

}
