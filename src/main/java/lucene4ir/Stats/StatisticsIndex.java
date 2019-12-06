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

import lucene4ir.ExampleStatsApp;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
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
    String outDir , indexName , fldName;
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
        ex.indexName = indexName;
        ex.openReader();
        ex.fieldsList();
    } // End Function

    private void openWriter (String extra) throws Exception
    {
        String outFileName =  outDir + indexName + fldName + extra + ".sts";
        if (!screenOutput)
            pr = new PrintWriter(outFileName);
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
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // End Function

    private void printTermList(boolean withFrequency) throws Exception
    {
        String line ;
        Terms trs;
        TermsEnum it;
        BytesRef term;
        openWriter("TermList");
      //  line = String.format("Index Name : %s\nFieldName : %s",indexName , fldName);
      //  printLine(line);
        openReader();
        if (maxdoc == 0 )
            maxdoc = reader.maxDoc();
        for (int i = 0 ; i < maxdoc ; i++)
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
                } // End while
        } // End For
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

/*private void newTermCount() throws Exception
{
    openReader();
    reader.document(0).getField(fldName).tokenStream(new StandardAnalyzer() , ts);
}*/

    private void printTermCount() throws Exception
    {
        String line ;
        Terms trs;
        long totalTerms = 0 , currentTermsCount;
        ExampleStatsApp ex = new ExampleStatsApp();
        ex.indexName = indexName;
        openWriter("TermCount");

        line = String.format("Index Name : %s\nFieldName : %s",indexName , fldName);
        printLine(line);
        ex.openReader();

        if (maxdoc == 0 )
            maxdoc = ex.reader.maxDoc();
        for (int i = 0 ; i < maxdoc ; i++)
        {
           trs = ex.reader.getTermVector(i,fldName);
           currentTermsCount = trs.getSumTotalTermFreq();
         //  line = "Document " + i + " TermCount : " + currentTermsCount;
         //   printLine(line);
            totalTerms += currentTermsCount;
        } // End For
        line = "Total Terms : " + totalTerms + "\n" +
                "Average Document Length : " + totalTerms * 1.0 /maxdoc ;
        printLine(line);
    }  // End Function

    private void printLeavesCount ()
    {
        String line ;
        ExampleStatsApp ex = new ExampleStatsApp();
        ex.indexName = indexName;
        ex.openReader();
        screenOutput = true;
        line =    "Index : " + indexName
                + "\nLeaves Count : " + ex.reader.leaves().size();
        printLine(line);
    }


private void  checkAnalyzer (String indexType) throws Exception
{
    String all = "AAR CORP reports earnings for Qtr to Nov 30 1987/01/011987 The New York Times COMPANY REPORTS Statistics Top/News/Business Company Reports Corporations The New York Times AAR CORP reports earnings for Qtr to Nov 30 LEAD: *3*** COMPANY REPORTS ** *3*AAR CORP (NYSE) Qtr to Nov 30 1986 1985 Sales 75,907,000 61,040,000 Net inc 3,953,000 2,858,000 Share earns .38 .32 Shares outst 10,479,000 9,069,000 6mo sales 142,283,000 114,876,000 Net inc 7,054,000 5,300,000 Share earns .71 .59 Shares outst 9,932,000 9,066,000 LEAD: *3*** COMPANY REPORTS ** *3*AAR CORP (NYSE) Qtr to Nov 30 1986 1985 Sales 75,907,000 61,040,000 Net inc 3,953,000 2,858,000 Share earns .38 .32 Shares outst 10,479,000 9,069,000 6mo sales 142,283,000 114,876,000 Net inc 7,054,000 5,300,000 Share earns .71 .59 Shares outst 9,932,000 9,066,000 *3*** COMPANY REPORTS ** *3*AAR CORP (NYSE) Qtr to Nov 30 1986 1985 Sales 75,907,000 61,040,000 Net inc 3,953,000 2,858,000 Share earns .38 .32 Shares outst 10,479,000 9,069,000 6mo sales 142,283,000 114,876,000 Net inc 7,054,000 5,300,000 Share earns .71 .59 Shares outst 9,932,000 9,066,000 The 1985 share earnings and shares outstanding are adjusted for the 3-for- 2 stock split in February 1986. The company said the 1986 shares outstanding reflects the pro rata effect of the issuance of 1.35 million shares in public offering in August 1986.  The New York Times 1987/01/01",
            biTokenFilterFile = "params/index/TokenFilterFile_Bigram.xml",
            uniTokenFilterFile = "params/index/TokenFilterFile_Unigram.xml" ,
            combinedTokenFilterFile = "params/index/TokenFilterFile_Combinedgram.xml",
            inputFilter = "",line;
    TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
    Analyzer an;
    TokenStream ts;
    CharTermAttribute term;
    int termsCount = 0;

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
    screenOutput = true;

    while (ts.incrementToken())
    {
        term = ts.getAttribute(CharTermAttribute.class);
        printLine(term.toString());
        termsCount++;
    }

    printLine(indexType + " Index has " + termsCount + " terms");
} // End Function

    private void printDocLength (int docID) throws Exception
    {
        openReader();
      String temp = reader.document(docID).get("raw").toString();
    }
    private void printDocCount ()
    {
        openReader();
        System.out.println("Total Document Count : " + reader.maxDoc());
    }
    public static void main(String[] args) {
        /*
      Small :  smallFieldedIndex smallCombinedIndex smallBigramIndex smallUnigramIndex testIndex biraw
      Single : SingleDocument30WordsBigramIndex SingleDocument30WordsCombinedIndex SingleDocument30WordsUnigramIndex SingleDocument30WordsFieldedIndex
      Corpus:  Core17BigramIndex Core17CombinedIndex Core17UnigramIndex Core17FieldedIndex
       AquaintsmallFieldedIndex AquaintsmallUnigramIndex AquaintsmallBigramIndex AquaintsmallCombinedIndex
       Corpus:  AquaintBigramIndex AquaintCombinedIndex AquaintUnigramIndex AquaintFieldedIndex
         */

        String indexesFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\Indexes\\";
        StatisticsIndex sts = new StatisticsIndex();
         // C:/Users/kkb19103/Desktop/My Files 07-08-2019/LUCENE/anserini-master/lucene-index.core18.pos+docvectors+rawdocs
        // oneDocCombinedIndex - oneDocUnigramIndex
        sts.indexName =  "oneDocCombinedIndex";
        sts.fldName = "raw";
        sts.outDir = "C:\\Users\\kkb19103\\Desktop\\CheckTerms\\";

        sts.screenOutput = true;
        sts.maxdoc = 0;
        try {
         //   sts.checkAnalyzer("Unigram");
        //    sts.checkAnalyzer("Bigram");
        //    sts.checkAnalyzer("Combined");

          //  sts.printDocLength(0);

            sts.printTermList(true);
        //   sts.printTermCount();
          //  sts.printDocCount();

          //  sts.printLeavesCount();
         //  sts.printTermLeavesCount();
         //  sts.printFieldList();
            if (!sts.screenOutput)
                sts.pr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // End Main Function

} // End Class