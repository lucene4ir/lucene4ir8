package lucene4ir.utils;
/*
This Class is used to cross all files in a directory even within inner directories
 */

import java.io.File;
import java.util.ArrayList;

public  class CrossDirectoryClass {
     public int fileCount, // The Number of crossed files
                         folderCount, // The Number of crossed folders
                maxFileDig; // The Maximum Number of files to take from each directory

     private ArrayList<String> fileList ; // The Resultant list of file names in a directory


    public CrossDirectoryClass()
    {
        // Constructor Method
        // Initialize RunExperimentsRetrievabilityCalculatorApp Variables and Counts
        fileCount = 0;
        folderCount = 0;
        fileList =  new ArrayList<String>() ;
        maxFileDig = 0;

    }

    private  void listDirectory(File aDir , boolean crossInner) {
        /*
        This is a recursive function to traverse all folders and all files in a Given Directory
        and fill the name of files in the input tempList
         */

        int maxFileCtr = 0;
        if (maxFileDig > 0 ) maxFileCtr = maxFileDig;

        for (File aFile : aDir.listFiles())
            if (aFile.isDirectory() )
            {
                if (crossInner)
                {
                    folderCount++;
                    listDirectory(aFile,crossInner);
                } // End if (crossInner)
            } // End (aFile.isDirectory()
            else {
                maxFileCtr--;
                fileCount++;
                fileList.add( aFile.getAbsolutePath() );
                if (maxFileDig > 0 && maxFileCtr <= 0 )
                    break;
            }
    }

    public ArrayList<String> crossDirectory(String inPath , boolean crossInner)
    {
        /*
        This function is used to traverse all files in the input Directory path
        Cross inner variable to identify whether to cross inner directories or not
        MaxFileDig for identifying the maximum number of files to take from each directory or else zero = all files
        */

        File directory = new File(inPath);
        // check  whether the directory is exist of not
        if (directory.exists())
            listDirectory(directory , crossInner);
        else
            System.out.println("Directory is not Found");
        return fileList;
    }


}
