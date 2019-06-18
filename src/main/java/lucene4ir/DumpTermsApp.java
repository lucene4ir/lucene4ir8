package lucene4ir;

        import javax.xml.bind.JAXB;
        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Paths;
        import java.util.*;
        import org.apache.lucene.index.Term;
        import org.apache.lucene.index.Terms;
        import org.apache.lucene.index.TermsEnum;
        import org.apache.lucene.index.MultiTerms;
        import lucene4ir.utils.LanguageModel;
        import org.apache.lucene.analysis.Analyzer;
        import org.apache.lucene.analysis.TokenStream;
        import org.apache.lucene.analysis.standard.StandardAnalyzer;
        import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
        import org.apache.lucene.index.*;
        import org.apache.lucene.index.memory.MemoryIndex;
        import org.apache.lucene.document.Document;
        import org.apache.lucene.search.IndexSearcher;
        import org.apache.lucene.store.FSDirectory;
        import org.apache.lucene.util.BytesRef;

        import org.apache.lucene.search.CollectionStatistics;

/**
 * Created by leif on 21/08/2016.
 */

public class DumpTermsApp {

    public String indexName;
    public IndexReader reader;

    public DumpTermsApp() {
        System.out.println("DumpTerms");
        /*
        Shows a number of routines to access various term, document and collection statistics

        Assumes index has a docnum (i.e. trec doc id), title and content fields.
         */
        indexName = "";
        reader = null;
    }


    public void readParamsFromFile(String indexParamFile) {
        try {
            IndexParams p = JAXB.unmarshal(new File(indexParamFile), IndexParams.class);
            indexName = p.indexName;

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }

    public void openReader() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    public void numSegments(){
        // how do we get the number of segements
        int segments = reader.leaves().size();
        System.out.println("Number of Segments in Index: " + segments);

        // you can use a writer to force merge - and then you will only
        // have one segment
        // the maximum number of documents in a lucene index is approx 2 millio
        // you need to go solr or elastic search for bigger collections
        // solr/es using sharding.
    }


    public void termsList(String field) throws IOException {

        // again, we'll just look at the first segment.  Terms dictionaries
        // for different segments may well be different, as they depend on
        // the individual documents that have been added.
        LeafReader leafReader = reader.leaves().get(0).reader();

        System.out.println(reader.leaves().size());
        Terms terms = leafReader.terms(field);

        // The Terms object gives us some stats for this term within the segment
        System.out.println("Number of docs with this term:" + terms.getDocCount());

        TermsEnum te = terms.iterator();
        BytesRef term;
        while ((term = te.next()) != null) {
            System.out.println(term.utf8ToString() + " DF: " + te.docFreq() + " CF: " + te.totalTermFreq());
        }
    }



    public void termTest(String field)  throws IOException {
        Terms terms = MultiTerms.getTerms(reader, field);
        TermsEnum termsEnum = terms.iterator();
        BytesRef text;
        int cnt = 0;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            if (term.length() == 0) continue;

            Term t = new Term(field, term);
            long df  = reader.docFreq(t);
            long cf = reader.totalTermFreq(t);
            System.out.println(term + " DF: " + df + " CF: " + cf );

        }

    }




    public void reportCollectionStatistics()throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);

        CollectionStatistics collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_RAW);
        long token_count = collectionStats.sumTotalTermFreq();
        long doc_count = collectionStats.docCount();
        long sum_doc_count = collectionStats.sumDocFreq();
        long avg_doc_length = token_count / doc_count;

        System.out.println("ALL: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);

        collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_TITLE);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();
        avg_doc_length = token_count / doc_count;

        System.out.println("TITLE: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);


        collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_BODY);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();
        avg_doc_length = token_count / doc_count;

        System.out.println("CONTENT: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);

    }



    public static void main(String[] args)  throws IOException {
        String statsParamFile = "";

        try {
            statsParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        DumpTermsApp dtApp = new DumpTermsApp();
        dtApp.readParamsFromFile(statsParamFile);

        dtApp.openReader();
        dtApp.reportCollectionStatistics();
        //dtApp.termsList(Lucene4IRConstants.FIELD_ALL);

        dtApp.termTest(Lucene4IRConstants.FIELD_RAW);



    }

}


class DumpTermsParams {
    public String indexName;
}
