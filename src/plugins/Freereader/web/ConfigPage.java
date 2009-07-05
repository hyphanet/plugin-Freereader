package plugins.Freereader.web;

import plugins.Freereader.Freereader;
import plugins.Freereader.models.Config;

import freenet.clients.http.PageMaker;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * ConfigPage
 * 
 * @author Mario Volke
 */
class ConfigPage implements WebPage 
{
	private final PageMaker pageMaker;
	private final PluginRespirator pr;
	private Config config;

	ConfigPage(Freereader freereader) 
	{
		pageMaker = freereader.getPageMaker();
		pr = freereader.getPluginRespirator();

		config = Config.getInstance(freereader);
	}

	public synchronized void processPostRequest(HTTPRequest request, HTMLNode contentNode) 
	{
		if(request.isPartSet("updateInterval")) {
			int i = request.getIntPart("updateInterval", config.getUpdateInterval() / 60);
			config.setUpdateInterval(i * 60);
		}
		if(request.isPartSet("basename")) {
			String s = request.getPartAsString("basename", 200);
			config.setBasename(s);
		}
		if(request.isPartSet("theme")) {
			String s = request.getPartAsString("theme", 200);
			config.setTheme(s);
		}
		
		config.store();
		
		pageMaker.getInfobox("infobox infobox-success", "Configuration saved", contentNode);
	}

	public void writeContent(HTTPRequest request, HTMLNode contentNode) 
	{	
		HTMLNode configContent = pageMaker.getInfobox("#", "Configuration", contentNode);
		HTMLNode configForm = pr.addFormChild(configContent, "", "configForm");
	
		configForm.addChild("div", "class", "configprefix", "Freereader Options");
		
		HTMLNode freereaderConfig = configForm.addChild("ul", "class", "config");
		
		addConfig(freereaderConfig,
		        "Update Interval (min)", "This is the interval Freereader will fetch all feeds and update your Freereader freesite.",
		        "updateInterval",
		        new String[] { "10", "20", "30", "40", "50", "60", "120", "180", "240", "300", "360", "420", "480", "540", "600", "1200", "1800", "2400"},
		        String.valueOf(config.getUpdateInterval() / 60));
		
		addConfig(freereaderConfig,
		        "Freesite Basename", "The base directory of the freesite.",
		        "basename",
		        config.getBasename());
		
		addConfig(freereaderConfig,
		        "Theme", "At the moment there's only one Freereader theme.",
		        "theme",
		        new String[] { "default"},
		        Long.toString(config.getUpdateInterval()));
		
		configForm.addChild("input",
				new String[] { "type", "value", "style" },
				new String[] { "submit", "Apply", "margin-top:10px;" });
	}

	/* THIS METHOD IS UNUSED ATM
	private void addHTML(HTMLNode configUi, String shortDesc, HTMLNode node) {
		HTMLNode li = configUi.addChild("li");
		li.addChild("span", "class", "configshortdesc", shortDesc);
		li.addChild("span", "class", "config").addChild(node);
	}
	*/
	
	private void addConfig(HTMLNode configUi, String shortDesc, String longDesc, String name, String value) 
	{
		HTMLNode li = configUi.addChild("li");
		li.addChild("span","class","configshortdesc", shortDesc);
		li.addChild("span","class","config")
			.addChild("input",
		                new String[] { "class", "type", "name", "value" },
		                new String[] { "config", "text", name, value });
		li.addChild("span", "class", "configlongdesc", longDesc);
	}
	
	private void addConfig(HTMLNode configUi, String shortDesc, String longDesc, String name, String[] values, String value) 
	{
		HTMLNode li = configUi.addChild("li");
		li.addChild("span","class","configshortdesc", shortDesc);
		HTMLNode select = li.addChild("span", "class", "config") 
		        .addChild("select",
		                new String[] { "class", "name" }, 
		                new String[] { "config", name });
		for (String v : values) {
			HTMLNode o = select.addChild("option", "value", v, v);
			if (v.equals(value))
				o.addAttribute("selected", "selected");
		}
		li.addChild("span", "class", "configlongdesc", longDesc);
	}
	
	/* THIS METHOD IS UNUSED ATM
	private void addConfig(HTMLNode configUi, String shortDesc, String longDesc, String name, String[] value) {
		StringBuilder value2 = new StringBuilder(value[0]);
		for (int i = 1; i < value.length; i++) {
			value2.append(", ");
			value2.append(value[i]);
		}

		HTMLNode li = configUi.addChild("li");
		li.addChild("span", "class", "configshortdesc", shortDesc);
		li.addChild("span", "class", "config") //
		        .addChild("input", //
		                new String[] { "class", "type", "name", "value" }, //
		                new String[] { "config", "text", name, value2.toString() });
		li.addChild("span", "class", "configlongdesc", longDesc);
	}
	*/
}
