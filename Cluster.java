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
	int docCount;
	long totalTermCount;// repetition counted
	
	public Cluster(String clusterDocIDFile) throws IOException {
		File cluster = new File(clusterDocIDFile);
		this.docList = new ArrayList<String>();
		if(!cluster.exists())
			System.out.println(cluster + " does not exits");
		else if(!cluster.isFile())
			System.out.println(cluster + " is not a file");
		else {
			BufferedReader bufferedReader = new BufferedReader(
												new InputStreamReader(
													new FileInputStream(cluster)));		
			String id;
			while((id = bufferedReader.readLine()) != null)
				docList.add(id);
			bufferedReader.close();
		}
		
		this.totalTermCount = 0;
		this.termClusterFreq = new HashMap<String, Long>();
	}
	
	public void put(String term, long docFreq) {
		Long key = this.termClusterFreq.get(term);
		if(key != null)
			this.termClusterFreq.put(term, key + docFreq);
		else this.termClusterFreq.put(term, docFreq);
	}	
	
	public Map<String, Long> getTermList() {
		return this.termClusterFreq;
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
