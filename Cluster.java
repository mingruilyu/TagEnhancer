import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Cluster {	
	private List<String> docList;
	Map<String, Long> termClusterFreq;
	Map<String, Integer> termDocClusterFreq;
	int docCount;
	long totalTermCount;// repetition counted
	
	public Cluster(String clusterDocIDFile) throws IOException {
		File cluster = new File(clusterDocIDFile);
		this.docList = new ArrayList<String>();
		this.docCount = 0;
		if(!cluster.exists())
			System.out.println(cluster + " does not exits");
		else if(!cluster.isFile())
			System.out.println(cluster + " is not a file");
		else {
			BufferedReader bufferedReader = new BufferedReader(
												new InputStreamReader(
													new FileInputStream(cluster)));		
			String id;
			while((id = bufferedReader.readLine()) != null) {
				docList.add(id);
				docCount ++;
			}
			bufferedReader.close();
		}
		
		this.totalTermCount = 0;
		this.termClusterFreq = new HashMap<String, Long>();
		this.termDocClusterFreq = new HashMap<String, Integer>();
	}
	
	public void put(String term, long docFreq) {
		Long termFreq = this.termClusterFreq.get(term);
		Integer termDocCount = this.termDocClusterFreq.get(term);
		
		if(termDocCount != null)
			this.termDocClusterFreq.put(term, termDocCount + 1);
		else this.termDocClusterFreq.put(term, 1);
		
		if(termFreq != null)
			this.termClusterFreq.put(term, termFreq + docFreq);
		else this.termClusterFreq.put(term, docFreq);
	}	
	
	public Map<String, Long> getTermList() {
		return this.termClusterFreq;
	}
	
	public Map<String, Integer> getTermDocMap() {
		return this.termDocClusterFreq;
	}
	public int getDocCount() {
		return this.docCount;
	}
	
	private void countTotalTermNumber() {
		for(String term : this.termClusterFreq.keySet())
			this.totalTermCount += this.termClusterFreq.get(term);
	}
	
	public long getTotalTermCount() {
		if(this.totalTermCount == 0) 
			countTotalTermNumber();
		return totalTermCount;
	}
	
	public void incrementTotalTermCount(long count) {
		this.totalTermCount += count;
	}
	
	public List<String> getDocID() {
		return this.docList;
	}
}
