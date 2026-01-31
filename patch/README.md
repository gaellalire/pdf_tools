cd patch/target/classes/
cp ~/.m2/repository/com/itextpdf/sign/9.5.0/sign-9.5.0.jar .
jar uf sign-9.5.0.jar com/itextpdf/signatures/PdfPadesSigner.class
mvn install:install-file \
   -Dfile=sign-9.5.0.jar \
   -DgroupId=fr.gaellalire.itextpdf \
   -DartifactId=sign \
   -Dversion=9.5.0 \
   -Dpackaging=jar \
   -DgeneratePom=true