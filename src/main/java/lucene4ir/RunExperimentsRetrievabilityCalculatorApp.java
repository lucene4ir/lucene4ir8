package lucene4ir;

import lucene4ir.utils.XMLTextParser;

public class RunExperimentsRetrievabilityCalculatorApp {


    private String retirievalParamsFile = "params/temp_retrieval_params.xml",
            retrievalOutDir = "out/G/300K/B/",
            retrievabilityParamsFile = "params/temp_retrievability_calculator_params.xml";

    private void runRetrievalExperiment(String b)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName;

        XMLTextParser parser = new XMLTextParser(retirievalParamsFile);
        outFileName = retrievalOutDir + "result" + b + ".res";
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
        outFileName = retrievalOutDir + "result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = retrievalOutDir + "RCResults" + b + ".ret";
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



    public static void main(String[] args) {
	// write your code here
        String outFileName , b;
       RunExperimentsRetrievabilityCalculatorApp re = new RunExperimentsRetrievabilityCalculatorApp();
        for (int i = 1 ; i < 3 ; i++)
        {
            b = "0." + i ;
            re.runRCExperiment(b);
        } // End For
    }
}
