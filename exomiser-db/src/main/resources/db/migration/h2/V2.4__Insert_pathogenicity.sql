INSERT INTO pathogenicity SELECT *
                          FROM CSVREAD('${import.path}/variant.pg',
                                       'chromosome|position|ref|alt|sift|polyphen|mut_taster|cadd|cadd_raw',
                                       'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=null');
