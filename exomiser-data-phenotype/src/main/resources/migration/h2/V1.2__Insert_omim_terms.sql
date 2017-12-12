INSERT INTO omim_terms SELECT *
                       FROM CSVREAD('${import.path}/omimTerms.pg', 'omim_disease_id|omim_terms',
                                    'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');