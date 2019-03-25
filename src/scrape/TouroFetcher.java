package scrape;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;

public class TouroFetcher 
{
		private LinkedHashMap<Integer, Long> responseTimes = new LinkedHashMap<Integer, Long>();
	
		public Response getURLResponse(String URL) 
		{	
			Response response = null;
			//can only be on touro.edu's domain
			if(URL.matches("https?:\\/\\/www\\.(\\w+\\.)?touro\\.edu.*")) 
			{   
				if(determineThatRecentResponseTimesAcceptable()) 
				{
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						
						System.out.println(e1.getMessage());
					}
					try 
					{
						long start = System.nanoTime();
						response =  Jsoup.connect(URL).ignoreContentType(true).ignoreHttpErrors(true).execute();
						long end = System.nanoTime();
						long elapsedTime = end - start;
						long seconds = (elapsedTime / 1000000000);
						responseTimes.put(responseTimes.size(),  seconds);
						
					} catch (Exception e) 
					{
						System.out.println(e.getMessage());
					}
				}
				else
					System.out.println("Recent response times do not allow a response now - response is null.");
			}
			else
				System.out.println("Only touro websites are valid - response is null.");  
			
			return response;
		}
		
		public boolean determineThatRecentResponseTimesAcceptable() {
			boolean acceptable = false;
			if(responseTimes.size() >= 3)
			{
				int last = responseTimes.size() - 1;
				long first = responseTimes.get(last);
				long second = responseTimes.get(last - 1);
				long third = responseTimes.get(last - 2);
				if(first < 15 && second < 15 && third < 15)
					acceptable = true;
			}
			else
				acceptable = true;
			
			
			return acceptable;
		}
		
		public Document getURLPage(String URL) 
		{
			Response response =  getURLResponse(URL);
			Document webpage =  null;
			try 
			{
				webpage = response.parse();
			} catch (Exception e) 
			{
				System.out.println(e.getMessage());
			}
			return webpage;
		}
	
}
