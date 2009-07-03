#!/bin/bash

longP="net/sourceforge/olduvai"
buildP="build"

#compile
javac $longP/treejuxtaposer/drawer/*java $longP/treejuxtaposer/*java

#copy
mkdir -p $buildP
for f in `find net/*`

  do test -d $f && mkdir -p $buildP/$f && cp $f/*class ${buildP}/$f
  done

cp ${longP}/treejuxtaposer/README ${buildP}/${longP}/treejuxtaposer
cp manifest.tj ${buildP}

#pack
pushd ${buildP}
TJ=tj-`date +%Y%m%d`.jar
jar cmf manifest.tj $TJ net/sourceforge/olduvai/treejuxtaposer
popd

mv ${buildP}/${TJ} .
