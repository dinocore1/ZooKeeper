cmake_minimum_required(VERSION 3.2 FATAL_ERROR)

project(add VERSION 0.1.0 LANGUAGES CXX C)

OPTION(BUILD_TESTS "Build unit tests" ON)

find_package(gtest2 REQUIRED)

add_library(add
  include/example.h
  src/add.cpp
)

target_include_directories(add
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
  $<INSTALL_INTERFACE:include>
  PRIVATE src
)


install(TARGETS add
  EXPORT addConfig
  RUNTIME DESTINATION bin
  LIBRARY DESTINATION lib
  ARCHIVE DESTINATION lib/static
)

install(DIRECTORY include/
  DESTINATION include
)

install(EXPORT addConfig
  DESTINATION cmake
)
