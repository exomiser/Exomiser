INSERT INTO hp_mp_mappings SELECT *
                           FROM CSVREAD('${import.path}/hpMpMapping.pg',
                                        'mapping_id|hp_id|hp_term|mp_id|mp_term|simJ|ic|score|lcs_id|lcs_term',
                                        'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

