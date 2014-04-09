INSERT INTO mouse_gene_level_summary SELECT * FROM CSVREAD('${import.path}/mouseGeneLevelSummary.pg', 'disease_id|mgi_gene_id|mgi_gene_symbol|max_combined_perc','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=null');

