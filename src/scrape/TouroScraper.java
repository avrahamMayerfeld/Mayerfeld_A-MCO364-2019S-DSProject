package scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class TouroScraper {
	
	private static int mstNodes = 0;
	private static String MST_URL = null;
	
	private static TouroFetcher tf = new TouroFetcher();
	private static LinkedHashSet<String> internalURLset = new LinkedHashSet<String>();
	private static LinkedHashSet<String> externalURLset = new LinkedHashSet<String>();
	private static LinkedHashSet<String> emailSet = new LinkedHashSet<String>();
	private static LinkedHashSet<String> techSet = new LinkedHashSet<String>();
	private static Stack<String> internalStack = new Stack<String>();
	private static int numberOfURLsSearched = 0;
    	private static Runnable crawler = () -> crawl("https://www.touro.edu");
   
	public static void main(String[]args)  
	{    
		//ask user for input before program runs, to ensure proper functioning
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Enter the number of urls for the MST.");
		mstNodes = Integer.parseInt(keyboard.nextLine());
		System.out.println("Enter a starting url for the MST");
		MST_URL = keyboard.nextLine();
	
		Thread t1 = new Thread(crawler);
		Thread t2 = new Thread(crawler);
		Thread t3 = new Thread(crawler);
		Thread t4 = new Thread(crawler);
		Thread t5 = new Thread(crawler);
		Thread t6 = new Thread(crawler);
	
		long start = System.nanoTime();
		t1.start();
		t2.start();
		t3.start();
	        t4.start();
		t5.start();
		t6.start();
		    
		try 
		{
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	    	System.out.println("finished crawling.");
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
		
		createMST();

		    
	}
	
	public synchronized static void collectCSrelatedStuff(Document doc) //this might only work on webpages, not pdfs and the like. But to find a paragraph with the words is difficult.
	{	
		try 
		{
	            //i haven't found anything saying that you can combine :contains with regex, so i repeated.
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
	
	public static synchronized void collectURLs(Document doc) 
	{
		Elements links = doc.select("a[href]");
		
        
			try 
			{
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
							//++numberOfURLsSearched;
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
	
	public synchronized static void collectEmails(Document doc) 
	{
		//copied regex from Hirsch G's post on slack after the other one included phone numbers
		Pattern p = Pattern.compile("\\b[a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9.-]+\\b");
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
		String currentURL = URL;
		Document currentWebpage = tf.getURLPage(URL);
		internalStack.push(URL);
		internalURLset.add(URL);
		synchronized(tf)
		{
			numberOfURLsSearched++;
		}
		while (!internalStack.isEmpty())
		{   
			System.out.println("Searching #"+ numberOfURLsSearched +": " + currentURL);					
			collectURLs(currentWebpage);
			collectEmails(currentWebpage);
			collectCSrelatedStuff(currentWebpage);
			currentURL = internalStack.pop();
			currentWebpage = tf.getURLPage(currentURL);
		}
	}               
	
	/////////////////MST//////////////////MST////////////////////////MST//////////////////////////
	private static HashSet<String> urlSet = new HashSet<String>();
	private static LinkedList<String> urlQueue = new LinkedList<String>();
	private static int nodeCount = 1;
	private static LinkedHashMap<Node, Integer> vertices = new LinkedHashMap<Node, Integer>();
	
	public static void createMST() {
		LinkedHashSet <Entry<Node, Integer>> addedNodes = new LinkedHashSet<Entry<Node, Integer>>();
		
		MSTBFScrawl(MST_URL);
		
		//sort vertices on value
		LinkedList<Entry<Node,Integer>> verticesToSort = new LinkedList<>(vertices.entrySet());
        	verticesToSort.sort(Entry.comparingByValue());
        
		while(!verticesToSort.isEmpty())
		{	
			Entry<Node, Integer> currentNode = verticesToSort.removeFirst();
			boolean validEntry = true;
			//change to if the parent has the child or vice versa
			//override node hash to reflect urlname and parent
			//but must still check to see that the parent and child don't exist reversed
			
			Iterator<Entry<Node, Integer>> it = addedNodes.iterator();
			while(it.hasNext())
			{
				Node entryKey = it.next().getKey();
			    
			    	if( entryKey.equals(currentNode) ||  
			    		(entryKey.getParent().equals(currentNode) 
			    					&& entryKey.equals(currentNode.getKey().getParent())) )
			    	{
			    		validEntry = false;
			    	}
			}
			if(validEntry)
				addedNodes.add(currentNode);   
		}
		//print parent, weight, node url
		Iterator<Entry<Node, Integer>> it = addedNodes.iterator();
		while(it.hasNext())
		{
			Entry<Node, Integer> entry = it.next();
			System.out.println(entry.getKey().getParent().getName() + " --- "+entry.getValue()
			+ " --- "+entry.getKey().getName());
		}
		
	}
	
	public static void MSTBFScrawl(String URL) 
	{
		String currentURL = URL;
		
		urlQueue.add(URL);
		urlSet.add(URL);

		while (!urlQueue.isEmpty() &&  nodeCount < mstNodes)
		{   					
			MSTBFScollectURLs(currentURL);
			currentURL = urlQueue.poll();
		}
	}
	
	public static void MSTBFScollectURLs(String URL) 
	{
		
        	try 
		{
			Document currentWebpage = tf.getURLPage(URL);
			Elements links = currentWebpage.select("a[href]");
			Node node = new Node(URL);
			for (Element link : links) 
			{
				String linkText = link.attr("abs:href");
				int w8 = 0;
				if(linkText.matches("https?:\\/\\/www\\.(\\w+\\.)?touro\\.edu.*"))
				{
					if(!urlSet.contains(linkText) && nodeCount < mstNodes)
					{
						w8 = Math.abs( URL.length() - linkText.length() ) +1;
						urlQueue.add(linkText);
						urlSet.add(linkText);
						Node node2 = new Node(linkText);
						node.addEdge(node2, w8);
						node2.addEdge(node, w8);
						nodeCount ++;
						vertices.remove(node2);
						vertices.put(node2,w8);

						vertices.remove(node);
						vertices.put(node,w8);
					}
				}

			}
	    	}
        	catch(Exception e)
		{
	    		System.out.println(e.getMessage());
		}
	}
	
	
}
