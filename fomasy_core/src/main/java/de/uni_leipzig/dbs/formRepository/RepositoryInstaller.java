package de.uni_leipzig.dbs.formRepository;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.exception.InstallationException;

public class RepositoryInstaller {

  /**
   * create the schema of the DB repository
   * @throws InstallationException
   */
  public void installRepository() throws InstallationException{
    APIFactory.getInstance().getRepositoryAPI().installRepository();
    System.out.println("Repository installed");
  }
  
  /**
   * remove the schema
   */
  public void deleteRepository (){
    APIFactory.getInstance().getRepositoryAPI().deleteRepository();
    System.out.println("Repository deinstalled");
  }
  
  public static void main (String[] args){
    FormRepository fr = new FormRepositoryImpl();
    RepositoryInstaller installer = new RepositoryInstaller();
    try {
      fr.initialize(args[0]);
      if (args[1].startsWith("-i")){
        installer.installRepository();
      }else if (args[1].startsWith("-d")){
        installer.deleteRepository();
      }
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InitializationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstallationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
