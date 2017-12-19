INSERT INTO hp_hp_mappings SELECT *
                           FROM CSVREAD('${import.path}/hpHpmapping.pg',
                                        'mapping_id|hp_id|hp_term|hp_id_hit|hp_hit_term|simJ|ic|score|lcs_id|lcs_term',
                                        'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=null');

