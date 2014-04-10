package edu.mit.d54;

public class TwitterClient {
	
	private static boolean enableTwitter = false;
	private static TwitterClient instance;

	public static boolean enableTwitter()
	{
		enableTwitter = getInstance().doEnableTwitter();
		return enableTwitter;
	}
	
	public static void disableTwitter()
	{
		enableTwitter = false;
	}
	
	public static boolean isEnabled()
	{
		return enableTwitter;
	}
	
	public static void setInstance(TwitterClient instance)
	{
		enableTwitter = false;
		TwitterClient.instance = instance;
	}
	
	protected static TwitterClient getInstance()
	{
		if (instance == null)
			instance = new TwitterClient();
		return instance;
	}
	
	public static void tweet(String tweet)
	{
		getInstance().doTweet(tweet);
	}
	
	public TwitterClient()
	{
	}
	
	protected boolean doEnableTwitter()
	{
		return true;
	}
	
	protected void doTweet(String tweet) {
		if (enableTwitter)
		{
			System.out.printf("(SIM) Tweeted: %s\n", tweet);
		}
		else
		{
			System.out.printf("(SIM - Twitter not enabled) Would have tweeted: %s\n", tweet);
		}
	}
}
