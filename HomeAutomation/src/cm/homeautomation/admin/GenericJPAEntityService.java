package cm.homeautomation.admin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import cm.homeautomation.services.base.BaseService;

@Path("admin/entityservice")
public class GenericJPAEntityService extends BaseService {

	@GET
	@Path("getclasses")
	public List<GenericClassDescription> getSupportedClasses() {
		List<GenericClassDescription> classes=new ArrayList<GenericClassDescription>();
		
		Reflections reflections = new Reflections(
				new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(
						new SubTypesScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Entity.class);
		
		for (Class<?> declaringClass : typesAnnotatedWith) {
			GenericClassDescription clazz=new GenericClassDescription();
			clazz.setName(declaringClass.getName());
			
			Field[] fields = declaringClass.getDeclaredFields();
			for (Field field : fields) {
				clazz.getFields().put(field.getName(), field.getType().getSimpleName());
			}

			/*Method[] methods = declaringClass.getDeclaredMethods();
			for (Method method : methods) {
				clazz.getMethods().put(method.getName(), method.getParameters());
			}*/

			
			classes.add(clazz);
		}
		
		return classes;
	}

	
}
