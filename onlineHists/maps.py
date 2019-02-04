import sys
# Takes in 1 four digit number (ie python maps.py 0010) and returns a PMT number (0 - 255)
# Indexing starts at 1 for all variables. Ordering is Layer Sector Component Order (left/right)
# For example, Section 1, Layer 3, Component 3, Left PMT, should be entered as python maps 1331.
# The order for VETO bars does not matter, but must be included (ie need to write 1611, not just 161)

def sect(x): return {0 : 0, 1 : 6, 2 : 20, 3 : 22, 4 : 40}.get(x, -11)
def veto(x): return {0 : 0, 1 : 3, 2 : 10, 3 : 11, 4 : 22}.get(x, -13)

def mapping (sector,layer,component,order):
  #Shift all values down by one because some people want to start indexing at 1 instead of at 0
  sector = sector - 1
  layer = layer - 1
  component = component - 1
  order = order - 1

  if sector == 2 or sector == 3:
    component_mod = component*2
  else:
    component_mod = component

  if layer == 5:
    #if sector == 2 or sector == 3:
  #    PMT = 232+veto(sector) + component*2
#  else:
      print(veto(sector),component_mod)
      PMT = 232+veto(sector) + component_mod
  else:
    PMT =  layer*48 + sect(sector) + component_mod*2+order


  print(PMT+1)

i = [int(d) for d in sys.argv[1]]
mapping(i[0],i[1],i[2],i[3])

"""
20 - 11
30 - 12
21 - 13
31 - 14
22 - 15
32 - 16
23 - 17
33 - 18
24 - 19
34 - 20
25 - 21
35 - 22
"""
