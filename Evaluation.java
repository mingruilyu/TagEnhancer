import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {
	
	private final static PrintStream  stdout = System.out;
	private  static PrintStream printStream= null;
	public static void main(String args[]) {		
		
		
		File output = new File("../result.txt");
		FileOutputStream fileOutputStream=null;
		try {
			fileOutputStream = new FileOutputStream(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printStream = new PrintStream(fileOutputStream);
		System.setOut(printStream);

		generateFigure2();
	    //generateFigure2();

		// System.out.println("resultForLabel ="+resultForLabel );
		// System.out.println("resultForKeyTerms ="+resultForKeyTerms );

		// List<String> keyTerms = new ArrayList<String>();
		// //keyTerms.add("Arabica ");
		// //keyTerms.add("movie ");
		// //keyTerms.add("poet ");
		// TermExtractor termExtractor;
		// try {
		// termExtractor = new TermExtractor("clusterIndex");
		// keyTerms = termExtractor.extractTerms("Motocycles.txt", 4);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// System.out.println("The key terms that characterize the cluster is: "
		// + keyTerms);
		// LabelGenerator labelGenerator = new
		// LabelGenerator(TextFileIndexer.wikiIndex);
		// System.out.println(labelGenerator.generateLabel(keyTerms));
	}

	private static void generateFigure3() {
		// TODO Auto-generated method stub

		final int max = 100;
		File file = new File(".");
		File files[] = file.listFiles();

		// System.out.println(files.length);
		double[] resultForLabel = new double[11];
		double[] resultForKeyTerms = new double[11];
		for (int k = 6; k <= 8; k+=10) {
			double labelCount = 0;
			double keyTermsCount = 0;
			int count = 0;
			for (int i = 0; i < files.length; i++) {
				if (!files[i].getName().endsWith(".txt"))
					continue;
				else {
					System.out.println(files[i].getName());
					count++;
					List<TermExtractor.TermJSD> keyTerms = new ArrayList<TermExtractor.TermJSD>();
					TermExtractor termExtractor;
					try {
						termExtractor = new TermExtractor("clusterIndexODP");
						keyTerms = termExtractor.extractTerms(
								files[i].getName(),20);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// System.out.println("Key terms:" + keyTerms);
					LabelGenerator labelGenerator = new LabelGenerator(
							TextFileIndexer.wikiIndex);
					labelGenerator.setMAX_DOC_NO(k);
					List<LabelGenerator.Label> labels = labelGenerator
							.generateLabel(keyTerms,20);
					// System.out.println("Labels:" + labels);
					String correctResult = getCorrectCat(files[i].getName());
					// System.out.println(correctResult);
					int index1 = checkContains(labels, correctResult, -1)+1;
					System.out.println("index1 "+index1);
					if (index1 > 0) {
						
						labelCount+=1.0/index1;
					}
					int index2 = checkContainsKeyTerms(keyTerms, correctResult,
							-1)+1;
					System.out.println("index2 "+index2);
					if (index2 > 0) {
						
						//System.out.println("Find in key terms");
						keyTermsCount+=1.0/index2;
					}

				}
			}
			System.out.println("labelCount =" + labelCount);
			System.out.println("keyTermsCount =" + keyTermsCount);
//			resultForLabel[k/10-1] = labelCount * 1.0 / (count * 1.0);
//			resultForKeyTerms[k/10 - 1] = keyTermsCount * 1.0 / (1.0 * count);
			resultForLabel[0] = labelCount * 1.0 / (count * 1.0);
			resultForKeyTerms[0] = keyTermsCount * 1.0 / (1.0 * count);

//			System.out.println("labelCount =" + labelCount);
//			System.out.println("keyTermsCount =" + keyTermsCount);

//			resultForLabel[k - 1] = labelCount;
	//		resultForKeyTerms[k - 1] = keyTermsCount;
		}
		myPrint(resultForLabel);
		myPrint(resultForKeyTerms);

	}

	private static void generateFigure2() {
		// TODO Auto-generated method stub

		final int max = 30;
		File file = new File("./ODPCluster");
		File files[] = file.listFiles();

		// System.out.println(files.length);
		double[] resultForLabel = new double[max];
		double[] resultForKeyTerms = new double[max];
		for (int k = 5; k <= 5; k++) {
			double labelCount = 0;
			double keyTermsCount = 0;
			int count = 0;
			for (int i = 0; i < files.length; i++) {
				System.setOut(stdout);
				System.out.println(i);
				System.setOut(printStream);
				if (!files[i].getName().endsWith(".txt"))
					continue;
				else {
					System.out.println(files[i].getName());
					count++;
					List<TermExtractor.TermJSD> keyTerms = new ArrayList<TermExtractor.TermJSD>();
					TermExtractor termExtractor;
					try {
						termExtractor = new TermExtractor("clusterIndexODP");
						keyTerms = termExtractor.extractTerms(
								files[i].getPath(), k);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					//System.out.println("Key terms:" + keyTerms);
					System.out.println("Key terms: ");
					int x = 1;
					for(TermExtractor.TermJSD  term : keyTerms){
						if(x>10)
							break;
						System.out.print(term.getTerm()+"\t");
						x++;
					}
					System.out.println();
					LabelGenerator labelGenerator = new LabelGenerator(
							TextFileIndexer.wikiIndex);
					List<LabelGenerator.Label> labels = labelGenerator
							.generateLabel(keyTerms,k);
					System.out.println("Labels:" + labels);
					String correctResult = getCorrectCat(files[i].getName());
					// System.out.println(correctResult);
					int index1 = checkContains(labels, correctResult, -1)+1;
					if (index1 > 0) {
						//System.out.println("Find in labels");
						labelCount+=1.0/index1;
					}
					int index2 = checkContainsKeyTerms(keyTerms, correctResult,
							-1)+1;
					if (index2 > 0) {
						System.out.println("Find in key terms");
						keyTermsCount+=1.0/index2;
					}

				}
			}
			System.out.println("labelCount =" + labelCount);
			System.out.println("keyTermsCount =" + keyTermsCount);
			resultForLabel[k - 1] = labelCount * 1.0 / (count * 1.0);
			resultForKeyTerms[k - 1] = keyTermsCount * 1.0 / (1.0 * count);
//			System.out.println("labelCount =" + labelCount);
//			System.out.println("keyTermsCount =" + keyTermsCount);

//			resultForLabel[k - 1] = labelCount;
	//		resultForKeyTerms[k - 1] = keyTermsCount;
		}
		myPrint(resultForLabel);
		myPrint(resultForKeyTerms);

	}

	private static void generateFigure1() {
		// TODO Auto-generated method stub
		final int K = 15;
		File file = new File(".");
		File files[] = file.listFiles();

		// System.out.println(files.length);
		double[] resultForLabel = new double[K];
		double[] resultForKeyTerms = new double[K];
		for (int k = 1; k <= K; k++) {
			int labelCount = 0;
			int keyTermsCount = 0;
			int count = 0;
			for (int i = 0; i < files.length; i++) {
				if (!files[i].getName().endsWith(".txt"))
					continue;
				else {
					System.out.println(files[i].getName());
					count++;
					List<TermExtractor.TermJSD> keyTerms = new ArrayList<TermExtractor.TermJSD>();
					TermExtractor termExtractor;
					try {
						termExtractor = new TermExtractor("clusterIndexODP");
						keyTerms = termExtractor.extractTerms(
								files[i].getName(), 20);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// System.out.println("Key terms:" + keyTerms);
					LabelGenerator labelGenerator = new LabelGenerator(
							TextFileIndexer.wikiIndex);
					List<LabelGenerator.Label> labels = labelGenerator
							.generateLabel(keyTerms,20);
					// System.out.println("Labels:" + labels);
					String correctResult = getCorrectCat(files[i].getName());
					System.out.println("correntResult" +correctResult);
					int index1 = checkContains(labels, correctResult, k);
					if (index1 >= 0) {
						System.out.println("Find in labels");
						labelCount++;
					}
					int index2 = checkContainsKeyTerms(keyTerms, correctResult,
							k);
					if (index2 >= 0) {
						System.out.println("Find in key terms");
						keyTermsCount++;
					}

				}
			}
			System.out.println("labelCount =" + labelCount);
			System.out.println("keyTermsCount =" + keyTermsCount);

			resultForLabel[k - 1] = labelCount * 1.0 / (count * 1.0);
			resultForKeyTerms[k - 1] = keyTermsCount * 1.0 / (1.0 * count);
		}
		myPrint(resultForLabel);
		myPrint(resultForKeyTerms);

	}

	private static void myPrint(double[] array) {
		System.out.println();
		for (double d : array) {
			System.out.print(d + "\t");
		}
		System.out.println();
	}

	private static int checkContains(List<LabelGenerator.Label> labels,
			String correctResult, int k) {
		correctResult.toLowerCase();
		if(correctResult.endsWith("s"))
			correctResult = correctResult.substring(0,correctResult.length()-1);
		
		if (k == -1) {
			for (int i = 0; i < labels.size(); i++) {
				String s = labels.get(i).text.toLowerCase();
				if(s.contains(correctResult))
					return i;
//				String[] array = s.split("\\s+");
//				for (int j = 0; j < array.length; j++) {
//					if (array[j].equals(correctResul)
//							|| (correctResul + "s").equals(array[j])
//							|| (correctResul).equals(array[j] + "s"))
//						return i;
//				}

			}
			return -1;
		} else {
			for (int i = 0; i < k; i++) {
				String s = labels.get(i).text.toLowerCase();
				if(s.contains(correctResult))
					return i;

//				String[] array = s.split("\\s+");
//				for (int j = 0; j < array.length; j++) {
//					if (array[j].equals(correctResul)
//							|| (correctResul + "s").equals(array[j])
//							|| (correctResul).equals(array[j] + "s"))
//						return i;
//				}

			}
			return -1;
		}
	}

	private static int checkContainsKeyTerms(List<TermExtractor.TermJSD> keyTerms,
			String correctResult, int k) {
		correctResult.toLowerCase();
		if(correctResult.endsWith("s"))
			correctResult = correctResult.substring(0,correctResult.length()-1);

		if (k == -1) {
			for (int i = 0; i < keyTerms.size(); i++) {
				String s = keyTerms.get(i).getTerm().toLowerCase();
				if(s.contains(correctResult))
					return i;

				//				String[] array = s.split("\\s+");
//				for (int j = 0; j < array.length; j++) {
//					if (array[j].equals(correctResul)
//							|| (correctResul + "s").equals(array[j])
//							|| (correctResul).equals(array[j] + "s"))
//						return i;
//				}

			}
			return -1;

		} else {
			for (int i = 0; i < k; i++) {
				String s = keyTerms.get(i).getTerm().toLowerCase();
				if(s.contains(correctResult))
					return i;
			}
			return -1;
		}
	}

	private static String getCorrectCat(String fileName) {
		String[] arrays = fileName.split("\\.");
		// System.out.println("legnht"+arrays.length);
		return arrays[0];
	}

}
