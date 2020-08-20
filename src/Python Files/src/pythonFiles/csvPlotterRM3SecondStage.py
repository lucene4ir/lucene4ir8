# This Module is For Plotting Check Bigrams Charts
import src.pythonFiles.csvPlotterGeneral as gen

# Import Module in G Drive

# drivePath = '/content/gdrive'
# modPath = '/My Drive/Colab Notebooks'
# from google.colab import drive
# drive.mount (drivePath )
# import sys
# sys.path.append(drivePath + modPath)
# import csvPlotterGeneral as gen

"""
 *** Start Colab Form ***
 Input Data
 @title Plot Bias & Performance Experiments
"""
Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO","All"]
Model = "BM25"  # @param ["BM25", "LMD", "PL2" , "All"]
## @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
## @markdown Use Predefined ticks from ticks.csv files
##PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "Yes"  # @param ["Yes", "No"]

xAxis = "TrecMAP" # @param ["fbTerms", "TrecMAP","CWLMAP"]
yAxis = "G - Cumulative"  # @param ["G - Cumulative", "G - Gravity","TrecMAP","CWLMAP"]
# *** End Colab Form ***

def getSingleAxisValues(df , axis , fbDocs):
    criteria = df['fbDocs'] == fbDocs
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):

    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # yGroup = gen.getGroup(yAxisIndex)
    # if (yGroup == 'ret'):
    #     perType = 'Bias'
    # else:
    #     # Performance Extension (Trec or CWL)
    #     perType = gen.getPerformanceType(yAxisIndex)
    # if perType != '':
    #     pltTitle += '- ' + perType.upper() + ' Measurement'

    # PlotType Title
    pltTitle += '\n RM3 - Based on ' + gen.getModelCoefficient(model) + ' = ' + str(gen.getChosenCoefficient(model))

    # output Figure Name
    #figName = ''
    figName = gen.getFigureFileName(3,corpus,model,xAxisIndex , yAxisIndex)
    # if (gen.GCsvOutPath != ''):
    #     corpus = corpus[:2]
    #     xName = gen.getAxisName(xAxisIndex)
    #     yName = gen.getAxisName(yAxisIndex)
    #     b = ''
    #     if (yAxisIndex == gen.cumulativeGIndex ):
    #         b = 'GB0'
    #         figName = '-'.join(corpus,model,b,xName,yName)
    #     elif (yAxisIndex == gen.gravityGIndex):
    #         b = 'GB0.5'
    #         figName = '-'.join(corpus, model, b, xName, yName)
    #     else:
    #         figName = '-'.join(corpus, model, xName, yName)


    xTicks = ''
    # [0.22,0.27,0.01] [5,55,5]
    yTicks = ''
    legendTitle = 'fbDocs'

    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle , xTicks, yTicks, figName)

def csvPlot(Corpus, Model, DisplayData , xAxis , yAxis):

    if (xAxis in ["TrecMAP","CWLMAP"] and yAxis in ["TrecMAP","CWLMAP"]):
        print ('Invalid Axes choice , both axes are maps')
        return

    gen.initializeGlobals()
    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)
    yGroup = gen.getGroup(yAxisIndex)
    fbdocsRange = range(5, 55, 5)
    dfType = 'F'
    if (yGroup == 'ret'):
        fbdocsRange = [5, 10, 20]
        dfType = 'R'

    dfX = gen.getDataFrame(dfType , xAxisIndex , Corpus, Model, 0 )
    if (yAxis in ["TrecMAP","CWLMAP"]):
        dfY = dfX
    else:
        dfY = gen.getDataFrame(dfType , yAxisIndex , Corpus, Model, 0 )

    allXValues = []
    allYValues = []
    gen.setFigNum()
    for fbDocs in fbdocsRange:
        label = fbDocs
        marker = gen.getMarker(str(fbDocs))
        xValues = getSingleAxisValues(dfX,xAxisIndex,fbDocs)
        yValues = getSingleAxisValues(dfY,yAxisIndex,fbDocs)
        gen.plotList(xValues,yValues,label,marker)
        allXValues += list(xValues)
        allYValues += list(yValues)

    if (DisplayData == 'Yes'):
        gen.displayDf(dfX , allXValues , allYValues)
    if len(xValues) > 0:
        showFigure(Model, Corpus, xAxisIndex, yAxisIndex)
    else:
        print('No Data to plot')

if __name__ == '__main__':
    if (Corpus == 'All'):
        allCorpus = ["Aquaint", "Core17", "WAPO"]
    else:
        allCorpus = [Corpus]

    if (Model == 'All'):
        allModel = ["BM25", "LMD", "PL2"]
    else:
        allModel = [Model]

    for Corpus in allCorpus:
        for Model in allModel:
            # for xAxis in ["fbTerms", "TrecMAP"]:
            #     for yAxis in ["G - Cumulative", "G - Gravity"]:
                    csvPlot (Corpus, Model, DisplayData , xAxis ,  yAxis)

# xAxis = "fbTerms" # @param ["fbTerms", "TrecMAP","CWLMAP"]
# yAxis = "G - Cumulative"  # @param ["G - Cumulative", "G - Gravity","TrecMAP","CWLMAP"]