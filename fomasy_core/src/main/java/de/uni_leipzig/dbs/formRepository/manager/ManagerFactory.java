package de.uni_leipzig.dbs.formRepository.manager;

public class ManagerFactory {

	private static FormManager formManager;
	
	private static MatchManager matchManager;

	private static MappingManagerImpl mappingManager;
	
	private static ClusterManagerImpl clusterManager;

	private static GraphManagerImpl graphManager;
	
	public static FormManager getFormManager(){
		if (formManager ==null){
			formManager = new FormManagerImpl();
		}
		return formManager;
	}
	
	public static MatchManager getMatchManager(){
		if (matchManager ==null){
			matchManager = new MatchManagerImpl();
		}
		return matchManager;
	}

	public static MappingManager getMappingManger() {
		// TODO Auto-generated method stub
		if (mappingManager ==null){
			mappingManager = new MappingManagerImpl();
		}
		return mappingManager;
	}

	public static ClusterManager getClusterManager() {
		if (clusterManager ==null){
			clusterManager = new ClusterManagerImpl();
		}
		return clusterManager;
	}

	public static GraphManager getGraphManager() {
		if (graphManager ==null){
			graphManager = new GraphManagerImpl();
		}
		return graphManager;
	}
}
