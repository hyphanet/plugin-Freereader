package plugins.Freereader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import freenet.support.Logger;
import freenet.support.io.ArrayBucket;

/**
 * ImageFetcher
 * Downloads the images referenced in feed entries so they can be inserted
 * along with the freesite, and rewrites the entries to use the local copies.
 * Images that can't be fetched are removed, as Freenet would block them anyway.
 *
 * @author nicolas
 */
public class ImageFetcher
{
	/**
	 * Directory of the freesite where the images are stored.
	 */
	public static final String IMAGE_DIR = "images";
	
	private static final int MAX_IMAGE_SIZE = 1024 * 1024;
	private static final int CONNECT_TIMEOUT = 15 * 1000;
	private static final int READ_TIMEOUT = 30 * 1000;
	private static final int MAX_REDIRECTS = 5;
	
	private static final Pattern IMG_TAG = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
	private static final Pattern SRC_ATTR = Pattern.compile("\\ssrc\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))", Pattern.CASE_INSENSITIVE);
	private static final Pattern SRCSET_ATTR = Pattern.compile("\\s(?:srcset|sizes)\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)", Pattern.CASE_INSENSITIVE);

	/**
	 * Filename -> bucket of every downloaded image.
	 */
	private final HashMap<String, Object> images = new HashMap<String, Object>();
	
	/**
	 * URL -> filename, or null if the image couldn't be fetched.
	 */
	private final HashMap<String, String> fetched = new HashMap<String, String>();
	
	public HashMap<String, Object> getImages()
	{
		return images;
	}

	@SuppressWarnings("unchecked")
	public void processFeed(SyndFeed feed)
	{
		for(SyndEntry entry : (List<SyndEntry>)feed.getEntries()) {
			// relative image URLs are resolved against the entry link
			String base = entry.getLink() != null ? entry.getLink() : feed.getLink();
			processContent(entry.getDescription(), base);
			for(SyndContent content : (List<SyndContent>)entry.getContents())
				processContent(content, base);
		}
	}

	private void processContent(SyndContent content, String base)
	{
		if(content == null || content.getValue() == null)
			return;
		content.setValue(rewriteImages(content.getValue(), base));
	}

	private String rewriteImages(String html, String base)
	{
		Matcher tags = IMG_TAG.matcher(html);
		StringBuffer out = new StringBuffer();
		while(tags.find()) {
			String tag = tags.group();
			String replacement = "";
			Matcher src = SRC_ATTR.matcher(tag);
			if(src.find()) {
				String url = src.group(1) != null ? src.group(1) : src.group(2) != null ? src.group(2) : src.group(3);
				String file = fetch(resolve(base, url.trim().replace("&amp;", "&")));
				if(file != null) {
					replacement = tag.substring(0, src.start()) + " src=\"" + IMAGE_DIR + "/" + file + "\"" + tag.substring(src.end());
					replacement = SRCSET_ATTR.matcher(replacement).replaceAll("");
				}
			}
			tags.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		tags.appendTail(out);
		return out.toString();
	}

	private static String resolve(String base, String url)
	{
		try {
			return base != null ? new URL(new URL(base), url).toString() : new URL(url).toString();
		}
		catch(MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Returns the filename of the image in IMAGE_DIR, or null if it couldn't be fetched.
	 */
	private String fetch(String url)
	{
		if(url == null)
			return null;
		if(fetched.containsKey(url))
			return fetched.get(url);

		String file = null;
		try {
			file = download(url);
		}
		catch(IOException e) {
			Logger.normal(this, "Couldn't fetch image " + url + ": " + e.getMessage());
		}
		fetched.put(url, file);
		return file;
	}
	
	private String download(String address) throws IOException
	{
		URL url = new URL(address);
		HttpURLConnection connection;
		for(int redirects = 0; ; ++redirects) {
			if(!url.getProtocol().equals("http") && !url.getProtocol().equals("https"))
				throw new IOException("unsupported protocol");
			
			connection = (HttpURLConnection)url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			// redirects are followed by hand, as HttpURLConnection doesn't follow http -> https
			connection.setInstanceFollowRedirects(false);
			
			int code = connection.getResponseCode();
			String location = connection.getHeaderField("Location");
			if(code < 300 || code >= 400 || location == null)
				break;
			connection.disconnect();
			if(redirects == MAX_REDIRECTS)
				throw new IOException("too many redirects");
			url = new URL(url, location);
		}
		
		try {
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new IOException("HTTP " + connection.getResponseCode());
			if(connection.getContentLength() > MAX_IMAGE_SIZE)
				throw new IOException("image too big");
			
			byte[] data = read(connection.getInputStream());
			String extension = extension(data);
			if(extension == null)
				throw new IOException("not a JPEG, PNG, GIF, BMP or WebP image");
			
			// name the file after its content, so an unchanged image keeps the same key
			String file = hash(data) + extension;
			images.put(file, new ArrayBucket(data));
			return file;
		}
		finally {
			connection.disconnect();
		}
	}
	
	private static byte[] read(InputStream in) throws IOException
	{
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int n;
			while((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);
				if(out.size() > MAX_IMAGE_SIZE)
					throw new IOException("image too big");
			}
			return out.toByteArray();
		}
		finally {
			in.close();
		}
	}
	
	/**
	 * Detects the format from the magic bytes, limited to those the Freenet content filter accepts.
	 */
	private static String extension(byte[] d)
	{
		if(d.length >= 3 && (d[0] & 0xff) == 0xff && (d[1] & 0xff) == 0xd8 && (d[2] & 0xff) == 0xff)
			return ".jpg";
		if(d.length >= 8 && (d[0] & 0xff) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G')
			return ".png";
		if(d.length >= 6 && d[0] == 'G' && d[1] == 'I' && d[2] == 'F' && d[3] == '8')
			return ".gif";
		if(d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F' && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P')
			return ".webp";
		if(d.length >= 2 && d[0] == 'B' && d[1] == 'M')
			return ".bmp";
		return null;
	}
	
	private static String hash(byte[] data)
	{
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
			StringBuilder hex = new StringBuilder();
			for(int i = 0; i < 16; ++i)
				hex.append(String.format("%02x", digest[i]));
			return hex.toString();
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
