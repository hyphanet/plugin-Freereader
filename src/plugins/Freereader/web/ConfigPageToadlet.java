package plugins.Freereader.web;

import java.io.IOException;
import java.net.URI;

import plugins.Freereader.Freereader;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.RedirectException;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * ConfigPageToadlet
 * 
 * @author Mario Volke
 */
public class ConfigPageToadlet extends Toadlet 
{
	final Freereader freereader;
	
	protected ConfigPageToadlet(HighLevelSimpleClient client, Freereader freereader) 
	{
		super(client);
		this.freereader = freereader;
	}
	
	@Override
	public String path() 
	{
		return "/freereader/config";
	}

	@Override
	public String supportedMethods() 
	{
		return "GET, POST";
	}

	@Override
	public void handleGet(URI uri, final HTTPRequest request, final ToadletContext ctx) 
	throws ToadletContextClosedException, IOException, RedirectException 
	{
		ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Freereader.class.getClassLoader());
		try {
			ConfigPage page = new ConfigPage(freereader);
			PageNode p = ctx.getPageMaker().getPageNode(Freereader.pluginName, ctx);
			HTMLNode pageNode = p.outer;
			HTMLNode contentNode = p.content;
			page.writeContent(request, contentNode);
			writeHTMLReply(ctx, 200, "OK", null, pageNode.generate());
		} 
		finally {
			Thread.currentThread().setContextClassLoader(origClassLoader);
		}
	}
	
	@Override
	public void handlePost(URI uri, HTTPRequest request, final ToadletContext ctx) 
	throws ToadletContextClosedException, IOException, RedirectException 
	{
		ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Freereader.class.getClassLoader());
		try {
			PageNode p = ctx.getPageMaker().getPageNode(Freereader.pluginName, ctx);
			HTMLNode pageNode = p.outer;
			HTMLNode contentNode = p.content;
	
			WebPage page = new ConfigPage(freereader);
	
			page.processPostRequest(request, contentNode);
			page.writeContent(request, contentNode);
	
			writeHTMLReply(ctx, 200, "OK", null, pageNode.generate());
		} 
		finally {
			Thread.currentThread().setContextClassLoader(origClassLoader);
		}
	}
}
