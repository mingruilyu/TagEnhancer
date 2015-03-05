import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;


public class LabelGenerator {
	private static final int MAX_DOC_NO = 100;
	private Analyzer analyzer = new ClusterAnalyzer();
	private IndexReader indexReader;
	IndexSearcher indexSearcher;
	// keywordMap stores the pair of each keyword and
	// its weight
	private Map<String, Float> keywordMap;
	// labelMap stores the map from the label string to
	// an list of docID of the documents that contains 
	// the label
	private Map<String, ArrayList<Integer>> labelMap;

	// docMap stores for key terms score propagation
	private Map<Integer, Tuple<Integer, Float>> docMap;
	private final static String indexLocation = "wikiIndex";
	
	public static void main(String[] args) {
		List<String> keyTerms = new ArrayList<String>();
		keyTerms.add("Arabica ");
		keyTerms.add("movie ");
		keyTerms.add("poet ");
		LabelGenerator labelGenerator = new LabelGenerator(indexLocation);
		System.out.println(labelGenerator.generateLabel(keyTerms));
	}
	
	public LabelGenerator(String indexLocation) {
		try {
			indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
			indexSearcher = new IndexSearcher(this.indexReader);
			labelMap = new HashMap<String, ArrayList<Integer>>();
			keywordMap = new HashMap<String, Float>();
			docMap = new HashMap<Integer, Tuple<Integer, Float>>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<Label> generateLabel(List<String> keyTerms) {
		StringBuilder queryString = new StringBuilder();
		for(String term : keyTerms)
			queryString.append(term);
		query(queryString.toString());
		integrateKeyTerms(keyTerms);
		propagateScore();
		return evaluateLabels();
	}
	/**
	 * query wiki dataset with the queryString formed by concatena-
	 * ting all key terms extracted in previous steps. 
	 * 1.	put the docID and its corresponding statistics tuple into
	 * 	  	docMap. The 1st element of statistics tuple is label count
	 * 		- the sum of the number of categories and title. This will
	 * 		later be modified	by adding the number of unique key 
	 * 		terms the doc contains. The 2nd element is the document score.
	 * 2.	put the categories and title into labelMap. Notice that 
	 * 		one label can possibly appear in many docs. Thus, for each
	 * 		label we need to check labelMap if it is already exist in
	 * 		the labelMap.   
	 * 
	 */
	private void query(String queryString) {
		try {
			Query query = new QueryParser("contents", analyzer).parse(queryString);
			TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_DOC_NO);
			indexSearcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for(int i = 0; i < hits.length; i ++) {
				int docID = hits[i].doc;
				float docScore = hits[i].score;
				Document document = indexSearcher.doc(docID);
				// extract title and categories
				String title = document.get("title");
				String[] categories = document.getValues("category");
				// put title and categories into labelMap.
				ArrayList<Integer> docList = labelMap.get(title);
				if(docList != null)
					docList.add(docID);
				else {
					docList = new ArrayList<Integer>();
					docList.add(docID);
					labelMap.put(title, docList);
				}
				
				for(String category : categories) {
					docList = labelMap.get(category);
					if(docList != null)
						docList.add(docID);
					else {
						docList = new ArrayList<Integer>();
						docList.add(docID);
						labelMap.put(category, docList);
					}
				}
				// add docID to docMap
				docMap.put(docID, 
							new Tuple<Integer, Float>(categories.length + 1, 
													  docScore));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 1.	integrate key terms into labelMap
	 * 2.	if any document in the docMap contains key terms, 
	 * 		change it statistics tuple
	 * @param keyTerms
	 */
	private void integrateKeyTerms(List<String> keyTerms) {
		for(String keyTerm : keyTerms) {
			// see how many documents actually contains the keyTerms
			ScoreDoc[] hits = null;
			try {
				Query query = new QueryParser("contents", analyzer).parse(keyTerm);
				hits = indexSearcher.search(query, MAX_DOC_NO).scoreDocs;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			for(int i = 0; i < hits.length; i ++) {
				Tuple<Integer, Float> docTuple = docMap.get(hits[i].doc);
				if(docTuple != null)
					// if the document is within the docMap, renew the
					// label count.
					docMap.put(hits[i].doc, 
								new Tuple<Integer, Float>(docTuple.x + 1, 
														  docTuple.y));
			}
		}
	}
	/** 1.	calculate score for each labels
	 * 	2.	split the label into keywords, associate them with
	 * 		the current label score. Add them to keywordMap.
	 */
	private void propagateScore() {
		for(String label : labelMap.keySet()) {
			float labelDocScore = 0;
			ArrayList<Integer> docList = labelMap.get(label);
			// calculate score for each label
			for(Integer doc : docList) {
				Tuple<Integer, Float> stat = docMap.get(doc);
				labelDocScore += stat.y / stat.x;
			}
			// calculate score for each keyword
			String[] keywords = label.split(" ");
			for(String keyword : keywords) {
				Float keywordScore = keywordMap.get(keyword);
				if(keywordScore != null)
					keywordMap.put(keyword, keywordScore + labelDocScore);
				else keywordMap.put(keyword, labelDocScore);
			}
		}
	}
	
	private List<Label> evaluateLabels() {
		List<Label> labelCandidate = new ArrayList<Label>();
		for(String label : labelMap.keySet()) {
			float labelScore = 0;
			String[] keywords = label.split(" ");
			for(String keyword : keywords)
				labelScore += keywordMap.get(keyword);
			labelScore /= keywords.length;
			labelCandidate.add(new Label(label, labelScore));
		}
		labelCandidate.sort(new Comparator<Label>() {
			public int compare(Label o1, Label o2) {
				if(o1.score < o2.score) return 1;
				else if(o1.score > o2.score) return -1;
				else return 0;
			}
		});
		return labelCandidate;
	}  
	
	private class Label {
		final public String text;
		final public Float score; 
		public Label(String text, Float score) {
			this.text = text;
			this.score = score;
		}
		public String toString() {
			return this.text;
		}
	}
	 
	private class Tuple<X, Y> {
		final public X x;
		final public Y y;
		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
}
