/*
This Class is Used to report some statistical information about a given field from a given Index
Output Field Information :
    1- Number Of Unique terms
    2- Average Document length
    3- Number of Empty terms (Missing Terms )
    4- Total number of terms 
    5- Number of documents ( number of rows )
Created By : Abdulaziz AlQatan - 21/07/2019

 */

package lucene4ir.Stats;

import javafx.scene.control.RadioMenuItem;
import lucene4ir.ExampleStatsApp;
import lucene4ir.Lucene4IRConstants;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ConditionalTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.shingle.ShingleFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

public class StatisticsIndex {

    /*
   Output Field Properties
   1- Number Of Unique terms
   2- Average Document length
   3- Number of Empty terms (Missing Terms )
   4- Total number of terms 
   5- Number of documents ( number of rows )
   */
    public int uniqueTermsCtr ,emptyTermsCtr , allTermsCtr , docsCtr;
    public double avgDocLength;
    String outDir , indexName , fldName , indexesFolder;
    PrintWriter pr;
    boolean screenOutput;
    int maxdoc;

    // General Variables
    IndexReader reader;

    private String getFieldName(int fldID) {
        // Get Field Name by its ID
        return reader.leaves().get(0).reader().getFieldInfos().fieldInfo(fldID).name;
    } // End Function

    private void reset() {
        // Reset All Counters
        uniqueTermsCtr = 0;
        emptyTermsCtr = 0;
        allTermsCtr = 0;
    }

    private void displayResults(String indexName , String fldName)
    {
        // Display Results
        String outText;
        outText =  "\nIndex Statistics" ;
        outText += "\n--------------------";
        outText += "\nIndex : " + indexName;
        outText += "\nField Name : " + fldName;
        outText += "\n1- Unique Terms Count = " + uniqueTermsCtr;
        outText += String.format("\n2- Average Document Length = %1.4f", avgDocLength);
        outText += "\n3- Empty Terms Count = " + emptyTermsCtr;
        outText += "\n4- Total Terms Count = " + allTermsCtr;
        outText += "\n5- Documents Count = " + docsCtr;

        System.out.println(outText);
    } // End Function

    private void iterateField(String fldName) throws IOException {
        // Iterate through the given Field (fldName)
        // and calculate general statistics for its terms
        String currentTerm;
        ArrayList<String> terms = new ArrayList<String>(); // Record All Terms
        ArrayList<String> replicatedTerms = new ArrayList<String>();

        // Number Of Documents
        reset();
        docsCtr = reader.maxDoc();
        for (int i = 0; i < docsCtr; i++) {
            currentTerm = reader.document(i).get(fldName).trim();

            // Empty Terms Count
            if (currentTerm.isEmpty()) {
                emptyTermsCtr++;
                continue;
            } // end if (currentTerm.isEmpty())

            // Unique Terms Count
            if (terms.contains(currentTerm)) {
                if (!replicatedTerms.contains(currentTerm)) {
                    uniqueTermsCtr--;
                    replicatedTerms.add(currentTerm);
                } // End if (!replicateTerms.contains(currentTerm))
            } // end  if (terms.contains(currentTerm))
            else {
                uniqueTermsCtr++;
                terms.add(currentTerm);
            } // End Else

            // Total Terms
            allTermsCtr += currentTerm.split(" ").length;
        } // End For
        // Average Document Length
        avgDocLength = allTermsCtr * 1.0 / docsCtr;
        reader.close();
    } // End Function

    private void calculateGeneralStatistics(String indexName , String fldName )
    {
        /*
        This Function is used to calculate the General Statistics of an Index
        That is not specified for each document separately :
        General Statistics are :
        IndexName  -  Chosen FieldName  - Unique Terms Count - Average Document Length
        EmptyTerms Count - Total Terms Count - Documents Count
        */

        int numberOfFields;
        try {
            ExampleStatsApp ex = new ExampleStatsApp();
            openReader();
            if (fldName.isEmpty())
            {
                // Iterate All Fields
                numberOfFields = reader.leaves().get(0).reader().getFieldInfos().size();
                for (int i = 0 ; i < numberOfFields ; i++)
                {
                    fldName = getFieldName(i);
                    iterateField(fldName);
                    displayResults(indexName,fldName);
                } // End For
            } // End IF
            else
            {
                // Iterate Specific Field
                iterateField(fldName);
                displayResults(indexName,fldName);
            };
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        } // End Catch
        catch (Exception e) {
            e.printStackTrace();
        }
    } // End Function

    private void printFieldList () throws Exception
    {
        ExampleStatsApp ex = new ExampleStatsApp();
        ex.indexName = indexesFolder + indexName;
        ex.openReader();
        ex.fieldsList();
    } // End Function

    private void openWriter (String extra) throws Exception
    {
        String outFileName =  outDir + indexName + fldName + extra + ".txt";
        if (!screenOutput)
            pr = new PrintWriter(outFileName);
    }

    private void close() throws Exception
    {
        if (pr != null)
            pr.close();
        if (reader != null)
            reader.close();
        System.out.println("Done Successfully ");
    }
    private void printLine (String line)
    {
        if (screenOutput)
            System.out.println(line);
        else
            pr.write(line + "\n");
    }

    private void openReader()
    {
        String tempIndexName;
        try {
            if (indexesFolder.isEmpty())
                tempIndexName = indexName;
            else
                tempIndexName = indexesFolder + indexName;
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(tempIndexName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // End Function

    private void printTermList(int docid, boolean withFrequency) throws Exception
    {
        String line ;
        Terms trs;
        TermsEnum it;
        BytesRef term;
        int ctr = 0 , start  , end;
        openWriter("TermList");

        openReader();
        if (docid < 0 )
        {
            end = reader.maxDoc();
            start = 0;
        }
        else
        {
            start = docid;
           // ShingleFilter
            end = docid + 1;
        }

        for (int i = start ; i < end ; i++)
        {
            trs = reader.getTermVector(i,fldName);
         //   line = "Document " + i + "\nTermList : ";
         //  printLine(line);
            it = trs.iterator();
            while ((term = it.next()) != null)
                {
              //  line = "Term : " + term.utf8ToString();
                line = term.utf8ToString();
                if (withFrequency)
                    line += " " + it.totalTermFreq();
                printLine(line);
                ctr++;
                } // End while
        } // End For
        line = "Total Terms = " + ctr;
        printLine(line);
    } // End Function

    private void printTermLeavesCount () throws Exception
    {
      openReader();
        long totalTerms = 0 , currentTerms;

       String line = String.format("Index Name : %s\nFieldName : %s\n",indexName , fldName);
       printLine(line);
      for (int i = 0 ; i < reader.leaves().size() ; i ++)
      {
          currentTerms = reader.leaves().get(i).reader().getSumTotalTermFreq(fldName);
          System.out.println("Leaf " + i + " TermCount = " + currentTerms);
          totalTerms += currentTerms;
      } // End For
        printLine("Total Terms : " + totalTerms);
    }

    private void printTermCount(int docid) throws Exception
    {
        String line ;
        Terms trs;
        long totalTerms = 0 , currentTermsCount ;
        int start , end;

        openWriter("TermCount");
        openReader();

        line = String.format("Index Name : %s\nFieldName : %s\nDocID    Length",indexName , fldName);
        printLine(line);
       if (docid < 0 )
       {
           ExampleStatsApp ex = new ExampleStatsApp();
           ex.reader = reader;
           ex.termsList(fldName);
       }
        else
       {
           start = docid;
           end = start + 1;
           //end = reader.maxDoc();
           for (int i = start ; i < end ; i++)
           {
               trs = reader.getTermVector(i,fldName);
             /*  line = reader.document(i).get(fldName);
               checkAnalyzer(line,"Unigram");
               checkAnalyzer(line,"Bigram");
               checkAnalyzer(line,"Combined");
*/
               currentTermsCount = trs.size();
               line = " " + i + "          " + currentTermsCount;
               printLine(line);
               totalTerms += currentTermsCount;
           } // End For
           line = "Total Terms : " + totalTerms + "\n" +
                   "Average Document Length : " + totalTerms * 1.0 /reader.maxDoc() ;
           printLine(line);
       }


    }  // End Function

    private void printLeavesCount ()
    {
        String line ;
        ExampleStatsApp ex = new ExampleStatsApp();
        openReader();

        line =    "Index : " + indexName
                + "\nLeaves Count : " + reader.leaves().size();
        printLine(line);
    }


private void  checkAnalyzer (String all , String indexType) throws Exception
{
    String  biTokenFilterFile = "params/index/TokenFilterFile_BigramReplacePattern.xml",
            // TokenFilterFile_BigramReplacePattern TokenFilterFile_Bigram
            uniTokenFilterFile = "params/index/TokenFilterFile_Unigram.xml" ,
            combinedTokenFilterFile = "params/index/TokenFilterFile_Combinedgram.xml",
            inputFilter = "";
    TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
    Analyzer an;

    TokenStream ts;
    CharTermAttribute term;
    int termsCount = 0;

    System.out.println("Start Checking " + indexType);
    System.out.println("input : '" + all + "'");
    System.out.println("------------------");
    switch (indexType)
    {
        case "Unigram" :
            inputFilter = uniTokenFilterFile;
            break;
        case "Bigram":
            inputFilter = biTokenFilterFile;
            break;
        case "Combined":
            inputFilter = combinedTokenFilterFile;
            break;
    } // End Switch
    an = tam.createAnalyzer(inputFilter);

    ts =  an.tokenStream(fldName,all);

    ts.reset();

    while (ts.incrementToken())
    {
        term = ts.getAttribute(CharTermAttribute.class);
        printLine(term.toString());
        termsCount++;



    }

    printLine(indexType + " Index has " + termsCount +
            " terms\n----------------------------------------");
} // End Function

    private void printDocCount ()
    {
        openReader();
        System.out.println("Total Document Count : " + reader.maxDoc());
    }

    private void printIndexSet () throws Exception {
                String indexList[] =
                {"AquaintBigramIndex","AquaintCombinedIndex", "AquaintUnigramIndex",
                "Core17BigramIndex","Core17CombinedIndex","Core17UnigramIndex"};
        for (int i = 0 ; i < indexList.length ; i++)
        {
            indexName = indexList[i];
            printTermCount(-1);
            close();
        }
    }

    private void printDocIDs () throws Exception
    {
        String id;
        openReader();
        openWriter("");
        for (int i = 0 ; i < reader.maxDoc() ; i++)
        {
            id = reader.document(i).get("id");
            this.printLine(id);
        }
    }
    private void countFiller(int docid) throws Exception
    {
        String line ;
        Terms trs;
        TermsEnum it;
        long totalTerms = 0 ;
        int start , end , ctr = 0;


       // openWriter("TermCount");
        openReader();

        line = String.format("Index Name : %s\nFieldName : %s",indexName , fldName);
        printLine(line);
        if (docid < 0 )
        {
            start = 0;
            end = reader.maxDoc();
        }
        else
        {
            start = docid;
            end = start + 1;
        }

        for (int i = start ; i < end ; i++)
        {
            trs = reader.getTermVector(i,fldName);
            totalTerms += trs.size();
            it = trs.iterator();
            while(it.next() != null)
            {
                line = it.term().utf8ToString();
                if (line.contains("___"))
                {
                    printLine(line);
                    ctr++;
                }
            }

        } // End For
        line =  "Filtered Terms %d of %s \nAverage  = %2.2f";
        line = String.format(line,ctr,totalTerms,(ctr * 100.0) /totalTerms);
        printLine(line);
    }

    public static String removeStopWords(String textFile) throws Exception {
        CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
        TokenStream tokenStream = new StandardTokenizer();
        tokenStream.addAttribute (CharTermAttribute.class);
        tokenStream = new StopFilter( tokenStream, stopWords);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();
            sb.append(term + " ");
        }
        return sb.toString();
    }

    public void printDocLengths () throws Exception
    {
//        ExampleStatsApp ex = new ExampleStatsApp();
        openReader();
//        String id = reader.document(0).get("id");
//        TermsEnum termsEnum = reader.getTermVector( 1, fldName ).iterator();
        long doclen = 0;
//        while ( termsEnum.next() != null )
//            doclen += termsEnum.totalTermFreq();

        IndexSearcher is = new IndexSearcher(reader);
        Similarity s = is.getSimilarity();
        FieldInvertState state = new FieldInvertState(8,fldName,IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        s.computeNorm(state);
//        doclen = state.getLength();

        printLine(Long.toString(doclen));
    }
    public static void main(String[] args) {
        /*
      Small :  smallFieldedIndex smallCombinedIndex smallBigramIndex smallUnigramIndex testIndex biraw
      Single : SingleDocument30WordsBigramIndex SingleDocument30WordsCombinedIndex SingleDocument30WordsUnigramIndex SingleDocument30WordsFieldedIndex
      Corpus:
        AquaintUnigramIndex AquaintBigramIndex AquaintCombinedIndex AquaintFieldedIndex
        Core17UnigramIndex Core17BigramIndex Core17CombinedIndex Core17FieldedIndex */

       StatisticsIndex sts = new StatisticsIndex();
      /*  AquaintUnigramIndex AquaintBigramIndex AquaintCombinedIndex AquaintFieldedIndex
        Core17UnigramIndex Core17BigramIndex Core17CombinedIndex Core17FieldedIndex
        WAPOUnigramIndex
        */

//        C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\Indexes\AquaintIndex
//        sts.indexesFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\LUCENE\\anserini-master\\Indexes\\";
//        sts.indexName =  "lucene-index.robust05.pos+docvectors+rawdocs";

        sts.indexesFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\Indexes\\";
        sts.indexName =  "WAPOUnigramIndex";

        sts.fldName = Lucene4IRConstants.FIELD_RAW;
        sts.outDir = "C:\\Users\\kkb19103\\Desktop\\DocMaps\\";

        sts.screenOutput = true;
        sts.maxdoc = 0;
        try {

          // String all = "me is is now the floor never expected";
          // sts.checkAnalyzer(all,"Unigram");
          //  sts.checkAnalyzer(all,"Bigram");
          //  removeStopWords(all);
           // sts.checkAnalyzer(all,"Combined");
          //  sts.countFiller(-1);

           // sts.printTermList(1,false);
           sts.printTermCount(0);
//            sts.printDocIDs();
//            sts.printDocCount();
//            sts.printDocLengths();
          //  sts.printLeavesCount();
         //  sts.printTermLeavesCount();
//           sts.printFieldList();
         //  sts.printIndexSet();
           sts.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // End Main Function

} // End Class