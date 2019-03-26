package scrape;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class TouroScraper {
	
	public static LinkedHashSet<String> internalURLset = new LinkedHashSet<String>();
	public static LinkedHashSet<String> externalURLset = new LinkedHashSet<String>();
	public static LinkedHashSet<String> emailSet = new LinkedHashSet<String>();
	public static LinkedHashSet<String> techSet = new LinkedHashSet<String>();
	public static Stack<String> internalStack = new Stack<String>();
	public static int numberOfURLsSearched = 0;
	
	
	public static void main(String[]args) 
	{
		
		long start = System.nanoTime();
		crawl("https://www.touro.edu");
		long end = System.nanoTime();
		long elapsedTime = end - start;
		long minutes = (elapsedTime / 1000000000/60);
		System.out.println("TOTAL RUNNING TIME: "+minutes+" minutes");
		//print all sets
		Iterator<String> intrnl = internalURLset.iterator();
		Iterator<String> extrnl = externalURLset.iterator();
		Iterator<String> email = emailSet.iterator();
		Iterator<String> tech = techSet.iterator();
		
		System.out.println("Internal URLs:");
		while(intrnl.hasNext())
		        System.out.println(intrnl.next());
		
		System.out.println("External URLs:");
		while(extrnl.hasNext())
		        System.out.println(extrnl.next());
		
		System.out.println("Emails:");
		while(email.hasNext())
		        System.out.println(email.next());
		
		System.out.println("Technology related content:");
		while(tech.hasNext())
		        System.out.println(tech.next());
		    
	}
	
	public static void collectCSrelatedStuff(Document doc) //this might only work on webpages, not pdfs and the like. But to find a paragraph with the words is difficult.
	{	
		try 
		{
	            //i haven't found anything saying that you can combine :contains with regex, so i repeated.
				//i only ran the program searching for computer, i didnt test this improved method.
				Elements tech = doc.select("*:containsOwn(computer),*:containsOwn(computers),*:containsOwn(tech),"
						+ "*:containsOwn(technology),*:containsOwn(technological),*:containsOwn(technologically),*:containsOwn(software),"
						+ "*:containsOwn(hack),*:containsOwn(hacker),*:containsOwn(hackers),*:containsOwn(code),*:containsOwn(hackathon),"
						+ "*:containsOwn(coding),*:containsOwn(program),*:containsOwn(programmer),*:containsOwn(cyber),*:containsOwn(cyberattack),"
						+ "*:containsOwn(cyberwarfare),*:containsOwn(programming),*:containsOwn(internet),*:containsOwn(novick),*:containsOwn(fink),"
						+ "*:containsOwn(IT)");
				
				for (Element techElement : tech)
				{
					techSet.add(techElement.text());
				}
		}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
		
	}
	
	public static void collectURLs(Document doc) 
	{
		try 
		{
			Elements links = doc.select("a[href]");
		
			for (Element link : links) 
	        {
	        	String linkText = link.attr("abs:href");
	        	
	        	if(linkText.matches("https?:\\/\\/www\\.(\\w+\\.)?touro\\.edu.*"))//see comment in other class
	        	{
	        		if(!internalURLset.contains(linkText))
	        		{
	        			System.out.println("	Pushing onto stack: "+linkText);//for debugging purposes
	        			internalStack.push(linkText);
	        			internalURLset.add(linkText);
	        		}
	        	}
	        	else
	        	{
	        		externalURLset.add(linkText);
	        	}
	        }
		}
	    catch(Exception e)
		{
	    	System.out.println(e.getMessage());
		}
            
        
	}
	
	public static void collectEmails(Document doc) 
	{
		//copied regex from emailregex.com
		Pattern p = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
		try 
		{
			Matcher matcher = p.matcher(doc.text());
			while (matcher.find()) 
			{
			   emailSet.add(matcher.group());
			}
		}
		catch(Exception e) 
		{
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void crawl(String URL) 
	{   
		
		TouroFetcher tf = new TouroFetcher();
		
		String currentURL = URL;
		Document currentWebpage = tf.getURLPage(URL);
		internalStack.push(URL);
		internalURLset.add(URL);
		
		while (!internalStack.isEmpty())
	    {   
			System.out.println("Searching #"+ ++numberOfURLsSearched +": " + currentURL);
	    	collectURLs(currentWebpage);
	    	collectEmails(currentWebpage);
	    	collectCSrelatedStuff(currentWebpage);
	    	 
	    	currentURL = internalStack.pop();
	    	currentWebpage = tf.getURLPage(currentURL);
	    	
	    	
	    }
		System.out.println("finished crawling.");
	}               
	
}
