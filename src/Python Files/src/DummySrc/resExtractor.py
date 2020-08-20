import os

def extractResFile(sourceFile , destFile , numLines):
    sFile = open(sourceFile,'r')
    lines = []
    linectr = 1
    oldQryID = ""
    for line in sFile:
       qryID = line.split(" ",1)[0]
       if (qryID == oldQryID):
           if (linectr < numLines):
                linectr += 1
                lines.append(line)
       else:
            oldQryID = qryID
            linectr = 1
            lines.append(line)
    dFile = open(destFile, 'w')
    for line in lines:
        dFile.write(line)
    sFile.close()
    dFile.close()

def extractResPath(sourcePath):
   targetPath = sourcePath[:-2]
   if (not os.path.isdir(targetPath)):
      os.makedirs(targetPath)
   for i in range(10):
       # result100.res
       mu = (i + 1) * 100
       fileName = "\\result%s.res" % mu
       extractResFile(sourcePath+fileName ,targetPath+fileName , 10)
   fileName = "\\result%s.res" % 5000
   extractResFile(sourcePath + fileName, targetPath + fileName, 10)

def main():
    #sourcePath = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\Aquaint\BigramIndex\50\C1000"
    for collection in ["Aquaint", "Core17"]:
        for index in ["BigramIndex", "UnigramIndex", "CombinedIndex", "FieldedIndex"]:
            sourcePath = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\%s\%s\50\C1000" % (collection,index)
            extractResPath(sourcePath)

if __name__ == '__main__':
    main()