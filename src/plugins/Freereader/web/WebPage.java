package plugins.Freereader.web;

import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * WebPage
 * 
 * @author Mario Volke
 */
interface WebPage 
{
	public abstract void processPostRequest(HTTPRequest request, HTMLNode contentNode);

	public abstract void writeContent(HTTPRequest request, HTMLNode contentNode);
}