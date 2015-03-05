import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class TextFileIndexer {
	private static String indexLocation = "wikiIndex";
	
	private static Analyzer analyzer;
	private IndexWriter indexWriter;
	public IndexReader indexReader;
	
	public static void main(String[] args) throws IOException, ParseException {
		//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		TextFileIndexer textFileIndexer = new TextFileIndexer(indexLocation);
		//textFileIndexer.indexCollection("input");
		//textFileIndexer.indexWikiDataset("wikiText");
		//textFileIndexer.closeIndexer();
		textFileIndexer.indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
		//System.out.println(textFileIndexer.indexReader.totalTermFreq(new Term("contents", "tom")));
		//long count = textFileIndexer.indexReader.getSumTotalTermFreq("contents");
		
		
		/*System.out.println("total word count is: " + count);
		IndexSearcher indexSearcher = new IndexSearcher(textFileIndexer.indexReader);
		TermQuery termQuery = new TermQuery(new Term("id", "1"));
		CollectionStatistics stat = indexSearcher.collectionStatistics("contents");
		System.out.println("Statistics.docCount:" + stat.docCount());
		System.out.println("Statistics.sumDocFreq:" + stat.sumDocFreq());
		System.out.println("Statistics.sumTotalTermFreq:" + stat.sumTotalTermFreq());
		ScoreDoc[] scoreDocs = indexSearcher.search(termQuery, 1).scoreDocs;
		Terms terms = textFileIndexer.indexReader.getTermVector(scoreDocs[0].doc, "contents");
		System.out.println("Terms.getDocCount:" + terms.getDocCount());
		System.out.println("Terms.getSumTotalTermFreq:" + terms.getSumTotalTermFreq());
		System.out.println("Terms.getSumDocFreq:" + terms.getSumDocFreq());
		System.out.println("Terms.size():" + terms.size());
		TermsEnum it = terms.iterator(null);
		BytesRef term = null;

		while ((term = it.next()) != null) {
			String termText = term.utf8ToString();
			long termFreq = it.totalTermFreq(); // FIXME: this only return
												// frequency in this doc
			long docCount = it.docFreq(); // FIXME: docCount = 1 in all cases

			System.out.println("term: " + termText + ", termFreq = " + termFreq
					+ ", docCount = " + docCount);
		} */      
	}

	public TextFileIndexer(String indexLocation) throws IOException {
		FSDirectory indexDirectory = FSDirectory.open(Paths.get(indexLocation));
		analyzer = new ClusterAnalyzer();
		//analyzer = new SimpleAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(indexDirectory, config);
	}
	
	public void indexClusters(File clusterDir) throws IOException {
		if(!clusterDir.isDirectory()) return;
		BufferedWriter bufferedWriter = new BufferedWriter(
											new OutputStreamWriter(
												new FileOutputStream(
													new File(clusterDir.getName() + ".txt"))));
		File[] files = clusterDir.listFiles();
		int originalDocNum = indexWriter.numDocs();
		for(File file : files) {
/*			String fileName = file.getName().toLowerCase();
			if(!fileName.endsWith(".txt")) {
				System.out.println("Skipping " + fileName);
				continue;
			}*/
			FileReader fileReader = null;
			try {
				Document document = new Document();
				fileReader = new FileReader(file);
				// self defined unique ID
				String id = String.valueOf(indexWriter.numDocs() + 1);
				bufferedWriter.write(id + '\n');
				document.add(new StringField("id", id, Field.Store.YES));
				document.add(new Field("contents", fileReader, Field.TermVector.YES));
				//document.add(new StringField("path", file.getPath(), Field.Store.YES));
				//document.add(new StringField("filename", file.getName(), Field.Store.YES));
				
				indexWriter.addDocument(document);
				System.out.println("Document" + file + " added!");
			} catch(Exception ex) {
				System.out.println("Fail to add document " + file);
			} finally {
				fileReader.close();
			}
		}
		
		int newDocNum = indexWriter.numDocs();
		System.out.println("---------------------");
		System.out.println(clusterDir.getName() + "has been indexed");
		System.out.println((newDocNum - originalDocNum) + "documents added");
		System.out.println("---------------------");
		bufferedWriter.close();
	}

	private void indexCollection(String file) throws IOException {
		File input = new File(file);
		if(!input.exists()) {
			System.out.println(file + "does not exists");
			return;
		}
		if(input.isDirectory()) {
			for(File cluster : input.listFiles())
				indexClusters(cluster);
		}
	}
	
	private void indexWikiDataset(String wikiDir) {
		File wikiRoot = new File(wikiDir);
		if(!wikiRoot.exists()) {
			System.out.println(wikiRoot + "does not exists");
			return;
		}
		if(wikiRoot.isDirectory()) {
			for(File wikiFile : wikiRoot.listFiles())
				iterativelyIndexWiki(wikiFile);
		}
	}
	
	private void iterativelyIndexWiki(File wikiFile) {
		if(wikiFile.isDirectory()) {
			for(File wikiText : wikiFile.listFiles())
				iterativelyIndexWiki(wikiText);
		} else if(wikiFile.isFile() && wikiFile.getName().endsWith(".txt")) {
			Stack<String> categoryList = new Stack<String>();
			String title = null;
			StringBuilder allText = new StringBuilder();
			try {
				BufferedReader bufferedReader = new BufferedReader(
													new InputStreamReader(
														new FileInputStream(wikiFile)));
				String line = null;
				while((line = bufferedReader.readLine()) != null) {
					// HEADER and CATEGORY should be in one line
					if(line.startsWith("HEADER:")) {
						title = line.substring(line.lastIndexOf(':') + 1,
												line.length());
						allText.append(title + ' ');
					}
					else if(line.startsWith("CATEGORY:")) {
						categoryList.add(line.substring(line.lastIndexOf(':') + 1,
														line.length()));
						allText.append(categoryList.peek() + ' ');
					}
					else if(line.startsWith("PARAGRAPH:") || line.startsWith("TABLE:"))
						allText.append(line.substring(line.indexOf(':') + 1, line.length()) + ' ');
					else allText.append(line + ' ');
				}
				bufferedReader.close();
				Document document = new Document();
				// self defined unique ID
				String id = String.valueOf(indexWriter.numDocs() + 1);
				document.add(new StringField("id", id, Field.Store.YES));
				document.add(new StringField("title", title, Field.Store.YES));
				for(String category : categoryList)
					document.add(new StringField("category", category, Field.Store.YES));
				document.add(new TextField("contents", allText.toString(), Field.Store.YES));
				indexWriter.addDocument(document);
				System.out.println("Document " + wikiFile.getName() + " indexed!");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} else System.out.println("Skipping " + wikiFile.getName());
	}
	
	public void closeIndexer() throws IOException {
		indexWriter.close();
	}
}