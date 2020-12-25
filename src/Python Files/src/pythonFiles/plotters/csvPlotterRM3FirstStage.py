'''
This Module is For Plotting First Stage Of Qry Expansion Experiments Number 2
Given FBTerms (10,20,30)
Plot performance Results For RM3 execluding b = 0.75 which was chosen as a result from this Plot
'''

import src.pythonFiles.plotters.csvPlotterGeneral as gen


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
Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO", 'All']
Model = "BM25"  # @param ["BM25", "LMD", "PL2", 'All']
# @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 5  # @param {type:"slider", min:0, max:16, step:1}
# @markdown Use Predefined ticks from ticks.csv files
##PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "No"  # @param ["Yes", "No"]

xAxis = "modelCoefficient"  # @param ["modelCoefficient"]
fbTerms = 10 # @param {type:"slider", min:10, max:30, step:10}
yAxis = "TrecMAP"  # @param ["TrecMAP","CWLMAP",'TrecNDCG', "BPref", "P10","RBP0.6" , "RBP0.8"]
# *** End Colab Form ***

def getSingleAxisValues(df , axis , fbDocs):
    criteria = df['fbDocs'] == fbDocs
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):

    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # Performance Extension (Trec or CWL)

    # perType = gen.getPerformanceType(yAxisIndex)
    # if perType != '':
    #     pltTitle += '- ' + perType.upper() + ' Measurement'

    # PlotType Title
    pltTitle += '\n RM3 - Based on Fbterms ' + str(fbTerms)

    # output Figure Name
    figName = gen.getFigureFileName(3, corpus, model,'', xAxisIndex, yAxisIndex)
    # if (gen.GCsvOutPath != ''):
    #     corpus = corpus[:2]
    #     xName = gen.getAxisName(xAxisIndex,model)
    #     yName = gen.getAxisName(yAxisIndex,model)
    #     b = ''
    #     if (yAxisIndex == gen.cumulativeGIndex ):
    #         b = 'GB0'
    #         figName = '-'.join([corpus,model,b,xName,yName])
    #     elif (yAxisIndex == gen.gravityGIndex):
    #         b = 'GB0.5'
    #         figName = '-'.join([corpus, model, b, xName, yName])
    #     else:
    #         figName = '-'.join([corpus, model, xName, yName])

    # xTicks = [0,55,5]
    # [0, 1.1, 0.1] [100, 1100.1, 100] [0,55,5]
    xTicks = ''
    yTicks = ''
    legendTitle = 'fbDocs'
    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle, xTicks, yTicks, figName)

def csvPlot(Corpus, Model, DisplayData , yAxis):

    gen.initializeGlobals('W',2,'')
    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)

    criteria = {
        'corpus':Corpus,
        'model':Model,
        'fbTerms':fbTerms,
        'qryExpansion':'RM3',
        'beta':0.5,
        'RetrievalCoefficient-!':0.75
    }

    df = gen.getDataFrame(criteria , xAxisIndex)
    allXValues = []
    allYValues = []
    gen.setFigNum()
    for fbDocs in range(5,55,5):
        label = fbDocs
        marker = gen.getMarker(str(fbDocs))
        xValues = getSingleAxisValues(df,xAxisIndex,fbDocs)
        yValues = getSingleAxisValues(df,yAxisIndex,fbDocs)
       # gen.plotList(xValues[:-1],yValues[:-1],label,marker)
        gen.plotList(xValues, yValues, label, marker)
        allXValues += list(xValues)
        allYValues += list(yValues)

    if (DisplayData == 'Yes'):
        gen.displayDf(df, allXValues, allYValues)

    if len(xValues) > 0:
        showFigure(Model,Corpus,xAxisIndex,yAxisIndex)
    else:
        print('No Data to plot')

if __name__ == '__main__':
    [allCorpus,allModel] = gen.getAll(Corpus,Model)
    for Corpus in allCorpus:
        for Model in allModel:
            # for fbTerms in range(10,40,10):
                csvPlot(Corpus, Model, DisplayData, yAxis)