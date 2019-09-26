/*
    This Class is Used to extract Bigrams from a Lucene Index and Calculate their scores
    Based on the following book  :
    Title : "Foundations of Statistical Natural Language Processing" By
    Author : Christopher   D.   Manning and Hinrich  Schiitze
    URL : https://www.cs.vassar.edu/~cs366/docs/Manning_Schuetze_StatisticalNLP.pdf
    Mutual Information
    Browser Page (206) - Paper Page (178)
*/

// Import Section
package lucene4ir;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

public class TwoWordsQueryGeneratorApp {

    // Public Variables
    public TwoWordsQueryGeneratorParams p;
    public HashMap<String, Long> unigramMap;

    // Private Variables
    private double log2;
    private long totalTermCount = 0; // Totel Count of Unigram Terms
    private long totalBiTermCount = 0; // Totel Count of Bigram Terms
    private IndexReader reader; // Shared index reader between different functions
    private String inputParameterFile;

    // Constructor Method
    public TwoWordsQueryGeneratorApp(String inFile) {
        // Constructor Method to initialize main instance variables
        inputParameterFile = inFile;
        unigramMap = new HashMap<String, Long>();
        log2 = Math.log10(2);
    } // End Function

    private void displayMsgThenExit(String msg)
    {
        // This function is usually used to display a message then stop the process
        System.out.println(msg);
        System.exit(0);
    }// End Function

    private void readParamsFromFile(String paramFile) throws Exception{
        /*
        This function is used to :
         1- Read XML parameters file
         2- Fill these parameters into local class parameter
         3- Create Index Reader
         */
        System.out.println("Reading Parameter File");
        p = JAXB.unmarshal(new File(paramFile), TwoWordsQueryGeneratorParams.class );
        if (p.indexName.isEmpty())
            displayMsgThenExit("IndexName Parameter is Missing");
        System.out.println("Index: " + p.indexName);
        if (p.outFile.isEmpty())
            displayMsgThenExit("Query Output File Parameter is Missing");
        if (p.cutoff < 1)
            p.cutoff = 0;
        System.out.println("biGram Cutoff: " + p.cutoff);
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(p.indexName)));

    } // End Function

    private long getUnigramFrequency (String term)
    {
        // Given a unigram term , get its frequency from the document map or return 1 as a default
        long result = 1; // Default 1
        if (unigramMap.containsKey(term))
            result = unigramMap.get(term);
        return result;
    } // End Function

    private void createUnigramList(String field) throws Exception {
        /*
        This Function is used to :
            1- Use the index reader to read the source index
            2- Create Unigram term Map (Term - Frequency )
                for all unigram terms in the index  ( to be used later to calculate bigram score)
            3- Counting the total Unigrams and total Bigrams in the index
         */

        // Local Variables
        totalTermCount = 0;
        totalBiTermCount = 0;

        // I have reservations about doing this - what if there are multiple leaves????
        // Initialization

        LeafReader leafReader = reader.leaves().get(0).reader();
        String currentTerm;
        long currentFreq;
        Terms terms = leafReader.terms(field);
        TermsEnum te = terms.iterator();
        BytesRef term;

        // Iterate through all terms in the Index
        while ((term = te.next()) != null) {
            currentTerm = term.utf8ToString().trim();
            if (!currentTerm.contains("_"))
            {
                currentFreq = te.totalTermFreq();
                if (currentTerm.contains(" "))
                    // is a bigram or greater ... could be a trigram...
                    totalBiTermCount += currentFreq;
                else {
                    totalTermCount += currentFreq;
                    // may have to check if term is already in the map, and if so, add to it.
                    if (unigramMap.containsKey(currentTerm))
                        unigramMap.put(currentTerm, unigramMap.get(currentTerm)+currentFreq);
                    else
                        unigramMap.put(currentTerm, currentFreq);
                } // End Else {
            } // End  if (!currentTerm.contains("_"))
        } // End while
    } // End Function

    private void extractBigrams(String field) throws Exception {
        // iterate through all bigrams in the collection
        // compute score
        // output to file -> (qid, t1, t2, n(t1), n(t2), n(t1, t2), score

        // Local Variables
        PrintWriter outBigrams = new PrintWriter(p.outFile);
        long id = 0 , t1Frequency , t2Frequency;
        String termsList[];
        double pti = 0.0;
        double ptj = 0.0;
        double ptij = 0.0;
        LeafReader leafReader = reader.leaves().get(0).reader();
        String currentTerm;
        long currentFreq;
        Terms terms = leafReader.terms(field);
        TermsEnum te = terms.iterator();
        BytesRef term;
        // -------------------------------------
        // Iterate through all terms in the Index
        while ((term = te.next()) != null) {
            currentTerm = term.utf8ToString().trim();
            if (!currentTerm.contains("_"))
            {
                currentFreq = te.totalTermFreq();
                if (currentTerm.contains(" ") &&  currentFreq >= p.cutoff) {
                    // is a bigram or greater ... could be a trigram...
                    id++;
                    // compute the bigram score ( biterm, freq)
                    termsList = currentTerm.split(" ");
                    t1Frequency = getUnigramFrequency(termsList[0]);
                    t2Frequency = getUnigramFrequency(termsList[1]);

                    pti = (t1Frequency + 0.0)/totalTermCount;
                    ptj = (t2Frequency +0.0)/totalTermCount;
                    ptij = (currentFreq +0.0)/totalBiTermCount;
                    double score = calculatePMI(pti,ptj, ptij);
                    System.out.println(currentTerm);
                    outBigrams.write(String.format("%d %s %d %d %d %f\n",id, currentTerm,t1Frequency, t2Frequency,currentFreq, score ));
                } // End (currentTerm.contains(" ") &&  currentFreq >= p.cutoff)
            } // End if (!currentTerm.contains("_"))
        } // End while
        // Close and Save output Writer
        outBigrams.close();
        leafReader = null;
    } // End Function





  /*  private void outputUnigrams() throws Exception
    {

       *//* Output UnigramMap ( Term , Weight )*//*

        String term , line;
        int freq , ctr = 0;
        Iterator it;

        it = unigramMap.entrySet().iterator();
        Map.Entry item;
        PrintWriter outUnigrams = new PrintWriter(p.outFile);
        while (it.hasNext())
        {
            item = (Map.Entry) it.next();
            term = item.getKey().toString();
            freq = Integer.parseInt(item.getValue().toString());
            line = String.format("%d %s %d\n" , ++ctr , term , freq );
           // line = String.format("%s %d\n" ,  term , freq);
            outUnigrams.write(line);
        } // End While
        outUnigrams.close();
    } // End Function*/


    private void close()
    {
        // Close function to release memory
        unigramMap = null;
    } // End Function

    private double calculatePMI(double pi, double pj, double pij)
    // Calculates the Pointwise Mutual Information between two terms
    // See TEXTBOOK Statistical Natural Language Processing by manning, schutze, et al, Page XX
    // pi, pi, pij - probability of the term i, term i and term i and j
    {
        double score;
        score = Math.log10(pij / (pi * pj)) / log2;
        return score;
    } // End Function

    public void createBigrams() throws Exception {
        // check if the index has shingles of size 2

        String fldName = lucene4ir.Lucene4IRConstants.FIELD_RAW;
        // Read Parameters File
        readParamsFromFile(inputParameterFile);
        // create array with term list i.e. unigrams (term, count)
        createUnigramList(fldName);
        //outputUnigrams();

        // extract bigrams
        extractBigrams(fldName);
        reader.close();
        close();
        System.out.println("Done Successfully");
    } // End Function

    public static void main (String args[])
    {
        // RunExperimentsRetrievabilityCalculatorApp (Mystro Function) - Coordinate the process
        String bigramParamFile = "";
        try {
            bigramParamFile = args[0];
            TwoWordsQueryGeneratorApp bga = new TwoWordsQueryGeneratorApp(bigramParamFile);
            bga.createBigrams();

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        } // End Catch
    } // End Function
} // End Class

class TwoWordsQueryGeneratorParams {
    public String indexName;
    public String outFile;
    public int cutoff;
} // End Class
