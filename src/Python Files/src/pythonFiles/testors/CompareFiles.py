def getFeature (ln):
    # Get specific feature to use for comparison
   parts = str(ln).split(" ",3)
   return parts[2]
   #return ln

def compare(path1,path2):
    # Compare between two input files line by line and print the differences between them
    f1 = open(path1,'r')
    f2 = open(path2,'r')
    i = 1
    ctr = 0
    for ln1 in f1:
       ln2 = f2.readline()
       if (ln1 != ln2):
            print ("Line Differ " + str(i))
            print('Line 1 : ' + ln1)
            print('Line 2 : ' + ln2)
            ctr += 1
       i += 1
    print ('Done with ' + str(ctr) + " Differences")


if __name__ == '__main__':
    mainPath = r'C:\Users\kkb19103\Desktop\TempFiles\source'
    path1 = mainPath + r'\AQ-BM25-UI-300K-C100-AX-fbdocs30-fbterms30-b0.75.res'
    path2 = mainPath + r'\AQ-BM25-UI-300K-C100-Baseline-fbdocs0-fbterms0-b0.75.res'
    compare(path1,path2)