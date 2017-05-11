CREATE View cluster_annotation_view AS Select cs.cluster_structure_id as cluster_structure_id,c.cluster_id as cluster_id, 
ce.accession as cluster_acc, ie.ent_id as element_id, ie.accession as element_accession,representant_value as representant
from annotation_cluster_structure cs, annotation_cluster c, annotation_cluster_elem elem, annotation_cluster_representant rep,
entity ce, entity ie where
cs.cluster_structure_id  = c.cluster_structure_id_fk AND
c.cluster_id = elem.cluster_id_fk AND
elem.cluster_structure_id_fk = cs.cluster_structure_id AND
rep.cluster_id_fk = c.cluster_id AND 
rep.cluster_structure_id_fk = cs.cluster_structure_id AND 
ce.ent_id = c.cluster_id AND
ie.ent_id = elem.entity_id_fk order by cluster_id