INSERT INTO hp_zp_mappings SELECT *
                           FROM CSVREAD('${import.path}/hpZpMapping.pg',
                                        'mapping_id|hp_id|hp_term|zp_id|zp_term|simJ|ic|score|lcs_id|lcs_term',
                                        'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

