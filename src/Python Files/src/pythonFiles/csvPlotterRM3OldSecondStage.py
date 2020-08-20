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
Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO"]
Model = "LMD"  # @param ["BM25", "LMD", "PL2"]
# @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
# @markdown Use Predefined ticks from ticks.csv files
##PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "Yes"  # @param ["Yes", "No"]

xAxis = "fbTerms"  # @param ["fbTerms"]
yAxis = "CWLMAP"  # @param ["TrecMAP","CWLMAP", "BPref", "P10","RBP0.6" , "RBP0.8"]
# *** End Colab Form ***

def getSingleAxisValues(df , axis , fbDocs):
    criteria = df['fbDocs'] == fbDocs
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):

    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # Performance Extension (Trec or CWL)

    perType = gen.getPerformanceType(yAxisIndex)
    if perType != '':
        pltTitle += '- ' + perType.upper() + ' Measurement'

    # PlotType Title
    pltTitle += '\n RM3 - Based on ' + gen.getModelCoefficient(model) + ' = ' + str(gen.getChosenCoefficient(model))

    # output Figure Name
    figName = ''
    if (gen.GCsvOutPath != ''):
        corpus = corpus[:2]
        xName = gen.getAxisName(xAxisIndex)
        yName = gen.getAxisName(yAxisIndex)
        b = ''
        if (yAxisIndex == gen.cumulativeGIndex ):
            b = 'GB0'
            figName = '-'.join(corpus,model,b,xName,yName)
        elif (yAxisIndex == gen.gravityGIndex):
            b = 'GB0.5'
            figName = '-'.join(corpus, model, b, xName, yName)
        else:
            figName = '-'.join(corpus, model, xName, yName)


    gen.showFigure(model,pltTitle,xAxisIndex,yAxisIndex , figName , 'RM3-2')

def csvPlot(Corpus, Model, DisplayData , yAxis):

    gen.initializeGlobals()
    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)
    df = gen.getDataFrame(yAxisIndex, DisplayData, Corpus, Model, -1)

    for fbDocs in range(5,50,5):
        label = fbDocs
        marker = gen.getMarker(str(fbDocs))
        xValues = getSingleAxisValues(df,xAxisIndex,fbDocs)
        yValues = getSingleAxisValues(df,yAxisIndex,fbDocs)
        gen.plotList(xValues,yValues,label,marker)
    if len(xValues) > 0:
        showFigure(Model,Corpus,xAxisIndex,yAxisIndex)
    else:
        print('No Data to plot')

if __name__ == '__main__':
    csvPlot (Corpus, Model, DisplayData , yAxis)