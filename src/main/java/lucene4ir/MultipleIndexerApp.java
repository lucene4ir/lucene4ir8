package lucene4ir;

import lucene4ir.utils.XMLTextParser;
import java.util.ArrayList;

public class MultipleIndexerApp {
    private String indexType , filterFile , fileList ;
    private  String indexerAppFile = "params/index/temp_index_params.xml";

    private void setIndexerParameters (String indexName)
    {
        String biTokenFilterFile = "params/index/TokenFilterFile_Bigram.xml",
                uniTokenFilterFile = "params/index/TokenFilterFile_Unigram.xml",
                combinedTokenFilterFile = "params/index/TokenFilterFile_Combinedgram.xml";

        if (indexName.startsWith("WAPO"))
        {
            fileList = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\LUCENE\\anserini-master\\data\\WAPO\\WashingtonPost.v2\\data";
            indexType = "WAPO";
        }
        else if (indexName.startsWith("Core17"))
        {
            fileList = "C:/Users/kkb19103/Desktop/My Files 07-08-2019/LUCENE/anserini-master/data/nyt_corpus/data";
            indexType = "CommonCore";
        }
        else
        {
            fileList = "C:/Users/kkb19103/Desktop/My Files 07-08-2019/LUCENE/anserini-master/data/Aquaint";
            indexType = "TRECAQUAINT";
        }
        if (indexName.contains("Bigram"))
            filterFile = biTokenFilterFile;
        else if (indexName.contains("Unigram") || indexName.contains("Fielded"))
            filterFile = uniTokenFilterFile;
        else if (indexName.contains("Combined"))
            filterFile = combinedTokenFilterFile;
    } // End Function

    private void createIndexerByName(String indexName) {
        String  outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\Indexes\\";
        setIndexerParameters(indexName);

        XMLTextParser parser = new XMLTextParser(indexerAppFile);
        parser.setTagValue("indexName", indexName);
        parser.setTagValue("tokenFilterFile", filterFile);
        parser.setTagValue("fileList",fileList);
        parser.setTagValue("indexType",indexType);
        parser.save();
        IndexerApp indexer = new IndexerApp(indexerAppFile);
        ArrayList<String> files = indexer.readFileListFromFile();
        for (String file : files) {
            System.out.println("About to Index Files in: " + file);
            indexer.indexDocumentsFromFile(file);
        }
        indexer.finished();
        System.out.println("Done Building Index " + indexName);
    }

    private void createIndexerByFolder(String indexerAppFolderPath)
    {
        IndexerApp indexer = new IndexerApp(indexerAppFolderPath);
        ArrayList<String> files = indexer.readFileListFromFile();
        for (String file : files) {
            System.out.println("About to Index Files in: " + file);
            indexer.indexDocumentsFromFile(file);
        }
        indexer.finished();
        System.out.println("Done Building Index ");
    }

    public static void main(String[] args) {
      /*  String[] indexNames = { "Core17UnigramIndex", "Core17BigramIndex" ,"Core17CombinedIndex"
                ,"AquaintUnigramIndex" , "AquaintBigramIndex","AquaintCombinedIndex"};*/

        String[] indexNames = { "WAPOUnigramIndex", "WAPOBigramIndex","WAPOCombinedIndex"};
        MultipleIndexerApp mn = new MultipleIndexerApp();
        for (int i = 0; i < indexNames.length; i++)
          mn.createIndexerByName(indexNames[i]);
    } // End function
}
