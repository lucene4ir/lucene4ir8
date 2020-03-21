import os

def extractLines(dir):
    previousID = 0
    newLines = []
    destDir = dir.replace("C1000","C10")
    for fName in os.listdir(dir):
        if (fName.endswith('res')):
            f = open(dir + '\\' + fName,'r')
            for line in f:
                qryid = line.split(" ",2)[0]
                if (previousID != qryid):
                    i = 1
                    newLines.append(line)
                    previousID = qryid
                elif (previousID == qryid and i < 10):
                    newLines.append(line)
                    i += 1
            f.close()
            if (newLines.__len__() > 0):
                if (not os.path.isdir(destDir)):
                    os.mkdir(destDir)
                f = open (destDir + '\\' + fName,'w')
                for line in newLines:
                    f.write(line)
                f.close()
def main():
    #dir = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\Aquaint\BigramIndex\50\C1000"
    corpus = ['Aquaint',"Core17"]
    index = ["UnigramIndex","BigramIndex","CombinedIndex","FieldedIndex"]

    for c in corpus:
        for i in index:
          dir = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\%s\%s\50\C1000" % (c , i)
          extractLines(dir)

if __name__ == '__main__':
    main()