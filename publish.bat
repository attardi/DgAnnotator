@echo off
mkdir dga
copy src\dga\DGA*xml dga
cd ..
\usr\bin\tar cf dga.t DgAnnotator/dga.jar DgAnnotator/Dga.png DgAnnotator/bin  DgAnnotator/Doc DgAnnotator/dga
\usr\bin\gzip -S gz dga.t
\usr\bin\pscp -p DgAnnotator/dga.msi dga.tgz medialab.di.unipi.it:/project/medialab/Project/QA/Parser/DgAnnotator
del dga.tgz
cd DgAnnotator
del dga\*
rmdir dga