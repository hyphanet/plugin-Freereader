package plugins.Freereader;

/**
 * Version
 * 
 * @author Mario Volke
 */
public class Version {
	/** Git revision number. Only set if the plugin is compiled properly e.g. by emu. */
	private static final String gitRevision = "@custom@";
	
	/** Version number of the plugin for getRealVersion(). Increment this on making
	 * a major change, a significant bugfix etc. These numbers are used in auto-update 
	 * etc, at a minimum any build inserted into auto-update should have a unique 
	 * version. */
	public static long version = 4;
	
	static String getGitRevision() {
		return gitRevision;
	}
	
	static long getVersion() {
		return version;
	}
	
	public static void main(String[] args) {
		System.out.println("=====");
		System.out.println(gitRevision);
		System.out.println("=====");
		System.out.println(getVersion());
	}
}
