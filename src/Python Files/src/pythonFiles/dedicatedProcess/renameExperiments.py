import os
sourcePath = r'I:\Science\CIS\abdulaziz\CORE17 25-02-2020'

def getCoefficient(fPath):
    models = ['BM25','PL2','LMD']
    coefficients = ['b' , 'c' , 'mu']
    for i in range(3):
        if (fPath.find(models[i]) > -1):
            break
    result = coefficients[i]
    return result

def getFileName (fPath):
    global sourcePath
    targetPath = sourcePath
    result = ""
    folder = ""
    sep = '\\'
    if fPath.endswith('.sh'):
        folder = "Bash"
        result = fPath
    else:
        c = getCoefficient(fPath)
        if (fPath.endswith('.res')):
            # Res File
            result = fPath.replace('result',c)
            folder = "Res"
        elif (fPath.endswith('.trec')):
            result = fPath.replace('-trec', '-' + c)
            folder = "Trec"
        elif (fPath.endswith('.ret')):
            result = fPath.replace('RCResults', c).replace('-0-','-gb0-').replace('-0.5-','-gb0.5-')
            folder = "Ret"
    result = sep.join([targetPath, folder,result])
    return result
   #  # AQUAINT - CORE17 - WAPO
   #  corpus = 'WAPO'
   # # targetPath += sep + corpus + sep
   #  targetPath += sep
   #  corpus = corpus[:2].upper()
   #  inPath = fPath.replace(sourcePath + sep,'')
   #  parts = inPath.split(sep)
   #  modelIndex = 0
   #  # model = parts[modelIndex]
   #  model = parts[modelIndex]
   #  index = parts[modelIndex + 1][0].upper() + "I"
   #  qry = parts[modelIndex + 2]
   #  c = parts[modelIndex + 3]
   #  if (inPath.endswith('ret')):
   #      b = parts[modelIndex + 4]
   #      if (b == "Cumulative"):
   #          b = '0'
   #      else:
   #          b = '0.5'
   #      fName = b + '-' + parts[modelIndex + 5]
   #  else:
   #      fName = parts[modelIndex + 4]
   #
   #  result = '-'.join([corpus,model,index,qry,c,fName])

def listFiles (dirName):
    folder = os.listdir(dirName)
    for fileName in folder:
        fullPath = dirName + '\\' + fileName
        # if (os.path.isdir(fullPath)):
            # listFiles(fullPath)
            # fullPath =  ''
        # else:
        if (os.path.isfile(fullPath)):
            newFullPath = getFileName(fileName)
            if newFullPath != '':
                # os.rename(fullPath,newFullPath)
                 print (fullPath + '\n' + newFullPath + '\n')
def main():
    global sourcePath
    listFiles(sourcePath)
if __name__ == '__main__':
    main()