# Set up
cmake_minimum_required (VERSION 2.6)

# Project name
project (BAND_HV)

install (FILES BAND_HV-aftersingleCheckout.snp DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

# Add executable
add_executable(code code.cxx)
