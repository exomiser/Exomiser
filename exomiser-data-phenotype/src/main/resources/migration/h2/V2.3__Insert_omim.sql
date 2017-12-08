INSERT INTO disease SELECT *
                    FROM CSVREAD('${import.path}/omim.pg',
                                 'disease_id|omim_gene_id|diseasename|gene_id|type|inheritance',
                                 'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
