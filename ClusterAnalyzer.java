import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;

public class ClusterAnalyzer extends Analyzer {
	public ClusterAnalyzer() {
		super();
	}
	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		Tokenizer tokenizer = new LowerCaseTokenizer();
		TokenStream stream = new StandardFilter(tokenizer);
		stream = new StopFilter(stream, StandardAnalyzer.STOP_WORDS_SET);
		// stream = new PorterStemFilter(stream);
		return new TokenStreamComponents(tokenizer, stream);
	}
}