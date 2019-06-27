package main.java.lucene4ir;


//import main.java.lucene4ir.Lucene4IRConstants;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;

public class BigramGeneratorApp {

    // Public Variables
    public BigramGeneratorParams p;
    public HashMap<String, Long> unigramMap;
    private double log2;
    private long totalTermCount = 0;
    private long totalBiTermCount = 0;
    public IndexReader reader;


    // Constructor Method
    public BigramGeneratorApp(String inputParameterFile) {
        readParamsFromFile(inputParameterFile);
        unigramMap = new HashMap<String, Long>();
        log2 = Math.log10(2);
    }

    private void exitForParameterError(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    }
    private void readParamsFromFile(String paramFile){
        System.out.println("Reading Parameter File");
        try {
            p = JAXB.unmarshal(new File(paramFile), BigramGeneratorParams.class );
            if (p.indexName.isEmpty())
                exitForParameterError("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.outFile.isEmpty())
                exitForParameterError("Query Output File Parameter is Missing");
            if (p.cutoff < 1)
                p.cutoff = 0;
            System.out.println("biGram Cutoff: " + p.cutoff);

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }


    private long getUnigramFrequency (String term)
    {
        long result = 1; // Default 1
        if (unigramMap.containsKey(term))
            result = unigramMap.get(term);
        return result;
    }


    private void createUnigramList(String field) throws Exception {

        totalTermCount = 0;
        totalBiTermCount = 0;


        reader = DirectoryReader.open(FSDirectory.open(Paths.get(p.indexName)));
        // I have reservations about doing this - what if there are multiple leaves????
        LeafReader leafReader = reader.leaves().get(0).reader();

        String currentTerm;
        long currentFreq;
        Terms terms = leafReader.terms(field);
        TermsEnum te = terms.iterator();

        BytesRef term;
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
                }
            }

        }
    }

    private void extractBigrams(String field) throws Exception {
        // iterate through all bigrams in the collection
        // compute score
        // output to file -> (qid, t1, t2, n(t1), n(t2), n(t1, t2), score

        PrintWriter outBigrams = new PrintWriter(p.outFile);
        long id = 0;
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
                    pti = (getUnigramFrequency(termsList[0])+0.0)/totalTermCount;
                    ptj = (getUnigramFrequency(termsList[1])+0.0)/totalTermCount;
                    ptij = (currentFreq +0.0)/totalBiTermCount;
                    double score = calculatePMI(pti,ptj, ptij);
                    System.out.println(currentTerm);
                    outBigrams.write(String.format("%d %s %d %d %d %f\n",id, currentTerm, getUnigramFrequency(termsList[0]), getUnigramFrequency(termsList[1]),currentFreq, score ));
                }

            }

        }
        outBigrams.close();
    }


    private double calculatePMI(double pi, double pj, double pij)
    // Calculates the Pointwise Mutual Information between two terms
    // See TEXTBOOK Statistical Natural Language Processing by manning, schutze, et al, Page XX
    // pi, pi, pij - probability of the term i, term i and term i and j
    {
        double score;
        score = Math.log10(pij / (pi * pj)) / log2;
        return score;
    }


    public void createBigrams() throws Exception {
        // check if the index has shingles of size 2

        // create array with term list i.e. unigrams (term, count)
        createUnigramList(lucene4ir.Lucene4IRConstants.FIELD_RAW);
        // extract bigrams
        extractBigrams(lucene4ir.Lucene4IRConstants.FIELD_RAW);

    }


    public static void main (String args[])
    {
        String bigramParamFile = "";
        try {
            bigramParamFile = args[0];
            BigramGeneratorApp bga = new BigramGeneratorApp(bigramParamFile);
            bga.createBigrams();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }
}


class BigramGeneratorParams {
    public String indexName;
    public String outFile;
    public int cutoff;
}
