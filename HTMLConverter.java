import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class HTMLConverter {
	List<File> convertList;
	String outputPath;
	PrintStream stdout = System.out;
	public static void main(String[] args) throws Exception {
		String inputPath = "/home/david/Projects/SearchEngine/wikiDataset";
		String outputPath = "../wikiText";
		HTMLConverter converter = new HTMLConverter(outputPath);
		converter.addHTMLFiles(new File(inputPath));
		//converter.convert();
	}

	public HTMLConverter(String outputPath) {
		this.outputPath = outputPath;
		File outputDir = new File(outputPath);
		if(!outputDir.exists())
			outputDir.mkdir();
		convertList = new ArrayList<File>();
	}
	
	private void createXML(String fileName) throws FileNotFoundException {
		File output = new File(this.outputPath + '/' + fileName);
		FileOutputStream fileOutputStream = new FileOutputStream(output);
		PrintStream printStream = new PrintStream(fileOutputStream);
		System.setOut(printStream);
	}
	
	public void convert(File file) throws IOException {
		//for(File file : convertList) {
		
			Source source = new Source(file);
			source.fullSequentialParse();
			String fileName = file.getName();
			createXML(fileName.substring(0, fileName.lastIndexOf('.')) + ".txt");
			//extractTitle(source);		
			extractHeader(source);
			extractParagraph(source);
			extractTable(source);
			extractCategory(source);
			System.setOut(stdout);
			System.out.println(file.getName() + " converted!");
		//}
	}
	
	private void addHTMLFiles(File file) {
		if (!file.exists())
			System.out.println(file + " does not exists");
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				//System.out.println("Going into the directory " + subFile.getName());
				addHTMLFiles(subFile);
			}
		} else {
			String fileName = file.getName().toLowerCase();
			if (fileName.endsWith(".html")) {
				//convertList.add(file);
				try {
					convert(file);
					//System.out.println(fileName + " converted!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else
				System.out.println("Skipping " + fileName);
		}
	}
	
	private static void extractTitle(Source source) {
		//System.out.println("TITLE");
		//System.out.println("-----------------------------------------");
		Element titleElement=source.getFirstElement(HTMLElementName.TITLE);
		if (titleElement==null) return;
		String title = titleElement.getContent().getTextExtractor().toString();
		System.out.println("TITLE:" + title.substring(0, title.lastIndexOf('-')));
		//System.out.println();
	}
	
	private static void extractTable(Source source) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Element> tables = source.getAllElements(HTMLElementName.TABLE);
		for(Element table : tables) {
			String tableClass = table.getAttributeValue("class");
			if(tableClass == null || !tableClass.startsWith("infobox")) continue;
			List<Element> tableHeaderElements = table.getAllElements(HTMLElementName.TH);
			for (Element headerElement : tableHeaderElements) {
				String header = headerElement.getContent().getTextExtractor().toString();
				stringBuilder.append(header + ' ');
			}
			List<Element> tableStandardElements = table.getAllElements(HTMLElementName.TD);
			for (Element headerElement : tableStandardElements) {
				String standard = headerElement.getContent().getTextExtractor().toString();
				stringBuilder.append(standard + ' ');
			}
		}
		
		System.out.println("TABLE:" + stringBuilder.toString());
		//System.out.println();
	}
	
	private static void extractHeader(Source source) {
		//System.out.println("HEADER");
		//System.out.println("-----------------------------------------");
		List<Element> headerElements=source.getAllElements(HTMLElementName.H1);
		for (Element headerElement : headerElements) {
			String header = headerElement.getContent().getTextExtractor().toString();
			System.out.println("HEADER:" + header);
		}
		//System.out.println();
	} 
	
	private static void extractParagraph(Source source) {
		//System.out.println("PARAGRAPH");
		//System.out.println("-----------------------------------------");
		StringBuilder stringBuilder = new StringBuilder();
		List<Element> paragraphElements = source.getAllElements(HTMLElementName.P);
		for (Element paragraphElement : paragraphElements) {
			String paragraph = paragraphElement.getContent().getTextExtractor().toString();
			stringBuilder.append(paragraph + ' ');
		}
		System.out.println("PARAGRAPH:" + stringBuilder.toString());
		//System.out.println();
	}
	
	private static void extractCategory(Source source) {
		//System.out.println("CATEGORY");
		//System.out.println("-----------------------------------------");
		List<Element> linkElements = source.getAllElements(HTMLElementName.A);
		for (Element linkElement : linkElements) {
			String href = linkElement.getAttributeValue("title");
			if(href == null) continue;
			else if(href.startsWith("Category:")) 
				System.out.println("CATEGORY:" + href.substring(href.lastIndexOf(':') + 1, href.length()));
			// A element can contain other tags so need to extract the text from it:
			//String label=linkElement.getContent().getTextExtractor().toString();
		}
		//System.out.println();
	}
}