import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.DirectoryReader;

public class TermExtractor {
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;
	private long totalTermCount;
	private static float LAMDA = 0.99f;
	private Map<String, Integer> IDFMap;
	public static void main(String[] args) throws IOException, ParseException {
		TermExtractor termExtractor = new TermExtractor("clusterIndex");
		List<TermStat> keyTerms = termExtractor.extractTerms("Religion.txt", 10);
		System.out.println("The key terms that characterize the cluster is: " + keyTerms);
	}
	
	public TermExtractor(String indexDirectory) throws IOException {
		this.indexReader = DirectoryReader.open(
									FSDirectory.open(
										Paths.get(indexDirectory)));
		this.indexSearcher = new IndexSearcher(this.indexReader);
		this.totalTermCount = this.indexReader.getSumTotalTermFreq("contents");
		this.IDFMap = new HashMap<String, Integer>();
	}
	
	public List<TermStat> extractTerms(String clusterFile, int termNo) throws IOException {
		// 1. create a Cluster
		Cluster cluster = new Cluster(clusterFile);
		// 2. Search document by id one by one and 
		// scan over each term of the document
		for(String ID : cluster.getDocID()) {
			int docID = retrieveDocument(ID);
			analyzeDocument(docID, cluster);
		}
		
		// 3. calculate the JSD for each of the term
		Map<String, Long> termFreq = cluster.getTermList();
		Map<String, Integer> termDocClusterFreq = cluster.getTermDocMap();
		long clusterTermCount = cluster.getTotalTermCount();
		int clusterDocCount = cluster.getDocCount();
		List<TermStat> termJSDList = new ArrayList<TermStat>();
		List<TermStat> termCWList = new ArrayList<TermStat>();
		for(String term : termFreq.keySet()) {
			float JSD = calculateJSD(term, termFreq.get(term), clusterTermCount);
			float CW = calculateCentroidWeight(term, termFreq.get(term), 
													termDocClusterFreq.get(term), 
													clusterDocCount);
			termJSDList.add(new TermStat(term, JSD));
			termCWList.add(new TermStat(term, CW));
		}
		// 4. sort terms by JSD
		Collections.sort(termJSDList, new Comparator<TermStat>() {
					public int compare(TermStat o1, TermStat o2) {
						if(o1.getStat() < o2.getStat()) return 1;
						else if (o1.getStat() > o2.getStat()) return -1;
						else return 0;
					}
				});
		return termJSDList;
	} 
	
	public int retrieveDocument(String idIt) throws IOException {
		TermQuery termQuery = new TermQuery(new Term("id", idIt));
		TopDocs topDocs = indexSearcher.search(termQuery, 1);
		ScoreDoc[] hit = topDocs.scoreDocs;
		if(hit.length != 1)
			System.out.println("Error number of document whose Id is " + idIt);
		else {
			int docID = hit[0].doc;
			return docID;
		}
		return -1;
	}
	
	public void analyzeDocument(int docNo, Cluster cluster) throws IOException {
		
		Terms termVector = indexReader.getTermVector(docNo, "contents");
		if(termVector == null){
			System.out.println(docNo);
			return;
		}
			
		TermsEnum termEnum = termVector.iterator(null);
		BytesRef term = null;
		while ((term = termEnum.next()) != null) {
			String termText = term.utf8ToString();
			if(termText.length() < 2) continue;
			if(!this.IDFMap.containsKey(termText)) {
				int docFreq = indexReader.docFreq(new Term("contents", termText));
				this.IDFMap.put(termText, docFreq);
			}
			cluster.put(termText, termEnum.totalTermFreq());
		}
	}
	
	public float calculateJSD(String termText, long termCount, long docTermCount) 
			throws IOException {
		Term term = new Term("contents", termText);
		float PCollection = ((float) this.indexReader.totalTermFreq(term)) / this.totalTermCount;
		float PCluster = LAMDA * (float) termCount / docTermCount + (1 - LAMDA) * PCollection;
		float mid = 0.5f * (PCollection + PCluster);
		float sign = Math.signum(PCluster - PCollection);
		float DJS = PCluster * (float) Math.log(PCluster / mid) 
				+ sign * PCollection * (float)Math.log(PCollection / mid);
		//float DJS = PCluster * (float) Math.log(PCluster / mid) 
				//+ PCollection * (float)Math.log(PCollection / mid);
		//float DJS = PCluster - PCollection;
		return DJS;
	}
	
	public float calculateCentroidWeight(String termText, long termCount, 
										int termClusterDocFreq, long clusterDocCount) {
		float ctf = (float)termCount / clusterDocCount;
		double cdf = Math.log(termClusterDocFreq + 1);
		double idf = Math.log(indexReader.maxDoc() / (this.IDFMap.get(termText) + 1) + 1);
		return (float)(ctf * cdf * idf);
	}
	
	static class TermStat {
		private final String term;
		private float stat;
		
		public TermStat(String term, float stat) {
			this.term = term;
			this.stat = stat;
		}
		
		public String getTerm() {
			return this.term;
		}
		public float getStat() {
			return this.stat;
		}
		public void setStat(float stat) {
			this.stat = stat;
		}
	}
}
