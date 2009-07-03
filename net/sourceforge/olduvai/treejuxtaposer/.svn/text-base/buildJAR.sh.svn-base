#!/bin/bash

website="https://olduvai.svn.sourceforge.net/svnroot/olduvai/trunk/olduvai"
longP="net/sourceforge/olduvai"
joglP="../jogl"
trunk="olduvaiTrunk"
buildP="build"

#checkout
mkdir -p ${trunk}/${longP}/treejuxtaposer
mkdir -p ${trunk}/${longP}/accordiondrawer
svn co ${website}/${longP}/treejuxtaposer ${trunk}/${longP}/treejuxtaposer
svn co ${website}/${longP}/accordiondrawer ${trunk}/${longP}/accordiondrawer

pushd $trunk

#compile
javac -classpath $joglP/jogl.jar:. $longP/accordiondrawer/*java $longP/treejuxtaposer/drawer/*java $longP/treejuxtaposer/*java

#copy
mkdir -p $buildP
for f in `find net/* | grep -v svn | grep -v images`

  do test -d $f && mkdir -p $buildP/$f && cp $f/*class ${buildP}/$f
  done

cp ${longP}/treejuxtaposer/README ${buildP}/${longP}/treejuxtaposer
cp ../manifest.tj ${buildP}

#pack
pushd ${buildP}
TJ=tj-`date +%Y%m%d`.jar
jar cmf manifest.tj $TJ net/sourceforge/olduvai/accordiondrawer net/sourceforge/olduvai/treejuxtaposer
popd

popd

mv ${trunk}/${buildP}/${TJ} .
