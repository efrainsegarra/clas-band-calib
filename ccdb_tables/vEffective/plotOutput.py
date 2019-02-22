from __future__ import division
import numpy as np
import sys
import matplotlib.pyplot as plt

fadc_short = []
tdc_short = []
fadc_long = []
tdc_long = []
with open("output/effective_velocity.txt") as g:
	for line in g:
		li = line.strip().split("\t")

		if( float(li[3]) == 0): continue
		if( int(li[0]) == 3 or int(li[0]) == 4 ):
			tdc_short.append( float(li[3] ))
			fadc_short.append( float(li[4] ))
		else:
			tdc_long.append( float(li[3] ))
			fadc_long.append( float(li[4] ))

plt.scatter(fadc_short,tdc_short,color='red',label='Short Bars')
plt.scatter(fadc_long,tdc_long,color='blue',label='Long Bars')
plt.legend(numpoints=1,loc='best')
plt.xlabel('Speed of Light from FADC [cm/ns]')
plt.ylabel('Speed of Light from TDC [cm/ns]')
plt.xlim([11.5,14.5])
plt.ylim([11.5,14.5])
plt.grid(True)
plt.show()
