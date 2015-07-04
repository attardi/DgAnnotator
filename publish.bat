@echo off
mkdir dga
copy src\dga\DGA*xml dga
cd ..
\usr\bin\tar cf dga.t DgAnnotator/dga.jar DgAnnotator/Dga.png DgAnnotator/Doc DgAnnotator/dga DgAnnotator/lib
\usr\bin\gzip -fS gz dga.t
\usr\bin\pscp -p DgAnnotator/dga.msi dga.tgz medialab.di.unipi.it:/project/medialab/Project/QA/Parser/DgAnnotator
del dga.tgz
cd DgAnnotator
del /Q dga\*
rmdir dga