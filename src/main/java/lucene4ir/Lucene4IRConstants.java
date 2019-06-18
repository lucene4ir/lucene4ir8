package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class Lucene4IRConstants {

    // Common analyzer
    public static final Analyzer ANALYZER = new StandardAnalyzer();

    // Field names from Anserini
    public static final String FIELD_ID = "id";
    public static final String FIELD_DOCNUM = FIELD_ID;
    public static final String FIELD_RAW = "raw";
    public static final String FIELD_BODY = "contents";


    // Additional fields for trec collections
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_URL = "url";
    public static final String FIELD_DOCHDR = "dochdr";
    public static final String FIELD_PUBDATE = "pubdate";
    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_HDR = "hdr";
    public static final String FIELD_ANCHOR = "anchor";

}
