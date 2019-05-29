package scrape;
import java.awt.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.PriorityQueue;

public class Node {
	
		LinkedHashMap<Node, Integer> weightedAdjacents = new LinkedHashMap<Node, Integer>();
		String name = null;
		Node parent = null;
		
		public void setParent(Node n) {
			parent = n;
		}
		
		public Node getParent() {
			return this.parent;
		}
		public Node(String name) {
			this.name = name;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		public String getName() {
			return name;
		}
		
		public void addEdge(Node adj, Integer w8) {
			adj.setParent(this);
			weightedAdjacents.put(adj, w8);
		}
		
		public LinkedHashMap<Node, Integer> getWeightedAdjacents() {
			return weightedAdjacents;
		}
		
		LinkedList<Entry<Node,Integer>> getSortedListofEdges()
		{
			LinkedList<Map.Entry<Node, Integer>> edgeList = new LinkedList<Map.Entry<Node, Integer>>(weightedAdjacents.entrySet()); 
			Collections.sort(edgeList, new Comparator<Map.Entry<Node, Integer>>()
		    { 
		    	public int compare(Map.Entry<Node, Integer> o1, Map.Entry<Node, Integer> o2) 
		        { 
		    		return (o1.getValue()).compareTo(o2.getValue()); 
		        } 
		    }); 
		          
		    return edgeList;
		} 
			
		
	  
		
	
}
