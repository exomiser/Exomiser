The Exomiser - Core Library
===============================================================

Generating the AlleleProto.java file
```bash
EXOMISER_SRC_MAIN=~/GitHub/Exomiser/exomiser-core/src/main
protoc --proto_path=$EXOMISER_SRC_MAIN/proto --java_out=$EXOMISER_SRC_MAIN/java $EXOMISER_SRC_MAIN/proto/allele.proto
```
