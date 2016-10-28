package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class EncodedClusterTransformation {

	public static int id=-1;
	
	Logger log = Logger.getLogger(getClass());
	public EncodedEntityStructure transformToEncodedStructure(Collection<TokenCluster> cliques, String attributeName){
		EntityStructureVersion esv =this.transformToEntityStructureVersion(cliques, attributeName);
		EncodedEntityStructure ees= EncodingManager.getInstance().encoding(esv, true);
		return ees;
	}
	
	
	
	public EntityStructureVersion transformToEntityStructureVersion (Collection<TokenCluster> collection, String attributeName){
		VersionMetadata  vm =new VersionMetadata (id, null, null, "clusters", "keywords");
		EntityStructureVersion esv = new EntityStructureVersion(vm);
		StringBuffer sb = new StringBuffer();
		GenericProperty prop = new GenericProperty(0, attributeName, null, null);
		esv.addAvailableProperty(prop);
		int pvId =0;
		for (TokenCluster c :collection){
			GenericEntity ge = new GenericEntity (c.getClusterId(), "c:"+c.getClusterId(), "cluster",id);
			for (int tid: c.getTokenIds()){
				sb.append(EncodingManager.getInstance().getReverseDict().get(tid)+" ");
			}
			PropertyValue pv = new PropertyValue(pvId++,sb.toString().trim());
			ge.addPropertyValue(prop, pv);
			esv.addEntity(ge);
			sb.delete(0, sb.length());
		
		}
		id--;
		return esv;
	}
}
