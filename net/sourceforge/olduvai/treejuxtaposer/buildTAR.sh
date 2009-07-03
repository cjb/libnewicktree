#!/bin/bash

website="https://olduvai.svn.sourceforge.net/svnroot/olduvai/trunk/olduvai"
longP="net/sourceforge/olduvai"
joglP="../jogl"
trunk="olduvaiTrunk"
buildP="build"

#checkout
mkdir -p ${trunk}/${longP}/treejuxtaposer
mkdir -p ${trunk}/${longP}/accordiondrawer
mkdir -p ${trunk}/doc
svn co ${website}/${longP}/treejuxtaposer ${trunk}/${longP}/treejuxtaposer
svn co ${website}/${longP}/accordiondrawer ${trunk}/${longP}/accordiondrawer
svn co ${website}/doc ${trunk}/doc

pushd $trunk

#compile
javac -classpath $joglP/jogl.jar:. $longP/accordiondrawer/*java $longP/treejuxtaposer/drawer/*java $longP/treejuxtaposer/*java

#copy
mkdir -p $buildP
for f in `find net/* | grep -v svn | grep -v images`

  do test -d $f && mkdir -p $buildP/$f && mv $f/*class ${buildP}/$f
  done

cp ${longP}/treejuxtaposer/README ${buildP}/${longP}/treejuxtaposer
mv ../manifest.tj ${buildP}

popd

#pack
TJ=tj-`date +%Y%m%d`.tar
for f in `find $trunk | grep -v svn | grep -v revision` ; do test -d "$f" || tar rf $TJ "$f"; done;
gzip $TJ

cp ${trunk}/${buildP}/manifest.tj .
