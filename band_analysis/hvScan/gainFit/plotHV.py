from __future__ import division
import sys
import numpy as np
import matplotlib.pyplot as plt


if len(sys.argv)!=2:
	print 'Invalid number of arguments. Please use:\n\tpython plotHV.py [OutputOfComboPMTs.txt]'
	exit(-1)



hamHV = []
etHV = []
with open(sys.argv[1],'rb') as g:
	for line in g:
		if '#' in line: continue
		sector = int(line.strip().split("\t")[0])
		layer = int(line.strip().split("\t")[1])
		component = int(line.strip().split("\t")[2])
		if layer == 6: continue
		if sector < 3 or sector > 5 or layer == 5:
			hamHV.append( float(line.strip().split("\t")[3]) )
			hamHV.append( float(line.strip().split("\t")[4]) )
		else:
			etHV.append( float(line.strip().split("\t")[3]) )
			etHV.append( float(line.strip().split("\t")[4]) )

plt.figure(1)
plt.hist( etHV ,label='Electron Tube PMTs')
plt.legend(numpoints=1,loc='best')

plt.figure(2)
plt.hist( hamHV , label='Hamamatsu PMTs')
plt.legend(numpoints=1,loc='best')
plt.show()
			

