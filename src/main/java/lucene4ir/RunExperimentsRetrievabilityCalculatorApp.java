package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.utils.XMLTextParser;

public class RunExperimentsRetrievabilityCalculatorApp {


    private String retirievalParamsFile = "params/temp_retrieval_params.xml",
          //  retrievalOutDir = "out/unigram/300K",
            retrievalOutDir = "out/unigramIndex/300K/BigramQuery",
            retrievabilityParamsFile = "params/temp_retrievability_calculator_params.xml";

    private void runRetrievalExperiment(String b)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName;

        XMLTextParser parser = new XMLTextParser(retirievalParamsFile);
        outFileName = retrievalOutDir + "/result" + b + ".res";
        parser.setTagValue("b", b );
        parser.setTagValue("resultFile",outFileName);
        parser.save();
        RetrievalApp retApp = new RetrievalApp(retirievalParamsFile);
        retApp.processQueryFile();
    } // End Function

    private void runRCExperiment (String b)
    {
        // Run Retrievability Calculator Experiments for given B Value
        String outFileName;

        XMLTextParser parser = new XMLTextParser(retrievabilityParamsFile);
        outFileName = retrievalOutDir + "/result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = retrievalOutDir +  "/RCResults" + b + ".ret";
        parser.setTagValue("retFile",outFileName);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(retrievabilityParamsFile);
        rcApp.calculate();
    } // End Function


    private void runWholeExperiment (String b)
    {
        // Run RetrievalApp & Retrievability Calculator Experiments for given B Value
        runRetrievalExperiment(b);
        runRCExperiment(b);
    } // End Function

    private void runRetrievalStatistics (String b)
    {
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = "out/unigramIndex/300K/BigramQuery/result" + b + ".res";
        sts.calculateStatistics(outFileName,"",100);
        System.out.println(String.format("%s %d %d %d",b , sts.lineCtr , sts.docCtr , sts.limitedQryCtr));
    }


    private void runRCStatistics (String b)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = "out/bigramIndex/50/2017/RCResults" + b + ".ret";
        sts.calculateStatistics(outFileName);
        System.out.println(String.format("%s %1.6f %d %d %1.4f %1.4f",b ,sts.G , sts.nonZeroDocCtr , sts.zeroDocCtr , sts.sum , sts.avg ));

    }



    public static void main(String[] args) {
	// write your code here
        String outFileName , b;
      // RunExperimentsRetrievabilityCalculatorApp re = new RunExperimentsRetrievabilityCalculatorApp();
          StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
      //  StatisticsRetrieval sts = new StatisticsRetrieval();
        for (int i = 1 ; i < 10 ; i++)
        {
            b = "0." + i ;

           // re.runRCExperiment(b);
           // re.runWholeExperiment(b);
        } // End For
    }
}
