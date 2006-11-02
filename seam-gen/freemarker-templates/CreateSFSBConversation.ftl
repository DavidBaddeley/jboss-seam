package ${packageName};

import java.io.Serializable;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

@Stateful
@Name("${actionName}")
public class ${actionName}Action implements ${actionName} {
	
    private @Logger log;
	
	@Begin
	public String begin()
	{
       //implement your begin conversation business logic
       log.info("beginning conversation");
	   return "success";
	}
	
	//add additional action methods that participate in this conversation
	
	@End
	public String end()
	{
        //implement your end conversation business logic
        log.info("ending conversation");
		return "success";
	}	
	
	@Destroy @Remove                                                                      
	public void destroy() {}	
}
