  /*not covered annotations for evaluation data set*/
 Select * from annotations a, entity e, entity src, not_covered nc, entity_structure es
 where a.target_entity_id = e.ent_id 
 AND a.src_entity_id = src.ent_id
 AND e.accession = nc.acc 
 AND src.ent_struct_id_fk = es.ent_struct_id 
 AND es.ent_type = 'eligibility criteria' 
 AND es.name  in ("NCT00168051","NCT00355849","NCT00175903","NCT00356109","NCT00357227",
					"NCT00359762","NCT00372229","NCT00190047","NCT00373373","NCT00195507",
					"NCT00376337","NCT00384046","NCT00385372","NCT00391287","NCT00391872",
					"NCT00393692","NCT00006045","NCT00048295","NCT00151112","NCT00153062",
					"NCT00156338","NCT00157157","NCT00160524","NCT00160706","NCT00165828"); 
 
 /*number of annotations for all ontologies*/
 Select * from annotations a, annotation_mapping am, entity_structure es 
 where a.mapping_id_fk = am.mapping_id 
 AND es.ent_struct_id = am.src_ent_struct_version_id_fk
 AND es.ent_type = 'eligibility criteria' 
 AND es.name in ("NCT00168051","NCT00355849","NCT00175903","NCT00356109","NCT00357227",
					"NCT00359762","NCT00372229","NCT00190047","NCT00373373","NCT00195507",
					"NCT00376337","NCT00384046","NCT00385372","NCT00391287","NCT00391872",
					"NCT00393692","NCT00006045","NCT00048295","NCT00151112","NCT00153062",
					"NCT00156338","NCT00157157","NCT00160524","NCT00160706","NCT00165828");
                    
 /*coverage ontology and concepts of reference mapping*/
 Select SAB, Count(Distinct(umls.CUI)) from UMLS2014AB.MRCONSO umls, covered_concepts cc
 where LAT = 'ENG' AND umls.CUI = cc.accession group by SAB ;    
    
    
    /*
    coverage by combination of different ontologies
    */
Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'CHV'
    union
    Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'NCI'
    union
    Select Distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'MTH'
      union
    Select DISTINCT CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'SNOMEDCT_US'
     union
    Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'MSH'
     union
    Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'MDR'
    ;
    
    
     Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'SNOMEDCT_US'
     union
    Select CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'NCI'
    union
    Select distinct CUI from covered_concepts cc, UMLS2014AB.MRCONSO umls 
 where cc.accession = umls.CUI
	AND LAT = 'ENG' AND umls.SAB = 'CHV';
                       
                    
 