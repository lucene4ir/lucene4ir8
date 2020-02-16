package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.parse.CSVParser;

import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class RunExperimentSet {
    private ExperimentSetParams p;

    private String
           /* biFilter = "params/index/p.tokenFilterFile_Bigram.xml",
            uniFilter = "params/index/p.tokenFilterFile_Unigram.xml" ,
            combinedFilter = "params/index/p.tokenFilterFile_Combinedgram.xml" ,
            tokenFilterFile = combinedFilter,*/
            defaultCSVKey;
    private CSVParser csvPaser;

    private String getCorpus (String indexName)
    {
        String result = "";

        if (!indexName.isEmpty())
            if (indexName.contains("Core17"))
                result = "Core17";
            else  if (indexName.contains("WAPO"))
                result = "WAPO";
            else
                result = "Aquaint";
        return result;
    } // End Function


    private String getBashLine(String b)
    {
        String qrelFile , trecEvalLine , corpus;
        corpus = getCorpus(p.indexName);
        switch (corpus)
        {
            case "Core17":
                qrelFile = "307-690.qrels";
                break;
            case "WAPO":
                qrelFile = "qrels.core18.txt";
                break;
            default:
                qrelFile = "trec2005.aquaint.qrels";
        } // End switch

        trecEvalLine  = String.format("~/trecEval/trec_eval ~/trecEval/Qrels/%s ./result%s.res > ./trec%s.trec\n" ,
                qrelFile,b,b);
        return trecEvalLine;
    } // End Function

   /* private void initDocumentMap() throws Exception {

        docMap = new HashMap<String, Double>();
        // Initialize Document Hash MAP (docid , r = 0)
        IndexReader reader;
        long docCount;
        String docid;
        final String docIDField = Lucene4IRConstants.FIELD_DOCNUM;
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(p.indexName)));
        docCount = reader.maxDoc();

        for (int i = 0; i < docCount; i++)
        {
            docid = reader.document(i).get(docIDField);
            docMap.put(docid,0.0);
        }
        reader.close();
    } // End Function*/

    private void fillAutoParameters()
    {
        /*
        Fill Parameters Automatically
        Fill QueryFile based on Max Results
        if Max Results = 1000 - Query 50
        else Query 300K
        + fill The outputDir Automatically based on MaxResults & IndexName
         Sample OutputDir
         out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
         */
        String qryCount = "" , indexFolder = "" , C = "", corpus , csvFile ,
        outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments";

        C = "C" + p.maxResults;
        if (validRetrievability())
      //  if (p.maxResults.equals("100"))
            qryCount = "300K";
        else
            qryCount = "50";

        corpus = getCorpus(p.indexName);
        p.queryFile = String.format("%s/%s/Queries/%s.qry" , outPutFolder, corpus , qryCount);

        if (p.indexName.contains("Combined"))
            indexFolder = "CombinedIndex";
        else if (p.indexName.contains("Unigram"))
            indexFolder = "UnigramIndex";
        else if (p.indexName.contains("Bigram"))
            indexFolder = "BigramIndex";
        else if (p.indexName.contains("Fielded"))
            indexFolder = "FieldedIndex";

        //   Sample Output
        //   out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
        p.outputDir = String.format("%s/%s/%s/%s/%s/%s" ,
                outPutFolder , corpus , p.model , indexFolder , qryCount , C);
        csvFile = outPutFolder + "/out.csv";
        csvPaser = new CSVParser();
        csvPaser.readFromFile(csvFile);

        /*CSV Key
        corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient (B , mu , c) */
        defaultCSVKey = String.format("%s,%s,%s,%s,%s,%s,",
                corpus,indexFolder , "combinedQuery" , qryCount , p.model , p.maxResults);
        p.indexName = outPutFolder + "\\Indexes\\" + p.indexName;
    }
    private void readParamsFromFile(String paramFile) throws Exception
    {
        // Read All Parameters and check that all of them are true
        p = JAXB.unmarshal(new File(paramFile), ExperimentSetParams.class);
       fillAutoParameters();
    } // End Function

    private void printOutput (String outPath , String outText) throws Exception
    {
        PrintWriter pr = new PrintWriter(outPath);
        pr.write(outText);
        pr.close();
    }
    private void fillExperimentParameterFile(String fileName , String model , String indexName , String maxResults)
    {
        XMLTextParser parser = new XMLTextParser(fileName);
        parser.setTagValue("indexName",indexName);
        parser.setTagValue("maxResults",maxResults);
        parser.setTagValue("model",model);
        parser.save();
    } // End Function

    private String getRetTypeFolder(String retType)
    {
        String result = "";
        if (retType == "Gravity")
            result =   "\\GravityWeightB0.5C" + p.maxResults;
        else
            result =   "\\Cumulative";
        return result;
    }

    private String runRetrievalStatistics (String b)
    {
        int maxResultsInt;
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "\\result" + b + ".res";
        maxResultsInt = Integer.parseInt(p.maxResults);
        sts.calculateStatistics(outFileName,"",maxResultsInt);
        return String.format("%d,%d,%d", sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
    }
    private String runRCStatistics (String b , String retType)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = p.outputDir + getRetTypeFolder(retType) +  "/RCResults" + b + ".ret";
        sts.calculateStatistics(outFileName);
        return String.format("%1.6f,%d,%1.4f",sts.G , sts.zeroDocCtr , sts.sum );
    }

    private String runRetrievalExperiment(String b)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName;
        FieldedRetrievalApp fRetApp;
        RetrievalApp retApp;

        XMLTextParser parser = new XMLTextParser(p.retrievalParamsFile);
        outFileName = p.outputDir + "/result" + b + ".res";
        parser.setTagValue("c", b );
        parser.setTagValue("resultFile",outFileName);
        parser.setTagValue("queryFile",p.queryFile);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("maxResults",p.maxResults);
        parser.save();
        if (p.indexName.contains("Fielded"))
        {
            fRetApp = new FieldedRetrievalApp(p.retrievalParamsFile);
            fRetApp.processQueryFile();
        } // End IF
        else
        {
            retApp = new RetrievalApp(p.retrievalParamsFile);
            retApp.processQueryFile();
        } // End Else
        return runRetrievalStatistics(b);
    } // End Function

    private String runRCExperiment (String b , String retType)
    {
        // Run Retrievability Calculator Experiments for given B Value
        String outFileName ;
        XMLTextParser parser = new XMLTextParser(p.retrievabilityParamsFile);
        outFileName = p.outputDir + "\\result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = p.outputDir + getRetTypeFolder(retType) +  "\\RCResults" + b + ".ret";
        parser.setTagValue("retFile",outFileName);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("c",p.maxResults);
        parser.setTagValue("retType",retType);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(p.retrievabilityParamsFile);
        rcApp.calculate();
        return runRCStatistics(b,retType);
    } // End Function
    private String getPerformanceValues(String b) throws Exception
    {
        String inFile ,
                line,
                result = "",
                keys[] = {"map","bpref","P10"};
        BufferedReader br;
        int i = 0;
        inFile = p.outputDir + "\\trec" + b + ".trec";
        br = new BufferedReader(new FileReader(inFile));

        while ((line = br.readLine()) != null && i < keys.length )
            if (line.startsWith(keys[i]))
            {
                result += "," + line.split("\t",3)[2];
                i++;
            } // End if

        return result.replaceFirst(",","");
    } // End Function

    private String getNewKey (String retB , String b)
    {
        return defaultCSVKey + retB + "," + b;
    }

    private boolean validRetrievability()
    {
        return p.maxResults.equals("100");
    }
    private void runALLRC (String b)
    {
        String result ;
        // Calculation
        result = runRCExperiment(b,"Cumulative");
        // *** Statistics ***
        csvPaser.setRet(result);
        csvPaser.addCSVLineToMap();

        result = runRCExperiment(b,"Gravity");
        csvPaser.setKey(getNewKey("0.5",b));
        csvPaser.setRet(result);
}

private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
        return LocalDateTime.now().format(dtf);
    }

    private String[] getbRange()
    {
        /*
        Get Coefficient Values based on input model
         */
        String[]
                 BM25Set = {"0.1", "0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0"},
                 PL2Set = {"0.1", "0.5", "1.0", "5.0", "10", "15", "20", "50"},
                 //PL2Set = {"0.1", "0.5"},
                 LMDSet = {"100","200","300","400","500","600","700","800","900","1000","5000"},
                 result;
        if (p.model.equals("BM25"))
            result = BM25Set;
        else if (p.model.equals("PL2"))
            result = PL2Set;
        else result = LMDSet;
        return result;
    }

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String b , retB  = "0", bashLines = ""  ;

         String bRange[] = getbRange();

         for (int i = 0; i < bRange.length; i++) {
             b = bRange[i];
         if (p.exType.equals("Gravity"))
             retB = "0.5";
        csvPaser.newLine(getNewKey(retB,b));

        switch (p.exType)
            {
                case "All":
                    csvPaser.setRes(runRetrievalExperiment(b));
                    if (validRetrievability())
                        runALLRC(b);
                    else
                        bashLines += getBashLine(b);
                    break;
                case "AllRC":
                    runALLRC(b);
                    break;
                case "Cumulative":
                case "Gravity":
                    if (validRetrievability())
                        csvPaser.setRet(runRCExperiment(b,p.exType));
                    break;

                case "Retrieval":
                    csvPaser.setRes(runRetrievalExperiment(b));
                    bashLines += getBashLine(b);
                    break;
                case "Performance" :
                    csvPaser.setPerformance(getPerformanceValues(b));
                    break;
                case "ReadSts":
                    if (p.maxResults.equals("1000"))
                        csvPaser.setPerformance(getPerformanceValues(b));
                    csvPaser.setRes(runRetrievalStatistics(b));
                    if (validRetrievability())
                    {
                        csvPaser.setRet(runRCStatistics(b,"Cumulative"));
                        csvPaser.addCSVLineToMap();
                        csvPaser.setKey(getNewKey("0.5",b));
                        csvPaser.setRet(runRCStatistics(b,"Gravity"));
                    } // End if
            } // End Switch
            csvPaser.addCSVLineToMap();

        } // End For

        if (!bashLines.isEmpty())
            printOutput(p.outputDir + "/bash.sh" , bashLines);
        csvPaser.updateCSVFile();

    } // End Function

    private void runExperimentFile (String fileName)
    {
        try {
            System.out.println("Experiment " + fileName + " started at : " + getCurrentTime());
            readParamsFromFile(fileName);
            processExperimentSet();
            System.out.println("Experiment " + fileName + " is done successfully at : " + getCurrentTime());
         } // End Try
            catch (Exception e) {
            e.printStackTrace();
        } // End Catch
    } // End Function

    private void runFileList (String folderName) {
        CrossDirectoryClass cr = new CrossDirectoryClass();
        ArrayList<String> fileList = cr.crossDirectory(folderName, false);
        RunExperimentSet re = new RunExperimentSet();
        for (String file : fileList)
            runExperimentFile(file);
    }

    private void runCalculatedList ()
    {
        int t = 0;
        String paramFileName =  "params/BMExperimentSets/Experiment2.xml";
       String[] indexNames = {
              /* "AquaintBigramIndex","AquaintCombinedIndex","AquaintUnigramIndex" , "AquaintFieldedIndex",
               "Core17UnigramIndex","Core17BigramIndex","Core17CombinedIndex"  , "Core17FieldedIndex"*/
              // "WAPOUnigramIndex","WAPOBigramIndex","WAPOCombinedIndex"  , "WAPOFieldedIndex"
               "WAPOUnigramIndex","WAPOBigramIndex","WAPOCombinedIndex"  , "WAPOFieldedIndex"
       };
       String maxResults[] = {"100","1000"};
       //String models[] = {"LMD","PL2"};
        String models[] = {"PL2","LMD"};

       for (int m = 0 ; m < models.length ; m++)
       {
           if (m == 0)
               t=2;
           else
               t=0;
           for ( ; t < indexNames.length ; t++)
           {
               for (int j=0 ; j < maxResults.length ; j++ )
               {
                   fillExperimentParameterFile(paramFileName , models[m] , indexNames[t] , maxResults[j]);
                   runExperimentFile(paramFileName);
               } // End For J
           } // End For I
       }


       // runExperimentFile(paramFileName);
    }

    public static void main(String[] args)
        {
        // write your code here
            String sourceFile = "params\\BMExperimentSets\\Experiment2.xml",
                    result;
        RunExperimentSet re = new RunExperimentSet();
        re.runCalculatedList();
        // re.runExperimentFile(sourceFile);

    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            maxResults,
            queryFile,
            exType,
            model,
            retrievalParamsFile,
            retrievabilityParamsFile,
            outputDir;
}
