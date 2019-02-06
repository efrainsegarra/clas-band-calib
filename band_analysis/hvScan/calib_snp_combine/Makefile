.DELETE_ON_ERROR:

CXX         := g++
CXXFLAGS    := -std=c++11 -O2 -Wall -fPIC
LD          := g++
LDFLAGS     := -O2

DEFINES     := -DWITHRINT

SRC_FILES   := $(wildcard *.cxx)
EXE_FILES   := $(SRC_FILES:%.cxx=%) 

.PHONY: all clean

all: $(EXE_FILES)


%: %.o
	$(LD) $(LDFLAGS) $^ $(LIBS) -o $@

%.o: %.cxx
	$(CXX) $(CXXFLAGS) $(DEFINES) -c $< -o $@

clean:
	rm -f $(EXE_FILES:%=%.o) $(EXE_FILES)
