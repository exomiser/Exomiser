INSERT INTO fish_gene_level_summary SELECT * FROM CSVREAD('${import.path}/fishGeneLevelSummary.pg', 'disease_id|zfin_gene_id|zfin_gene_symbol|max_combined_perc','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

